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

package Tas2.logicalModel.solver.TPC.two;

import Tas2.core.ModelResult;
import Tas2.core.Tas2Metrics;
import Tas2.core.Tas2ModelResult;
import Tas2.core.environment.WorkParams;
import Tas2.exception.Tas2Exception;
import Tas2.logicalModel.solver.probabilities.Probabilities;
import Tas2.logicalModel.solver.probabilities.Tas2Probabilities;
import Tas2.physicalModel.PhysicalModel2;
import Tas2.util.Tas2Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 07/12/12
 */
public abstract class AbstractTPCSolver2 {

   private static final Log log = LogFactory.getLog(AbstractTPCSolver2.class);
   private static final boolean useOneLessThread = true;


   protected abstract double txLocalLambda(WorkParams workParams, Probabilities probabilities);

   protected abstract double txRemoteLambda(WorkParams workParams, Probabilities probabilities);

   protected abstract double updateTransactionResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel) throws Tas2Exception;


   public ModelResult _solve(PhysicalModel2 physicalModel, WorkParams workParams, Probabilities probabilities) throws Tas2Exception {
      this.completeProbabilitiesFromGranules(probabilities, workParams);
      log.trace("Lambda = " + 1e9 * workParams.getLambda());
      log.trace("Effective lambda (with RoA) = " + 1e9 * (this.txLocalLambda(workParams, probabilities) + this.txRemoteLambda(workParams, probabilities)));
      ModelResult result;
      try {
         result = finalizeResult(physicalModel, workParams, probabilities);
      } catch (Tas2Exception tas) {
         log.trace(tas.getMessage());
         throw tas;
      }
      return result;

   }

   private ModelResult finalizeResult(PhysicalModel2 physicalModel, WorkParams workParams, Probabilities probabilities) throws Tas2Exception {
      Tas2Metrics metrics = new Tas2Metrics();
      Tas2ModelResult modelResult = new Tas2ModelResult();
      log.trace("Computing new execution times");
      double avgLocalWrite = this.expectedLocalWritePerTransaction(probabilities, workParams);
      log.trace("LocalWritePerTx " + avgLocalWrite);
      double avgRemoteWrite = this.expectedRemoteWritePerTransaction(probabilities, workParams);
      log.trace("RemoteWritePerTx " + avgLocalWrite);
      double localHoldTime = this.expectedLocalHoldTime(probabilities, physicalModel, workParams);
      log.trace("LocalHoldTime " + localHoldTime);
      double remoteHoldTime = this.expectedRemoteHoldTime(probabilities, physicalModel, workParams);
      log.trace("RemoteHoldTime " + remoteHoldTime);
      double newLocalAbortProb = this.expectedLocalAbortProbability(probabilities, workParams, physicalModel, useOneLessThread);
      double newRemoteAbortProb = this.expectedRemoteAbortProbability(probabilities, workParams, physicalModel);
      double newDaggerProb = this.expectedDaggerProbability(probabilities, workParams, physicalModel);
      double avgResponseTime = this.expectedAverageResponseTime(physicalModel, workParams, probabilities);
      log.trace("AvgResponseTime " + avgResponseTime);
      double newTasRtt = physicalModel.getPrepareRtt();
      log.trace("TasRtt " + newTasRtt);
      double commitTime = physicalModel.getNetCommitTime() + physicalModel.getUpdateLocalTxCommitR();
      log.trace("CommitTime " + commitTime);
      double commitCpuR = physicalModel.getUpdateLocalTxCommitR();
      log.trace("CommitCpuR " + commitCpuR);
      double execNoContR = physicalModel.getUpdateLocalTxLocalExecutionR();
      log.trace("ExecNoContR " + execNoContR);
      double readOnlyR = this.readOnlyResponseTime(probabilities, workParams, physicalModel);
      log.trace("readOnlyR " + readOnlyR);

      double suxLocalHoldTime = expectedLocalCommittedHoldTime(probabilities, physicalModel, workParams);
      log.trace("SuxLocalHoldTime " + suxLocalHoldTime);
      double suxRemoteHoldTime = remoteCommittedHoldTime(probabilities, physicalModel, workParams);
      log.trace("SuxRemoteHoldTime " + suxRemoteHoldTime);

      double rollbackR = -1;//physicalModel.getCpuRollbackResponseTime();
      double replayR = physicalModel.getUpdateRemoteTxExecutionR();
      log.trace("ReplayR " + replayR);
      double updateTxR = updateTransactionCommittedTime(probabilities, workParams, physicalModel) / probabilities.getCommitProbability();
      log.trace("UpdateTxR " + updateTxR);
      //                       Preparing the output

      log.trace("Computing the new set of probabilities");
      Tas2Probabilities newProbabilities = new Tas2Probabilities();

      newProbabilities.setDaggerProbability(newDaggerProb);
      newProbabilities.setLocalAbortProbability(newLocalAbortProb);
      newProbabilities.setRemoteAbortProbability(newRemoteAbortProb);
      double newPrepareProbability = this.prepareProbability(newProbabilities, workParams);
      double newCoherProbability = this.coherProbability(newProbabilities, workParams, workParams.getNumNodes() - 1);
      double newWriteCommitProbability = newPrepareProbability * newCoherProbability;
      double newRemoteCoherProbability = this.coherProbability(newProbabilities, workParams, workParams.getNumNodes() - 2);
      double newOkOneNodeCommitDagger = this.oneNodeOkToCommitDagger(newProbabilities, workParams);
      double newOverallCommitProbability = newWriteCommitProbability * workParams.getWritePercentage() + (1.0D - workParams.getWritePercentage());
      newProbabilities.setPrepareProbability(newPrepareProbability);
      newProbabilities.setCoherProbability(newCoherProbability);
      newProbabilities.setCommitProbability(newWriteCommitProbability);
      newProbabilities.setRemoteCoherProbability(newRemoteCoherProbability);
      newProbabilities.setOkOneNodeCommitDaggerProbability(newOkOneNodeCommitDagger);


      double subIncarnations = 1.0D / newPrepareProbability;
      double incarnations = 1.0D / newCoherProbability;
      double totalIncarnations = subIncarnations * incarnations;

      log.trace("Populating the to-be-returned metrics");
      metrics.setTotalIncarnations(totalIncarnations);
      metrics.setSubReincarnations(subIncarnations);
      metrics.setReincarnations(incarnations);

      metrics.setAvgResponseTime(avgResponseTime);
      metrics.setPrepareRtt(newTasRtt);
      metrics.setLocalHoldTime(localHoldTime);
      metrics.setRemoteHoldTime(remoteHoldTime);
      metrics.setSuxLocalHoldTime(suxLocalHoldTime);
      metrics.setSuxRemoteHoldTime(suxRemoteHoldTime);
      metrics.setCommitCommandR(commitCpuR);
      metrics.setReadOnlyR(readOnlyR);
      metrics.setExecNoContR(execNoContR);
      metrics.setPrepareProbability(newPrepareProbability);
      metrics.setWriteCommitProbability(newWriteCommitProbability);
      metrics.setCommitProbability(newOverallCommitProbability);
      metrics.setCoherProbability(newCoherProbability);
      metrics.setLocalAbortProbability(newLocalAbortProb);
      metrics.setNetUtilization(physicalModel.getNetUtilization());
      metrics.setCpuUtilization(physicalModel.getCpuUtilization());
      metrics.setReplayR(replayR);
      metrics.setCpuRollbackR(rollbackR);
      metrics.setUpdateTxResponseTime(updateTxR);
      metrics.setCommitTime(commitTime);
      //TODO this is not well done
      metrics.setPrepareCommitResponseTime(physicalModel.getNetCommitTime());


      //Tas2Util.debug("ATPCSolver2: avgResponseTime " + avgResponseTime);
      modelResult.setProbabilities(newProbabilities);
      modelResult.setMetrics(metrics);
      log.trace("Successfully returning a modelResult " + modelResult);
      return modelResult;
   }

   private void completeProbabilitiesFromGranules(Probabilities probabilities, WorkParams params) {
      double prepare = prepareProbability(probabilities, params);
      double coher = coherProbability(probabilities, params, params.getNumNodes() - 1);
      double commit = prepare * coher;
      double remoteCoher = coherProbability(probabilities, params, params.getNumNodes() - 2);
      double okOnOneNodeDagger = oneNodeOkToCommitDagger(probabilities, params);

      probabilities.setPrepareProbability(prepare);
      probabilities.setCoherProbability(coher);
      probabilities.setCommitProbability(commit);
      probabilities.setRemoteCoherProbability(remoteCoher);
      probabilities.setOkOneNodeCommitDaggerProbability(okOnOneNodeDagger);
      log.trace(probabilities.toString());

   }

   private double expectedAverageResponseTime(PhysicalModel2 physicalModel, WorkParams workParams, Probabilities probabilities) throws Tas2Exception {
      double updateR = updateTransactionResponseTime(probabilities, workParams, physicalModel);
      double readOnlyR = readOnlyTransactionResponseTime(probabilities, workParams, physicalModel);
      double wrPerc = workParams.getWritePercentage();
      double avgR = wrPerc * updateR + (1.0D - wrPerc) * readOnlyR;
      return avgR;
   }

   private double readOnlyTransactionResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel) {
      double exec = physicalModel.getReadOnlyTxLocalExecutionR();
      double prepare = physicalModel.getReadOnlyTxPrepareR();
      double commit = physicalModel.getReadOnlyTxCommitR();
      return exec + prepare + commit;
   }

   /**
    * RoA:
    * Maybe I could compute it just by averaging over J bernoulli processes with p = p_dagger
    * Otherwise I can impose at least a tx to do Nl writes, and the other ones to behave like a Bernoulli
    * process. But in this case, these processes should be conditioned to the fact that the relevant incarnations
    * are doomed to be remotely aborted, because of a death on this node or another one.
    * I am going with the first option.
    * and the other ones pay  x in [0..Nl]
    * <p/>
    * !RoA: even though from the perspective of the coordinator of a xact, the coherence probability
    * on one node depends only by the conflict with local transactions on this node,
    * a xact can die because of a remote-remote conflict, that here I try to take into account
    *
    * @param probabilities
    * @param scenario
    * @return
    * @throws Tas2.exception.Tas2Exception
    */
   private double expectedRemoteWritePerTransaction(Probabilities probabilities, WorkParams scenario) throws Tas2Exception {
      double Nl = scenario.getWriteOpsPerTx();
      double sum = 0.0D;
      //Posso morire sia perché una locale mi ruba il lock ma anche x colpa di una remota!!
      double granuleAbortProb = probabilities.getDaggerProbability();
      double okProb = probabilities.getOkOneNodeCommitDaggerProbability();

      for (int i = 1; i <= Nl; i++) {
         sum += granuleAbortProb * Math.pow((1.0D - granuleAbortProb), i - 1) * (i - 1);   //numero di scritture che PRENDONO il lock!!!
      }
      sum += okProb * Nl;
      Tas2Util.checkBoundaries(sum, 0, Nl, false, false, "Nr");
      //System.out.println("remoteWrites "+sum);
      return sum;
   }

   private double expectedLocalWritePerTransaction(Probabilities p, WorkParams scenario) throws Tas2Exception {
      double sum = 0.0D;
      double granuleAbortProb = p.getLocalAbortProbability();
      double prepareProb = p.getPrepareProbability();
      int Nl = (int) scenario.getWriteOpsPerTx();

      for (int i = 1; i <= Nl; i++) {
         sum += granuleAbortProb * Math.pow((1.0D - granuleAbortProb), i - 1) * (i - 1);   //numero di scritture che PRENDONO il lock!!!
      }
      sum += prepareProb * Nl;
      //System.out.println("localWrites "+sum);
      Tas2Util.checkBoundaries(sum, 0, Nl, false, false, "Nl");
      return sum;

   }

   /*
   Probabilities
    */

   //NB: i thread in realtà li uso solo x' so che userò solo il closed solver. Altrimenti non dovrei usarli!
   private double expectedLocalAbortProbability(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel, boolean oneLessThread) throws Tas2Exception {

      double threads = scenario.getThreadsPerNode();
      double Nr = expectedRemoteWritePerTransaction(probabilities, scenario);
      double Nl = expectedLocalWritePerTransaction(probabilities, scenario);
      double txLocalLambda = txLocalLambda(scenario, probabilities);
      double txRemoteLambda = txRemoteLambda(scenario, probabilities);
      double lockRemoteLambda = Nr * txRemoteLambda;
      double lockLocalLambda = Nl * txLocalLambda;

      double otherLocalFractio = (threads - (oneLessThread ? 1 : 0)) / threads;
      double otherLocalLambda = lockLocalLambda * otherLocalFractio;
      double localHoldTime = expectedLocalHoldTime(probabilities, physicalModel, scenario);
      double remoteHoldTime = expectedRemoteHoldTime(probabilities, physicalModel, scenario);
      double acf = scenario.getApplicationContentionFactor();
      double ret = acf * (localHoldTime * otherLocalLambda + remoteHoldTime * lockRemoteLambda);
      try {
         Tas2Util.checkProb(ret, "localAbortProbability");
      } catch (Tas2Exception t) {
         log.debug("localAbortProb = " + ret + " localLambda " + lockLocalLambda + " localHoldTime " + localHoldTime + " remoteLambda " + lockRemoteLambda + " remoteHoldTime " + remoteHoldTime);
         throw t;
      }
      return ret;

   }

   private double expectedRemoteAbortProbability(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel) throws Tas2Exception {
      //in remoto posso morire sia per una remota che per una locale; tuttavia
      //se muoio per colpa di una remota, sono anche morto sulla sua incarnazione locale
      //sul nodo d'origine!! Quindi non considero le remote nel computo di questa
      double Nl = expectedLocalWritePerTransaction(probabilities, scenario);

      double txLocalLambda = txLocalLambda(scenario, probabilities);
      double lockLocalLambda = Nl * txLocalLambda;
      double localHoldTime = expectedLocalHoldTime(probabilities, physicalModel, scenario);
      double acf = scenario.getApplicationContentionFactor();
      double ret = lockLocalLambda * acf * localHoldTime;
      Tas2Util.checkProb(ret, "remoteAbortProbability");
      return ret;

   }

   private double expectedDaggerProbability(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel) throws Tas2Exception {
      double Nr = expectedRemoteWritePerTransaction(probabilities, scenario);
      double Nl = expectedLocalWritePerTransaction(probabilities, scenario);
      double txLocalLambda = txLocalLambda(scenario, probabilities);
      double txRemoteLambda = txRemoteLambda(scenario, probabilities);
      double lockRemoteLambda = Nr * txRemoteLambda;
      double lockLocalLambda = Nl * txLocalLambda;
      double nodes = scenario.getNumNodes();


      double otherRemoteFractio = (nodes - 2.0D) / (nodes - 1);
      log.trace("otherRemoteFractio = " + otherRemoteFractio);
      double remoteLambda = lockRemoteLambda * otherRemoteFractio;
      double localHoldTime = expectedLocalHoldTime(probabilities, physicalModel, scenario);
      double remoteHoldTime = expectedRemoteHoldTime(probabilities, physicalModel, scenario);
      double acf = scenario.getApplicationContentionFactor();
      double ret = acf * (lockLocalLambda * localHoldTime + remoteLambda * remoteHoldTime);
      Tas2Util.checkProb(ret, "Pdagger");
      return ret;
   }

   private double prepareProbability(Probabilities p, WorkParams scenario) {

      double lap = p.getLocalAbortProbability();
      double Nl = scenario.getWriteOpsPerTx();
      double prep = Math.pow((1 - lap), Nl);
      log.trace("localAbortProb " + lap + " Nl " + Nl + " prep " + prep);
      return prep;
   }

   private double coherProbability(Probabilities p, WorkParams scenario, double nodes) {

      double rap = p.getRemoteAbortProbability();
      double Nl = scenario.getWriteOpsPerTx();
      double okOneNode = Math.pow((1 - rap), Nl);
      return Math.pow(okOneNode, nodes);

   }

   private double oneNodeOkToCommitDagger(Probabilities p, WorkParams scenario) {
      double rap = p.getDaggerProbability();
      double Nl = scenario.getWriteOpsPerTx();
      return Math.pow((1.0D - rap), Nl);
   }

   /*
     Hold times
     //TODO duble check the times
    */

   protected double expectedLocalHoldTime(Probabilities probabilities, PhysicalModel2 physicalModel, WorkParams scenario) throws Tas2Exception {

      double prepareProb = probabilities.getPrepareProbability();
      double coherProb = probabilities.getCoherProbability();
      double commitProb = prepareProb * coherProb;

      double localAbortHoldTime = expectedLocalLocallyAbortedHoldTime(probabilities, physicalModel, scenario);          //tempo medio di hold per una tx che muore in locale
      log.trace("LocalAbortHoldTime " + localAbortHoldTime);
      double remoteAbortHoldTime = expectedLocalRemotelyAbortedHoldTime(probabilities, physicalModel, scenario);
      log.trace("RemoteAbortHoldTime " + remoteAbortHoldTime);
      double commitHoldTime = expectedLocalCommittedHoldTime(probabilities, physicalModel, scenario);
      log.trace("CommitHoldTime " + commitHoldTime);
      Tas2Util.checkPositive(localAbortHoldTime, false, "localAbortHoldTime");
      Tas2Util.checkPositive(remoteAbortHoldTime, false, "remoteAbortHoldTime");
      Tas2Util.checkPositive(commitHoldTime, false, "commitHoldTime");
      return commitHoldTime * commitProb + remoteAbortHoldTime * prepareProb * (1.0D - coherProb) + localAbortHoldTime;

   }

   protected double expectedLocalCommittedHoldTime(Probabilities probabilities, PhysicalModel2 physicalModel, WorkParams scenario) {
      double commitR = physicalModel.getUpdateLocalTxCommitR();
      double rtt = physicalModel.getPrepareRtt();
      //TODO still missing coordFlush
      double commitHoldTime = commitR + rtt;
      double tNoc = physicalModel.getUpdateLocalTxLocalExecutionR();
      double Nl = scenario.getWriteOpsPerTx();
      double cost = tNoc / Nl;
      double cumulativeHoldTime = 0D;
      for (int i = 1; i <= (int) Nl; i++) {
         cumulativeHoldTime += cost * i;
      }
      cumulativeHoldTime /= Nl;
      commitHoldTime += cumulativeHoldTime;
      return commitHoldTime;
   }

   protected double expectedLocalRemotelyAbortedHoldTime(Probabilities probabilities, PhysicalModel2 physicalModel, WorkParams scenario) throws Tas2Exception {
      double tNoc = physicalModel.getUpdateLocalTxLocalExecutionR();
      double Nl = scenario.getWriteOpsPerTx();
      double cost = tNoc / Nl;

      double localRollback = physicalModel.getUpdateLocalTxLocalRemoteRollbackR();
      double prepareR = physicalModel.getUpdateLocalTxPrepareR();
      //IF at least one can do the replay, I pay also the replay(?)
      double remoteRollback = localRollback + physicalModel.getNetCommitTime() + prepareR;

      double remoteAbortHoldTime = 0;
      for (int i = 1; i <= (int) Nl; i++) {
         remoteAbortHoldTime += cost * i;
      }
      remoteAbortHoldTime = (remoteAbortHoldTime / Nl) + remoteRollback;
      return remoteAbortHoldTime;
   }

   protected double expectedLocalLocallyAbortedHoldTime(Probabilities probabilities, PhysicalModel2 physicalModel, WorkParams scenario) throws Tas2Exception {
      log.trace("Trying to compute lolcalLocallyAbortedHoldTime");
      double Nl = scenario.getWriteOpsPerTx();
      double lab = probabilities.getLocalAbortProbability();
      log.trace("LocalAbortProbability " + lab);
      double partialSum = 0D;

      double localAbortHoldTime = 0;
      double localRollback = physicalModel.getUpdateLocalTxLocalLocalRollbackR();
      log.trace("localRollbackR " + localRollback);
      double tNoc = physicalModel.getUpdateLocalTxLocalExecutionR();
      log.trace("TnoCont " + tNoc);
      double cost = tNoc / Nl;
      double notDeadTilli;
      for (int i = 2; i <= (int) Nl; i++) {
         notDeadTilli = Math.pow((1.0D - lab), (i - 1));
         log.trace("noDeathTill " + i + "th operations " + notDeadTilli);
         Tas2Util.checkProb(notDeadTilli, "NotDeadTilli");
         double ithAbort = lab * notDeadTilli;
         log.trace(i + "th abort " + ithAbort);
         Tas2Util.checkProb(ithAbort, "ithAbort");
         for (int j = 2; j <= i; j++) {
            partialSum += ithAbort * cost * (j - 1);
         }
         localAbortHoldTime += partialSum / (i - 1);
         partialSum = 0.0D;
      }
      localAbortHoldTime += localRollback;
      return localAbortHoldTime;
   }

   protected double expectedRemoteHoldTime(Probabilities probabilities, PhysicalModel2 physicalModel, WorkParams scenario) throws Tas2Exception {

      double remoteCommit = remoteCommittedHoldTime(probabilities, physicalModel, scenario);
      double remoteLocalAbort = remoteAbortedOnTheNodeHoldTime(probabilities, physicalModel, scenario);
      double remoteRemoteAbort = remoteOkOnNodeAbortedRemotelyHoldTime(probabilities, physicalModel, scenario);
      return remoteCommit + remoteLocalAbort + remoteRemoteAbort;
   }

   protected double remoteCommittedHoldTime(Probabilities probabilities, PhysicalModel2 physicalModel, WorkParams scenario) throws Tas2Exception {
      double okToCommit = probabilities.getOkOneNodeCommitDaggerProbability();
      double globalOk = probabilities.getRemoteCoherProbability();
      double prepareNet = physicalModel.getNetPrepareResponseTime();
      double flushCpu = physicalModel.getUpdateRemoteTxCommitR();
      double ret = okToCommit * globalOk * (prepareNet + flushCpu);
      log.trace("CommitedRHT okToCommit " + okToCommit + " globalOk " + globalOk + " prepareNet " + prepareNet + " commitCpu " + flushCpu);
      try {
         Tas2Util.checkPositive(ret, true, "remoteCommittedHoldTime");
      } catch (Tas2Exception t) {
         log.debug("okToCommit " + okToCommit + " globalOk " + globalOk + " prepareNet " + prepareNet + " commitCpu " + flushCpu);
         throw t;
      }
      return ret;
   }

   protected double remoteOkOnNodeAbortedRemotelyHoldTime(Probabilities probabilities, PhysicalModel2 physicalModel, WorkParams scenario) throws Tas2Exception {
      double okToCommit = probabilities.getOkOneNodeCommitDaggerProbability();
      double globalOk = probabilities.getRemoteCoherProbability();
      double prepareNet = physicalModel.getNetPrepareResponseTime();
      double rollbackCpu = physicalModel.getUpdateRemoteTxRollbackR();
      double ret = okToCommit * (1.0D - globalOk) * (prepareNet + rollbackCpu);
      log.trace("RRAHT okToCommit " + okToCommit + " globalOk " + globalOk + " prepareNet " + prepareNet + " rollbackCpu " + rollbackCpu);
      try {
         Tas2Util.checkPositive(ret, globalOk != 1, "remoteOkOnNodeAbortedRemotelyHoldTime");
      } catch (Tas2Exception t) {
         log.debug("okToCommit " + okToCommit + " globalOk " + globalOk + " prepareNet " + prepareNet + " rollbackCpu " + rollbackCpu);
         throw t;
      }
      return ret;
   }

   protected double remoteAbortedOnTheNodeHoldTime(Probabilities probabilities, PhysicalModel2 physicalModel, WorkParams scenario) throws Tas2Exception {
      double okToCommit = probabilities.getOkOneNodeCommitDaggerProbability();
      double rtt = physicalModel.getNetPrepareResponseTime();
      double rollback = physicalModel.getUpdateRemoteTxRollbackR();
      return (1.0D - okToCommit) * (rollback + rtt);
   }

   protected double readOnlyResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel) {
      //double rPer = 1.0D - scenario.getWritePercentage();
      double local = physicalModel.getReadOnlyTxLocalExecutionR();
      double commit = physicalModel.getReadOnlyTxCommitR();
      double prepare = physicalModel.getReadOnlyTxPrepareR();
      double roExec = local + commit + prepare;
      //return rPer * roExec;
      return roExec;
   }

   /*
    Unconditioned response times for update transactions
    */
   protected final double updateTransactionLocallyAbortedTime(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel) {
      double exec = physicalModel.getUpdateLocalTxLocalExecutionR();
      double localRollback = physicalModel.getUpdateLocalTxLocalLocalRollbackR();
      int Nl = (int) scenario.getWriteOpsPerTx();
      double lap = probabilities.getLocalAbortProbability();
      double p_p = probabilities.getPrepareProbability();
      if (p_p == 1)
         return 0D;
      return Tas2Util.meanAbortedExecutionTime(exec, Nl, lap) / (1 - p_p) + localRollback;
   }

   protected final double updateTransactionRemotelyAbortedTime(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel) {
      double exec = physicalModel.getUpdateLocalTxLocalExecutionR();
      double prepare = physicalModel.getUpdateLocalTxPrepareR();
      double remoteRollback = physicalModel.getUpdateLocalTxLocalRemoteRollbackR();
      double rtt = physicalModel.getPrepareRtt();
      return exec + prepare + rtt + remoteRollback;
   }

   protected final double updateTransactionCommittedTime(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel) throws Tas2Exception {
      double exec = physicalModel.getUpdateLocalTxLocalExecutionR();
      double prepare = physicalModel.getUpdateLocalTxPrepareR();
      double rtt = physicalModel.getPrepareRtt();
      double commit = physicalModel.getUpdateLocalTxCommitR();
      double commitN = physicalModel.getNetCommitTime();
      return exec + prepare + rtt + commit + commitN;
   }

}
