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

package Tas2.physicalModel.cpunet.cpu.one;


import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.exception.Tas2Exception;
import Tas2.physicalModel.cpunet.cpu.CpuModel;
import Tas2.physicalModel.cpunet.parameters.WorkloadParams;
import Tas2.physicalModel.queues.QueueFactory;
import Tas2.util.Tas2Util;
import open.queues.OpenClazz;
import open.queues.Queue;


/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 24/11/12
 */
public class CpuModel1 extends CpuModel {
   final static int LOCAL_READONLY = 0;
   final static int LOCAL_UPDATE = 1;
   final static int REMOTE_UPDATE = 2;

   private int numCpuCores;
   private CpuServiceTimes1 serviceTimes;
   private WorkloadParams params;
   private DSTMScenarioTas2 scenarioTas2;

   private Queue cpuQueue;

   private boolean debug = false;

   public CpuModel1(int numCpuCores, CpuServiceTimes1 serviceTimes, DSTMScenarioTas2 scenario, WorkloadParams params) throws Tas2Exception {
      this.numCpuCores = numCpuCores;
      this.serviceTimes = serviceTimes;
      this.params = params;
      this.scenarioTas2 = scenario;
      this.cpuQueue = initCpuQueue();
   }

   public double getUtilization() {
      return cpuQueue.getRo();
   }

   public Queue initCpuQueue() throws Tas2Exception {
      OpenClazz[] cpuClazz = new OpenClazz[3];

      cpuClazz[LOCAL_READONLY] = initLocalCpuReadOnlyTxClass(serviceTimes, params);
      cpuClazz[LOCAL_UPDATE] = initLocalCpuUpdateTxClass(serviceTimes, params);
      cpuClazz[REMOTE_UPDATE] = initRemoteCpuUpdateTxClass(serviceTimes, params);

      Queue CpuQueue = QueueFactory.buildQueue(numCpuCores, "CPU", cpuClazz);

      //((YuLoadIndependentOpenMMK)CpuQueue).forceUtilization(this.scenarioTas2.getWorkParams().getCpu());

      Tas2Util.debug("CpuModel1\n" + cpuClazz[LOCAL_READONLY].toString() + "\n" + cpuClazz[LOCAL_UPDATE] + "\n" + cpuClazz[REMOTE_UPDATE]);

      return CpuQueue;
   }

   private OpenClazz initLocalCpuUpdateTxClass(CpuServiceTimes1 serviceRates, WorkloadParams params) {
      double meanUpdateS = this.meanUpdateLocalTxS(serviceRates, params);
      double lambda = this.nanoLambdaLocalUpdateTx(serviceRates, params);
      OpenClazz clazz = new OpenClazz(LOCAL_UPDATE, meanUpdateS, lambda);
      return clazz;
   }

   /**
    * Cpu demands of a local transaction
    *
    * @param serviceRates
    * @param params
    * @return
    */
   private double meanUpdateLocalTxS(CpuServiceTimes1 serviceRates, WorkloadParams params) {
      return noRetryOnAbortMeanUpdateLocalTxS(serviceRates, params);
   }

   private double noRetryOnAbortMeanUpdateLocalTxS(CpuServiceTimes1 serviceRates, WorkloadParams params) {
      double rollbackS = serviceRates.getRollbackS();
      double execNoContS = serviceRates.getExecNoContS();
      double prepareS = serviceRates.getPrepareS();
      double commitS = serviceRates.getCommitUpdateTxS();
      double flushS = serviceRates.getCoordFlushS();
      double p_p = params.getPrepareProbability();
      double p_la = params.getLocalAbortProbability();
      double p_ok = params.getCoherProbability();
      int Nl = (int) params.getWrPerTransaction();

      //Transaction is completed successfully
      double meanSuccessfulLocalTime = (p_p * p_ok) * (execNoContS + prepareS + commitS + flushS);
      //Transaction aborts locally
      double meanAbortedLocalTime = meanAbortedExecutionTime(execNoContS, Nl, p_la) + (1.0D - p_p) * rollbackS;
      //Transaction completes locally but fails remotely
      double meanAbortedRemoteTime = (p_p * (1.0D - p_ok)) * (execNoContS + prepareS + rollbackS);

      return meanAbortedLocalTime + meanAbortedRemoteTime + meanSuccessfulLocalTime;
   }

   /**
    * Cpu demands of a remote transaction
    *
    * @param serviceRates
    * @param params
    * @return
    */
   private double meanUpdateRemoteTxS(CpuServiceTimes1 serviceRates, WorkloadParams params) {
      return noRetryOnAbortMeanUpdateRemoteTxS(serviceRates, params);
   }


