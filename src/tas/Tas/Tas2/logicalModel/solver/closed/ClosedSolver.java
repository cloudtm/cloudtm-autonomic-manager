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

package Tas2.logicalModel.solver.closed;


import Tas2.config.configs.ClosedSolverConfig;
import Tas2.config.configs.Configuration;
import Tas2.config.configs.Tas2Configuration;
import Tas2.core.Metrics;
import Tas2.core.ModelResult;
import Tas2.core.Tas2Metrics;
import Tas2.core.Tas2ModelResult;
import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.core.environment.WorkParams;
import Tas2.exception.Tas2Exception;
import Tas2.logicalModel.solver.AbstractSolver;
import Tas2.logicalModel.solver.closed.lambda.LambdaGenerator;
import Tas2.logicalModel.solver.closed.lambda.LambdaGeneratorFactory;
import Tas2.logicalModel.solver.open.AbstractOpenSolver;
import Tas2.logicalModel.solver.probabilities.Probabilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */
public abstract class ClosedSolver extends AbstractSolver {

   AbstractOpenSolver openSolver;
   private static double THRESHOLD = 5e-2;
   private boolean validateUnstable = false;
   private ClosedSolverConfig closedConfig;

   private static final Log log = LogFactory.getLog(ClosedSolver.class);


   public ClosedSolver(Configuration conf) {
      super(conf);

      closedConfig = ((Tas2Configuration) conf).getClosedSolverConfig();
      THRESHOLD = closedConfig.getConvergenceThreshold();
      validateUnstable = closedConfig.isValidateUnstable();
   }

   public final ModelResult solve(DSTMScenarioTas2 scenario) throws Tas2Exception {
      this.openSolver = buildOpenSolver(this.configuration);
      return this._solve(openSolver, scenario);
   }


   private ModelResult ___solve(AbstractSolver solver, DSTMScenarioTas2 dstm, LambdaGenerator lambdaGenerator) throws Tas2Exception {
      WorkParams scenario = dstm.getWorkParams();
      double exLambda, newLambda = lambdaGenerator.initLambda(), outLambda;
      ModelResult result = null, temp = null;
      int iteration = 0;
      double threshold = configuration().getClosedSolverConfig().getConvergenceThreshold();
      do {
         exLambda = newLambda;
         log.trace("+++ " + iteration + " inputLambda " + exLambda * 1e9 + " +++");
         scenario.setLambda(exLambda);
         try {

            temp = solver.solve(dstm);
            if (accept(temp))
               result = temp;
            else {
               String warn = "Current result has a cpuU value higher than allowed (" + closedConfig.getCutCpuU() + " with a lambda " + exLambda * 1e9 + ")";
               // setThroughputs(result,dstm);
               //return result;
               log.trace(warn);
               throw new Tas2Exception(warn);
            }
         } catch (Tas2Exception tas) {
            if (true)
               throw tas;
            if (result == null) {
               String msg = tas.getMessage();
               String newMsg = "No stable solution found even with lambda " + exLambda * 1e9 + "!. ".concat(msg);
               throw new Tas2Exception(newMsg);
            }
            if (false) {
               setThroughputs(result, dstm);
               return result;
            }
            setThroughputs(result, dstm);
            if (isAnyWayTheBest(exLambda, result, configuration().getClosedSolverConfig())) {
               assert result instanceof Tas2ModelResult;
               if (!validateUnstable)
                  ((Tas2ModelResult) result).markAsUnstable();

               log.info(tas.getMessage() + "\nCorresponding lambda = " + exLambda * 1e9);
               return result;
            }
            log.trace("Convergence threshold is " + threshold + " Exception : \n" + tas.getMessage());
            throw tas;
         }
         outLambda = this.extractOutputThroughput(result, dstm);
         newLambda = lambdaGenerator.getNewLambda(exLambda, outLambda);
         log.trace("+++ " + iteration + " outLambda " + outLambda * 1e9 + " newLambda " + newLambda * 1e9 + " +++");
         iteration++;
      }
      while (!lambdaGenerator.converge(exLambda, outLambda, newLambda, threshold)); //The convergence has to be done inputL-->outputT from the model!
      setThroughputs(result, dstm);
      return result;
   }


   private boolean accept(ModelResult res) {
      return res.getMetrics().getCpuUtilization() <= closedConfig.getCutCpuU();
   }


   /**
    * @param temp   Last stable results, if any
    * @param config Configuration of the closed solver
    * @return True if there is any stable result and we are in the iterative mode
    */
   private boolean isAnyWayTheBest(double inputLambda, ModelResult temp, ClosedSolverConfig config) {
      boolean isLastSolverToResortTo = config.getSolvingMethod().equals(ClosedSolverConfig.SOLVING_METHOD.ITERATIVE) && temp != null;
      boolean isAcceptableResult = converge(inputLambda, temp.getMetrics().getThroughput());
      return isAcceptableResult && isLastSolverToResortTo;
   }

