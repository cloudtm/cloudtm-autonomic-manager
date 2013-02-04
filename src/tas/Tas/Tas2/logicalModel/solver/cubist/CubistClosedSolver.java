/*
 *
 *  * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
 *  * Copyright 2013 INESC-ID and/or its affiliates and other
 *  * contributors as indicated by the @author tags. All rights reserved.
 *  * See the copyright.txt in the distribution for a full listing of
 *  * individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 3.0 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package Tas2.logicalModel.solver.cubist;

import Tas2.config.configs.ClosedSolverConfig;
import Tas2.config.configs.Configuration;
import Tas2.config.configs.CubistConfig;
import Tas2.config.configs.Tas2Configuration;
import Tas2.core.Metrics;
import Tas2.core.ModelResult;
import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.core.environment.WorkParams;
import Tas2.exception.Tas2Exception;
import Tas2.logicalModel.solver.Solver;
import Tas2.logicalModel.solver.TPC.one.TPCClosedSolver;
import Tas2.logicalModel.solver.closed.ClosedSolver;
import Tas2.physicalModel.cpunet.net.tas.FixedRttServiceTimes;
import Tas2.physicalModel.cpunet.net.tas.RttGenerator.RttGenerator;
import Tas2.physicalModel.cpunet.net.tas.RttGenerator.RttGeneratorFactory;
import oracle.AbtractOracle;
import oracle.Oracle;
import oracle.cubist.datasetBuilder.RangeScenario;
import oracle.cubist.datasetBuilder.SingleMatrixCubistDataSetBuilder;
import oracle.cubist.jni.JniCubistOracle;
import oracle.exceptions.OracleException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 02/12/12
 */
public class CubistClosedSolver implements Solver {

   private ClosedSolver closedSolver;
   private Oracle oracle;
   private String targetFeature;
   private double convergenceThreshold = 1e-2;
   private CubistConfig config;
   static final Log log = LogFactory.getLog(CubistClosedSolver.class);

   protected final static double IGNORED = -2;
   protected final static double TARGET = -1;

   double rttToCommit = .5D;

   public CubistClosedSolver(Configuration conf) throws Tas2Exception {
      this.closedSolver = new TPCClosedSolver(conf);
      this.config = ((Tas2Configuration) conf).getCubistConfig();
      initCubist(conf);
   }

   public final ModelResult solve(DSTMScenarioTas2 scenario) throws Tas2Exception {
      return __solve(this.closedSolver, scenario, this.config);
   }

   protected ModelResult __solve(ClosedSolver solver, DSTMScenarioTas2 scenario, CubistConfig conf) throws Tas2Exception {
      ClosedSolverConfig.SOLVING_METHOD origin = this.config.getSolvingMethod();
      try {
         return this.__cascadeSolve(this.closedSolver, scenario, this.config);
      } finally {
         this.config.setSolvingMethod(origin.toString());
      }
   }

   protected ModelResult _solve(DSTMScenarioTas2 scenario) throws Tas2Exception {
      return this.closedSolver.solve(scenario);
   }

   private void initCubist(Configuration con) throws Tas2Exception {

      Tas2Configuration conf = (Tas2Configuration) con;
      CubistConfig cconf = conf.getCubistConfig();

      String pathToCubist = cconf.getPathToCubist();
      String pathToOracle = cconf.getPathToOracle();
      int instances = cconf.getInstances();
      int committee = cconf.getCommittee();
      try {
         this.oracle = new JniCubistOracle(pathToCubist, instances, committee);
         this.targetFeature = cconf.getTargetFeature();
         this.convergenceThreshold = cconf.getConvergenceThreshold();
         initDataSet(cconf, (AbtractOracle) oracle);
      } catch (OracleException o) {
         throw new Tas2Exception(o.getMessage());
      }
   }

   private void initDataSet(CubistConfig conf, AbtractOracle oracle) {
      SingleMatrixCubistDataSetBuilder builder = new SingleMatrixCubistDataSetBuilder(features(), conf.getPathToData());
      //builder.addFilter("failureProb", 0, 0.7);
      boolean overwriteOldModel = false;
      try {
         if (overwriteOldModel)
            builder.createDataset(conf.getPathToQueryData() + conf.getTargetFeature() + ".data", buildRangeScenarios(conf));
         oracle.loadDataset(conf.getTargetFeature(), conf.getPathToQueryData());
         oracle.loadModel(conf.getTargetFeature(), conf.getPathToQueryData(), overwriteOldModel);
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(-1);
      }
   }

   protected double queryCubist(String s) throws OracleException {
      return this.oracle.query(s, this.targetFeature);
   }

   private String[] features() {
      return new String[]{"Numkeys", "Warehouses", "NumNodes", "NumThread", "TxNetThroughput", "Throughput", "2PCBytes(Prep+Comm)", "TASRtt", "CPUUsage", "MemoryUsage"};
   }

