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
import Tas2.physicalModel.cpunet.cpu.CpuModel;
import Tas2.physicalModel.cpunet.parameters.WorkloadParams;
import Tas2.physicalModel.queues.QueueFactory;
import open.queues.OpenClazz;
import open.queues.Queue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 07/12/12
 */
public abstract class CpuModel2 extends CpuModel {

   final static int LOCAL_READONLY = 0;
   final static int LOCAL_UPDATE = 1;
   final static int REMOTE_UPDATE = 2;

   private final static boolean inject = false;

   private int numCpuCores;
   private CpuServiceTimes2 serviceTimes;
   private WorkloadParams params;
   private DSTMScenarioTas2 scenarioTas2;
   protected final static Log log = LogFactory.getLog(CpuModel2.class);

   private Queue cpuQueue;
   double NtS = 1e9;


   public CpuModel2(int numCpuCores, CpuServiceTimes2 serviceTimes, DSTMScenarioTas2 scenario, WorkloadParams params) throws Tas2Exception {
      this.numCpuCores = numCpuCores;
      this.serviceTimes = serviceTimes;
      this.params = params;
      this.scenarioTas2 = scenario;
      this.cpuQueue = initCpuQueue();
   }

   public double getUtilization() {
      return cpuQueue.getRo();
   }

   private void header(WorkloadParams params) {
      double lambda = params.getNanoTxLambda() * NtS;
      log.trace("WrPercentage " + params.getWrTxPercentage());
      log.trace("RdPercentage " + params.getRdTxPercentage());
      double nN = params.getNumberOfNodes();
      double ro = params.getRdTxPercentage() * lambda / nN;
      double lu = params.getWrTxPercentage() * lambda / nN;
      double ru = params.getWrTxPercentage() * lambda * (nN - 1) / nN;
      log.trace("Initing physicalModel\n" +
              "lambda = " + lambda +
              "\nroLambda = " + ro + "\nlocalUpdateLambda " + lu + "\nremoteUpdateLambda " + ru);
   }

   public Queue initCpuQueue() throws Tas2Exception {
      OpenClazz[] cpuClazz = new OpenClazz[3];
      header(params);

      cpuClazz[LOCAL_READONLY] = initLocalCpuReadOnlyTxClass(serviceTimes, params);

      cpuClazz[LOCAL_UPDATE] = initLocalCpuUpdateTxClass(serviceTimes, params);

      cpuClazz[REMOTE_UPDATE] = initRemoteCpuUpdateTxClass(serviceTimes, params);

      log.trace(cpuClazz[LOCAL_READONLY].toString(numCpuCores));
      log.trace(cpuClazz[LOCAL_UPDATE].toString(numCpuCores));
      log.trace(cpuClazz[REMOTE_UPDATE].toString(numCpuCores));

      Queue CpuQueue = QueueFactory.buildQueue(numCpuCores, "CPU", cpuClazz);
      log.trace("Cpu Utilization = " + CpuQueue.getRo());

      //((YuLoadIndependentOpenMMK)CpuQueue).forceUtilization(this.scenarioTas2.getWorkParams().getCpu());

      //((YuLoadIndependentOpenMMK)CpuQueue).forceUtilization((CpuQueue).getRo()*1.05);
      //Tas2Util.debug("CpuModel1\n" + cpuClazz[LOCAL_READONLY].toString() + "\n" + cpuClazz[LOCAL_UPDATE] + "\n" + cpuClazz[REMOTE_UPDATE]);

      return CpuQueue;
   }

