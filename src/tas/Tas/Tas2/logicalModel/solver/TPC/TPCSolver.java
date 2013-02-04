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

package Tas2.logicalModel.solver.TPC;


import Tas2.config.Tas2ConfigurationFactory;
import Tas2.config.configs.Configuration;
import Tas2.core.ModelResult;
import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.exception.Tas2Exception;
import Tas2.logicalModel.solver.TPC.one.TPCAnalyticalModelNoRetryOnAbort;
import Tas2.logicalModel.solver.TPC.two.TPCAnalyticalModelNoRetryOnAbort2;
import Tas2.logicalModel.solver.TPC.two.TPCAnalyticalModelRetryOnAbort2;
import Tas2.logicalModel.solver.open.AbstractOpenSolver;
import Tas2.logicalModel.solver.probabilities.Probabilities;
import Tas2.physicalModel.PhysicalModel;
import Tas2.physicalModel.PhysicalModel2;
import Tas2.physicalModel.cpunet.CpuNetPhysicalOpenModel2;
import Tas2.physicalModel.cpunet.PureQueueCpuNetPhysicalOpenModel;
import Tas2.physicalModel.cpunet.QueuePlusCubistCpuNetPhysicalOpenModel;
import Tas2.physicalModel.cpunet.cpu.CpuServiceTimes;
import Tas2.physicalModel.cpunet.cpu.one.CpuServiceTimes1;
import Tas2.physicalModel.cpunet.net.queue.ByteNetServiceTimes;
import Tas2.physicalModel.cpunet.net.queue.NetServiceTimes;
import Tas2.physicalModel.cpunet.parameters.ExtendedServiceRates;
import Tas2.physicalModel.cpunet.parameters.ExtendedServiceRatesImpl;
import Tas2.physicalModel.cpunet.parameters.NoRetry2PCWorkLoadParams;
import Tas2.physicalModel.cpunet.parameters.WorkloadParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */

public class TPCSolver extends AbstractOpenSolver {

   private final static Log log = LogFactory.getLog(TPCSolver.class);

   public TPCSolver(Configuration conf) {
      super(conf);
   }

   @Override
   //Here I solve against a fixed lambda and a fixed set of probabilities.
   //I have to expose the new set of probabilities that I get
   protected ModelResult _solve(DSTMScenarioTas2 scenario, Probabilities probabilities) throws Tas2Exception {
      if (!scenario.getWorkParams().isRetryOnAbort()) {
         if (scenario.getCpuServiceTimes() instanceof CpuServiceTimes1) {
            log.trace("!Roa model with 1-modality");
            PhysicalModel physicalModel = (PhysicalModel) buildPhysicalModel(scenario, probabilities);
            TPCAnalyticalModelNoRetryOnAbort analyticalModel = new TPCAnalyticalModelNoRetryOnAbort();
            return analyticalModel._solve(physicalModel, scenario.getWorkParams(), probabilities);
         } else {
            log.trace("!Roa model with 2-modality");
            PhysicalModel2 physicalModel = (PhysicalModel2) buildPhysicalModel(scenario, probabilities);
            TPCAnalyticalModelNoRetryOnAbort2 analyticalModel = new TPCAnalyticalModelNoRetryOnAbort2();
            return analyticalModel._solve(physicalModel, scenario.getWorkParams(), probabilities);
         }
      } else {
         log.trace("RoA model! (Only with 2-modality)");
         //Tas2Util.debug(true, "TPCSolver: creating retryOnAbort TPC model");
         PhysicalModel2 physicalModel2 = (PhysicalModel2) buildPhysicalModel(scenario, probabilities);
         log.trace("PhysicalModel2 successfully created!");
         TPCAnalyticalModelRetryOnAbort2 analyticalModel = new TPCAnalyticalModelRetryOnAbort2();
         log.trace("TPCAnalyticalModelRoA successfully created! Trying to solve now!");
         return analyticalModel._solve(physicalModel2, scenario.getWorkParams(), probabilities);
      }
   }

   private WorkloadParams buildWorkloadParams(DSTMScenarioTas2 scenario, Probabilities probabilities) {
      NoRetry2PCWorkLoadParams workloadParams = new NoRetry2PCWorkLoadParams(scenario, probabilities);
      //Tas2Util.debug(false, "TPCSolver buildWorkLoadParams " + workloadParams);
      return workloadParams;
   }

   private ExtendedServiceRates buildExtendedServiceRates(DSTMScenarioTas2 scenario, Probabilities probabilities) {
      CpuServiceTimes cpuServiceTimes = scenario.getCpuServiceTimes();
      NetServiceTimes netServiceTimes = scenario.getNetServiceTimes();

      return new ExtendedServiceRatesImpl(cpuServiceTimes, netServiceTimes);
   }

   private Object buildPhysicalModel(DSTMScenarioTas2 scenario, Probabilities probabilities) throws Tas2Exception {
      ExtendedServiceRates serviceRates = buildExtendedServiceRates(scenario, probabilities);
      log.trace("ExtendedServiceRates successfully created");
      WorkloadParams workloadParams = buildWorkloadParams(scenario, probabilities);
      log.trace("WorkloadParams successfully created");
      return buildPhysicalModel(Tas2ConfigurationFactory.getConfiguration().getPhysicalConfiguration().getNumCores(), serviceRates, scenario, workloadParams);
   }

   private Object buildPhysicalModel(int numCores, ExtendedServiceRates serviceRates, DSTMScenarioTas2 scenario, WorkloadParams params) throws Tas2Exception {
      if (scenario.getCpuServiceTimes() instanceof CpuServiceTimes1) {
         if (scenario.getNetServiceTimes() instanceof ByteNetServiceTimes)
            return new PureQueueCpuNetPhysicalOpenModel(numCores, serviceRates, scenario, params);
         return new QueuePlusCubistCpuNetPhysicalOpenModel(numCores, serviceRates, scenario, params);
      }
      /*
         We are in the case of the new serviceTimes;
         This does not support the white model of the net (coz it does not work anyway)
         This support retry on abort
       */

      return new CpuNetPhysicalOpenModel2(numCores, serviceRates, scenario, params);


   }

}