   private double noRetryOnAbortMeanUpdateRemoteTxS(CpuServiceTimes1 serviceRates, WorkloadParams params) {
      double rollbackS = serviceRates.getRollbackS();
      double replayS = serviceRates.getReplayS();
      double flushS = serviceRates.getCohortFlushS();
      double p_ok = params.getDaggerReplicationOkOnOneNodeProbability();  //based on P_dagger
      double conditionalRollbackTime = (1.0D - p_ok) * rollbackS;
      double p_ok_otherNodes = params.getCoherProbabilityOneNodeLess();
      int Nl = (int) (params.getWrPerTransaction());
      double p_dagger = params.getPDagger();

      //Replication on the node goes wrong
      double meanRemoteLocallyAbortedReplayTime = meanAbortedExecutionTime(replayS, Nl, p_dagger) + conditionalRollbackTime;
      //Replication on the node completes, but on another node it does not
      double meanRemoteRemotelyAbortedReplayTime = (p_ok * (1.0D - p_ok_otherNodes)) * (replayS + rollbackS);
      //Replication is completed system-wide
      //TODO there probabilites are a little sloppy
      double meanSuccessfulReplayTime = (replayS + flushS) * (p_ok * p_ok_otherNodes);

      return meanSuccessfulReplayTime + meanRemoteLocallyAbortedReplayTime + meanRemoteRemotelyAbortedReplayTime;
   }

   private OpenClazz initRemoteCpuUpdateTxClass(CpuServiceTimes1 serviceRates, WorkloadParams params) {
      double meanUpdateS = this.meanUpdateRemoteTxS(serviceRates, params);
      double lambda = this.nanoLambdaRemoteUpdateTx(serviceRates, params);
      OpenClazz clazz = new OpenClazz(REMOTE_UPDATE, meanUpdateS, lambda);
      return clazz;
   }

   private OpenClazz initLocalCpuReadOnlyTxClass(CpuServiceTimes1 serviceRates, WorkloadParams params) {
      double readOnlyS = serviceRates.getReadOnlyS();
      double readOnlyCommitS = serviceRates.getCommitReadOnlyTxS();
      readOnlyS += readOnlyCommitS;
      double readPercentage = 1.0D - params.getWrTxPercentage();
      double lambda = params.getNanoTxLambda() * readPercentage / params.getNumberOfNodes();
      Tas2Util.debug(debug, "initLocalCpuReadOnly: totalLambda " + params.getNanoTxLambda() + "" +
              " readOnlyLambda " + lambda);
      OpenClazz clazz = new OpenClazz(LOCAL_READONLY, readOnlyS, lambda);
      return clazz;
   }


   private double noRetryOnAbortNanoLambdaRemoteUpdateTx(CpuServiceTimes1 serviceRates, WorkloadParams params) {
      double prepareProb = params.getPrepareProbability();
      double lambda = params.getNanoTxLambda() * params.getWrTxPercentage();
      double numN = params.getNumberOfNodes();
      lambda = lambda * ((numN - 1.0D) / numN) * prepareProb;
      Tas2Util.debug(debug, "initRemoteCpuUpdate: totalLamdba " + params.getNanoTxLambda() + " prepareProb " + prepareProb + " " +
              "remoteLambda " + lambda);
      return lambda;
   }

   private double noRetryOnAbortNanoLambdaLocalUpdateTx(CpuServiceTimes1 serviceRates, WorkloadParams params) {
      double numN = params.getNumberOfNodes();
      double lambda = params.getNanoTxLambda() * params.getWrTxPercentage() / numN;

      Tas2Util.debug(debug, "initLocalCpuUpdate: totalLambda " + params.getNanoTxLambda() + "" +
              " readOnlyLambda " + lambda);
      return lambda;
   }

   private double nanoLambdaRemoteUpdateTx(CpuServiceTimes1 serviceRates, WorkloadParams params) {
      return this.noRetryOnAbortNanoLambdaRemoteUpdateTx(serviceRates, params);
   }

   private double nanoLambdaLocalUpdateTx(CpuServiceTimes1 serviceRates, WorkloadParams params) {
      return noRetryOnAbortNanoLambdaLocalUpdateTx(serviceRates, params);
   }


   public double getCpuExecNoContResponseTime() {
      return this.getResponseTime(this.serviceTimes.getExecNoContS());
   }

   public double getCpuReplayResponseTime() {
      return this.getResponseTime(this.serviceTimes.getReplayS());
   }

   public double getCpuPrepareResponseTime() {
      return this.getResponseTime(this.serviceTimes.getPrepareS());
   }

   public double getCpuCommitResponseTime() {
      return this.getResponseTime(this.serviceTimes.getCommitUpdateTxS());
   }

   public double getCpuFlushResponseTime() {
      return this.getResponseTime(this.serviceTimes.getCoordFlushS());
   }

   public double getCpuRollbackResponseTime() {
      return this.getResponseTime(this.serviceTimes.getRollbackS());
   }

   public double getCpuReadOnlyResponseTime() {
      return this.getResponseTime(this.serviceTimes.getReadOnlyS() + this.serviceTimes.getCommitReadOnlyTxS());
   }

   private double getResponseTime(double serviceTime) {
      return this.cpuQueue.getResponseTimeByServiceTime(serviceTime);
   }

}