   private RangeScenario[] buildRangeScenarios(CubistConfig bfc) {
      double minReads = bfc.getMinReads();
      double maxReads = bfc.getMaxReads();
      double minWrites;
      double maxWrites = bfc.getMaxWrites();
      double minNodes = bfc.getMinNodes();
      double maxNodes = bfc.getMaxNodes();
      double minThreads = bfc.getMinThreads();
      double maxThreads = bfc.getMaxThreads();
      double minWarehouses = bfc.getMinWarehouses();
      double maxWarehouses = bfc.getMaxWarehouses();
      double wh;
      ArrayList<RangeScenario> vec = new ArrayList<RangeScenario>();
      for (; minReads <= maxReads; minReads = nextKeys(minReads)) {

         for (minWrites = bfc.getMinWrites(); minWrites <= maxWrites; minWrites = nextWrites(minWrites)) {
            //rg
            if (minWarehouses == 0) {
               RangeScenario newR = new RangeScenario(minReads, minWrites, minNodes, maxNodes, minThreads, maxThreads);

               vec.add(newR);
            }
            //tpcc
            else {
               for (double w : bfc.warehouses()) {
                  //for (wh = minWarehouses; wh <= maxWarehouses; wh += 1) {

                  RangeScenario newR = new RangeScenario(minReads, minWrites, w, minNodes, maxNodes, minThreads, maxThreads);

                  vec.add(newR);
               }

            }
         }
      }
      return vec.toArray(new RangeScenario[vec.size()]);
   }

   private static double nextKeys(double d) {
      //return d * 10;
      if (d == 5343.0)
         return 9005;
      else
         return 9006;
   }

   private static double nextWrites(double d) {
      if (d == 4)
         return 13;
      else
         return 14;
      //return d * 10;
   }

   private ModelResult _solve(ClosedSolver closedSolver, DSTMScenarioTas2 scenario, RttGenerator rttGenerator) throws Tas2Exception {
      double inRtt, outRtt, newRtt = rttGenerator.initRtt();
      int iteration = 0;
      ModelResult result;
      double threshold = this.config.getConvergenceThreshold();

      do {
         inRtt = newRtt;
         log.debug("ClosedSolver init iteration " + iteration + " inputRtt " + inRtt);
         //scenario.setNetServiceTimes(new FixedRttServiceTimes(inRtt));
         scenario.setNetServiceTimes(new FixedRttServiceTimes(inRtt, inRtt * rttToCommit));
         result = closedSolver.solve(scenario);
         outRtt = this.computeNewRtt(result, scenario);
         newRtt = rttGenerator.newRtt(inRtt, outRtt);
         log.debug("ClosedSolver end iteration " + iteration + " newRtt " + newRtt);
         iteration++;
      }
      while (!rttGenerator.converge(inRtt, outRtt, newRtt, threshold));
      return result;
   }

   private double extractRtt(ModelResult result) {
      return result.getMetrics().getPrepareRtt();
   }

   private boolean converge(double exLamdba, double newLambda) {
      return Math.abs(exLamdba - newLambda) / exLamdba < convergenceThreshold;
   }

   private ModelResult __cascadeSolve(ClosedSolver solver, DSTMScenarioTas2 dstm, CubistConfig conf) throws Tas2Exception {
      RttGenerator rttGenerator = RttGeneratorFactory.buildRttGenerator(conf);
      switch (conf.getSolvingMethod()) {
         case PURE_RECURSION: {
            try {
               log.debug("Trying Recursively");
               ModelResult result = _solve(solver, dstm, rttGenerator);
               log.warn("Recursion: final rtt " + result.getMetrics().getPrepareRtt());
               return result;
            } catch (Tas2Exception t) {
               conf.setSolvingMethod(ClosedSolverConfig.SOLVING_METHOD.BINARY_SEARCH.toString());
               return __cascadeSolve(solver, dstm, conf);
            }
         }
         case BINARY_SEARCH: {
            try {
               log.debug("Trying Binary Search");
               ModelResult result = _solve(solver, dstm, rttGenerator);
               log.warn("Binary: final rtt " + result.getMetrics().getPrepareRtt());
               return result;
            } catch (Tas2Exception t) {
               conf.setSolvingMethod(ClosedSolverConfig.SOLVING_METHOD.ITERATIVE.toString());
               return __cascadeSolve(solver, dstm, conf);
            }
         }
         default: {
            log.debug("Trying Iterativevely");
            ModelResult result = _solve(solver, dstm, rttGenerator);
            log.warn("Iterative: final rtt " + result.getMetrics().getPrepareRtt());
            return result;
         }

      }

   }

   protected double computeNewRtt(ModelResult result, DSTMScenarioTas2 scenario) throws Tas2Exception {
      String string = extractString(result, scenario);
      double newRtt;
      try {
         newRtt = queryCubist(string);
      } catch (OracleException o) {
         throw new Tas2Exception(o.getMessage());
      }
      if (newRtt == 0)
         throw new Tas2Exception("Cubist predicts a null Rtt");
      return newRtt;
   }

   private String extractString(ModelResult result, DSTMScenarioTas2 scenario) {
      WorkParams params = scenario.getWorkParams();
      Metrics metrics = result.getMetrics();
      double reads = IGNORED;
      double warehouses = IGNORED;

      double memory = params.getMem();
      double mexSize = params.getPrepareMessageSize();
      double numNodes = params.getNumNodes();
      double numThreads = params.getThreadsPerNode();
      double cpu = metrics.getCpuUtilization();
      double netTh = metrics.getNetThroughput() * 1e9;
      double th = metrics.getThroughput() * 1e9;
      double tasRtt = TARGET;


      return build(reads, warehouses, numNodes, numThreads, netTh, th, mexSize, tasRtt, cpu, memory);
   }

   private String build(double... features) {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (double d : features) {
         if (first) {
            first = !first;
         } else {
            sb.append(",");
         }
         if (d == TARGET)
            sb.append("?");
         else
            sb.append(d);
      }
      return sb.toString();
   }
}