   private OpenClazz initLocalCpuUpdateTxClass(CpuServiceTimes2 serviceRates, WorkloadParams params) {
      double meanUpdateS = this.meanUpdateLocalTxS(serviceRates, params);
      double lambda = this.nanoLambdaLocalUpdateTx(serviceRates, params);
      log.trace("initLocalUpdate RoALambda = " + lambda * NtS + " serviceTime " + meanUpdateS);
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
   private double meanUpdateLocalTxS(CpuServiceTimes2 serviceRates, WorkloadParams params) {
      double localRollback = serviceRates.getUpdateTxLocalLocalRollbackS();
      double remoteRollback = serviceRates.getUpdateTxLocalRemoteRollbackS();
      double localExec = serviceRates.getUpdateTxLocalExecutionS();
      double prepare = serviceRates.getUpdateTxPrepareS();
      double commit = serviceRates.getUpdateTxCommitS();
      double p_p = params.getPrepareProbability();
      double p_la = params.getLocalAbortProbability();
      double p_ok = params.getCoherProbability();
      int Nl = (int) params.getWrPerTransaction();

      //Transaction is completed successfully
      double meanSuccessfulLocalTime = (p_p * p_ok) * (localExec + prepare + commit);
      //Transaction aborts locally
      double meanAbortedLocalTime = meanAbortedExecutionTime(localExec, Nl, p_la) + (1.0D - p_p) * localRollback;
      //Transaction completes locally but fails remotely
      double meanAbortedRemoteTime = (p_p * (1.0D - p_ok)) * (localExec + prepare + remoteRollback);

      return meanAbortedLocalTime + meanAbortedRemoteTime + meanSuccessfulLocalTime;
   }

   /**
    * Cpu demands of a remote transaction
    *
    * @param serviceRates
    * @param params
    * @return
    */

   private double meanUpdateRemoteTxS(CpuServiceTimes2 serviceRates, WorkloadParams params) {
      double rollbackS = serviceRates.getUpdateTxRemoteRollbackS();
      double replayS = serviceRates.getUpdateTxRemoteExecutionS();
      double flushS = serviceRates.getUpdateTxRemoteCommitS();
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
      //TODO these probabilities are a little sloppy
      double meanSuccessfulReplayTime = (replayS + flushS) * (p_ok * p_ok_otherNodes);

      return meanSuccessfulReplayTime + meanRemoteLocallyAbortedReplayTime + meanRemoteRemotelyAbortedReplayTime;
   }

   private OpenClazz initRemoteCpuUpdateTxClass(CpuServiceTimes2 serviceRates, WorkloadParams params) {
      double meanUpdateS = this.meanUpdateRemoteTxS(serviceRates, params);
      double lambda = this.nanoLambdaRemoteUpdateTx(serviceRates, params);
      log.trace("initRemoteCpuUpdateTx: RoALambbda = " + lambda * NtS + " serviceTime = " + meanUpdateS);
      OpenClazz clazz = new OpenClazz(REMOTE_UPDATE, meanUpdateS, lambda);
      return clazz;
   }

   private OpenClazz initLocalCpuReadOnlyTxClass(CpuServiceTimes2 serviceRates, WorkloadParams params) {
      double local = serviceRates.getReadOnlyTxLocalExecutionS();
      double prepare = serviceRates.getReadOnlyTxPrepareS();
      double commit = serviceRates.getReadOnlyTxCommitS();
      double total = local + prepare + commit;
      double readPercentage = 1.0D - params.getWrTxPercentage();
      double lambda = params.getNanoTxLambda() * readPercentage / params.getNumberOfNodes();
      log.trace("initLocalCpuReadOnly: serviceTime " + total);
      OpenClazz clazz = new OpenClazz(LOCAL_READONLY, total, lambda);
      return clazz;
   }


   protected abstract double nanoLambdaRemoteUpdateTx(CpuServiceTimes2 serviceRates, WorkloadParams params);// {

   protected abstract double nanoLambdaLocalUpdateTx(CpuServiceTimes2 serviceRates, WorkloadParams params);// {


   public double getUpdateLocalTxLocalExecutionR() {
      if (inject) {
         System.out.println("CpuModel2: using injected execNocont");
         return scenarioTas2.getWorkParams().getInjectedExecNoContR();
      }
      return this.cpuQueue.getResponseTimeByServiceTime(this.serviceTimes.getUpdateTxLocalExecutionS());
   }

   public double getUpdateLocalTxPrepareR() {

      return this.cpuQueue.getResponseTimeByServiceTime(this.serviceTimes.getUpdateTxPrepareS());
   }

   public double getUpdateLocalTxCommitR() {
      return this.cpuQueue.getResponseTimeByServiceTime(this.serviceTimes.getUpdateTxCommitS());
   }

   public double getUpdateLocalTxLocalLocalRollbackR() {
      return this.cpuQueue.getResponseTimeByServiceTime(this.serviceTimes.getUpdateTxLocalLocalRollbackS());
   }

   public double getUpdateLocalTxLocalRemoteRollbackR() {
      return this.cpuQueue.getResponseTimeByServiceTime(this.serviceTimes.getUpdateTxLocalRemoteRollbackS());
   }

   public double getUpdateRemoteTxExecutionR() {
      return this.cpuQueue.getResponseTimeByServiceTime(this.serviceTimes.getUpdateTxRemoteExecutionS());
   }

   public double getUpdateRemoteTxCommitR() {
      return this.cpuQueue.getResponseTimeByServiceTime(this.serviceTimes.getUpdateTxRemoteCommitS());
   }

   public double getUpdateRemoteTxRollbackR() {
      return this.cpuQueue.getResponseTimeByServiceTime(this.serviceTimes.getUpdateTxRemoteRollbackS());

   }

   public double getReadOnlyTxLocalExecutionR() {
      if (inject) {
         System.out.println("CpuModel2: using injected readOnlyExec");
         return scenarioTas2.getWorkParams().getInjectedReadOnlyR();
      }
      return this.cpuQueue.getResponseTimeByServiceTime(this.serviceTimes.getReadOnlyTxLocalExecutionS());
   }

   public double getReadOnlyTxPrepareR() {
      return this.cpuQueue.getResponseTimeByServiceTime(this.serviceTimes.getReadOnlyTxPrepareS());
   }

   public double getReadOnlyTxCommitR() {
      return this.cpuQueue.getResponseTimeByServiceTime(this.serviceTimes.getReadOnlyTxCommitS());
   }


}