   private ModelResult __cascadeSolve(AbstractSolver solver, DSTMScenarioTas2 dstm, ClosedSolverConfig conf) throws Tas2Exception {
      LambdaGenerator lambdaGenerator = LambdaGeneratorFactory.buildLambdaGenerator(conf);
      switch (conf.getSolvingMethod()) {
         case PURE_RECURSION: {
            try {
               log.trace("Trying Recursively");
               ModelResult result = ___solve(solver, dstm, lambdaGenerator);
               return result;
            } catch (Tas2Exception t) {
               conf.setSolvingMethod(ClosedSolverConfig.SOLVING_METHOD.BINARY_SEARCH.toString());
               return __cascadeSolve(solver, dstm, conf);
            }
         }
         case BINARY_SEARCH: {
            try {
               log.trace("Trying Binary Search");
               ModelResult result = ___solve(solver, dstm, lambdaGenerator);
               return result;
            } catch (Tas2Exception t) {
               conf.setSolvingMethod(ClosedSolverConfig.SOLVING_METHOD.ITERATIVE.toString());
               return __cascadeSolve(solver, dstm, conf);
            }
         }
         default: {
            log.trace("Trying Iterativevely");
            ModelResult result = ___solve(solver, dstm, lambdaGenerator);
            return result;
         }

      }

   }


   private ModelResult _solve(AbstractSolver solver, DSTMScenarioTas2 dstm) throws Tas2Exception {
      ClosedSolverConfig.SOLVING_METHOD orig = configuration().getClosedSolverConfig().getSolvingMethod();
      ModelResult result;
      try {
         result = __cascadeSolve(solver, dstm, configuration().getClosedSolverConfig());
      } finally {
         configuration().getClosedSolverConfig().setSolvingMethod(orig.toString());
      }
      return result;

   }

   //TODO let the solver expose some interface to compute the throughput in case it is forced to be close
   private void setThroughputs(ModelResult modelResult, DSTMScenarioTas2 scenario) {

      if (scenario.getWorkParams().isRetryOnAbort())
         this.retryOnAbortSetThroughputs(modelResult, scenario);
      else
         this.noRetryOnAbortSetThroughputs(modelResult, scenario);
   }


   private void noRetryOnAbortSetThroughputs(ModelResult modelResult, DSTMScenarioTas2 scenario) {
      WorkParams workParams = scenario.getWorkParams();
      Metrics metrics = modelResult.getMetrics();
      Probabilities probs = modelResult.getProbabilities();
      double prepareProb = probs.getPrepareProbability();
      double wrPerc = workParams.getWritePercentage();
      double avgResp = metrics.getAvgResponseTime();
      double nodesThreads = workParams.getNumNodes() * workParams.getThreadsPerNode();
      double totalTh = nodesThreads / avgResp;
      double commitP = probs.getCommitProbability();
      double rdTh = totalTh * (1.0D - wrPerc);
      double wrTh = totalTh * wrPerc * commitP;
      double netTh = totalTh * wrPerc * prepareProb;
      ((Tas2Metrics) metrics).setReadThroughput(rdTh);
      ((Tas2Metrics) metrics).setThroughput(rdTh + wrTh);
      ((Tas2Metrics) metrics).setWriteThroughput(wrTh);
      ((Tas2Metrics) metrics).setNetThroughput(netTh);
   }

   private void retryOnAbortSetThroughputs(ModelResult modelResult, DSTMScenarioTas2 scenario) {
      WorkParams workParams = scenario.getWorkParams();
      Metrics metrics = modelResult.getMetrics();
      double wrPerc = workParams.getWritePercentage();
      double avgResp = metrics.getAvgResponseTime();
      double nodesThreads = workParams.getNumNodes() * workParams.getThreadsPerNode();
      double totalTh = nodesThreads / avgResp;
      double rdTh = totalTh * (1.0D - wrPerc);
      double wrTh = totalTh * wrPerc;
      Probabilities prob = modelResult.getProbabilities();
      double coher = prob.getCoherProbability();
      double reincarnations = 1.0D / (coher);
      double netTh = wrTh * reincarnations;   //TODO change (???)
      ((Tas2Metrics) metrics).setReadThroughput(rdTh);
      ((Tas2Metrics) metrics).setThroughput(rdTh + wrTh);
      ((Tas2Metrics) metrics).setWriteThroughput(wrTh);

      ((Tas2Metrics) metrics).setNetThroughput(netTh);
   }

   private double extractOutputThroughput(ModelResult result, DSTMScenarioTas2 scenario) {
      Metrics metrics = result.getMetrics();
      WorkParams workParams = scenario.getWorkParams();
      double nodesThreads = workParams.getNumNodes() * workParams.getThreadsPerNode();
      double avgResp = metrics.getAvgResponseTime();
      double totalTh = nodesThreads / avgResp;
      return totalTh;
   }


   protected abstract AbstractOpenSolver buildOpenSolver(Configuration conf);

   private boolean converge(double exLamdba, double newLambda) {
      return Math.abs(exLamdba - newLambda) / exLamdba < THRESHOLD;
   }

}
