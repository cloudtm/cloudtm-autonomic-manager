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

package Tas2.physicalModel.cpunet.cpu.two;

import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.exception.Tas2Exception;
import Tas2.physicalModel.cpunet.parameters.WorkloadParams;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 10/12/12
 */
public class CpuModel2RoA extends CpuModel2 {

   //private static final Log log = LogFactory.getLog(CpuModel2RoA.class);
   private static final double NANO_TO_SEC = 1e9;

   public CpuModel2RoA(int numCpuCores, CpuServiceTimes2 serviceTimes, DSTMScenarioTas2 scenario, WorkloadParams params) throws Tas2Exception {
      super(numCpuCores, serviceTimes, scenario, params);
   }

   /**
    * Number of times that a transaction DIES remotely
    *
    * @param params
    * @return
    */
   private double expectedRemotelyAbortedTrials(WorkloadParams params) {
      return expectedIncarnations(params) - 1D;
   }


   private double expectedIncarnations(WorkloadParams params) {
      double coher = params.getCoherProbability();
      return 1.0D / coher;
   }

   /**
    * Number of times a transaction dies locally
    *
    * @param params
    * @return
    */
   private double expectedLocallyAbortedTrials(WorkloadParams params) {
      double numIncarnationsThatGetToPrepare = expectedIncarnations(params);
      double perIncarnationLocalRetries = this.perIncarnationLocalRetries(params);
      return perIncarnationLocalRetries * numIncarnationsThatGetToPrepare;
   }

   /**
    * Number of times a transaction dies locally per incarnation
    *
    * @param params
    * @return
    */
   private double perIncarnationLocalRetries(WorkloadParams params) {
      return expectedSubIncarnations(params) - 1;
   }


   private double expectedSubIncarnations(WorkloadParams params) {
      double prep = params.getPrepareProbability();
      return (1.0D / prep);
   }

   @Override
   protected double nanoLambdaLocalUpdateTx(CpuServiceTimes2 serviceRates, WorkloadParams scenario) {

      double subIncarnations = this.expectedSubIncarnations(scenario);
      double incarnations = this.expectedIncarnations(scenario);
      double numNodes = scenario.getNumberOfNodes();
      double wrPercentage = scenario.getWrTxPercentage();
      double localLambda = scenario.getNanoTxLambda() * wrPercentage / numNodes;
      double reincarnatedLambda = localLambda * subIncarnations * incarnations;
       /*
      log.debug("RoA_Lamba = " + scenario.getNanoTxLambda() * NANO_TO_SEC);
      log.debug("RoA_LocalLambda = " + localLambda * NANO_TO_SEC);
      log.debug("RoA_Incarantions = " + incarnations);
      log.debug("RoA_CoherProb = " + scenario.getCoherProbability());
      log.debug("RoA_SubIncarnations = " + subIncarnations);
      log.debug("RoA_PrepreProb = " + scenario.getPrepareProbability());
      log.debug("RoA_TotalIncarnations = " + (incarnations * subIncarnations));
      log.debug("RoA_TotalLocalLambda = " + reincarnatedLambda * NANO_TO_SEC);
      */
      return reincarnatedLambda;
   }

   @Override
   protected double nanoLambdaRemoteUpdateTx(CpuServiceTimes2 serviceRates, WorkloadParams scenario) {
      double incarnations = this.expectedIncarnations(scenario);
      double numNodes = scenario.getNumberOfNodes();
      double wrPercentage = scenario.getWrTxPercentage();
      double remoteLambda = scenario.getNanoTxLambda() * wrPercentage * (numNodes - 1) / numNodes;
      remoteLambda *= incarnations;
      return remoteLambda;
   }

}
