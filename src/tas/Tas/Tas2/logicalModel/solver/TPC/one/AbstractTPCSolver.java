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

package Tas2.logicalModel.solver.TPC.one;

import Tas2.core.ModelResult;
import Tas2.core.environment.WorkParams;
import Tas2.exception.Tas2Exception;
import Tas2.logicalModel.solver.probabilities.Probabilities;
import Tas2.physicalModel.PhysicalModel;
import Tas2.util.Tas2Util;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 07/12/12
 */
public abstract class AbstractTPCSolver {


   protected abstract ModelResult __solve(PhysicalModel physicalModel, WorkParams workParams, Probabilities probabilities) throws Tas2Exception;

   protected abstract double txLocalLambda(WorkParams workParams, Probabilities probabilities);

   protected abstract double txRemoteLambda(WorkParams workParams, Probabilities probabilities);

   public ModelResult _solve(PhysicalModel physicalModel, WorkParams workParams, Probabilities probabilities) throws Tas2Exception {
      this.completeProbabilitiesFromGranules(probabilities, workParams);
      return __solve(physicalModel, workParams, probabilities);
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
   protected double expectedRemoteWritePerTransaction(Probabilities probabilities, WorkParams scenario) throws Tas2Exception {
      double Nl = scenario.getWriteOpsPerTx();
      double sum = 0.0D;
      //Posso morire sia perché una locale mi ruba il lock ma anche x colpa di una remota!!
      double granuleAbortProb = probabilities.getDaggerProbability();
      double okProb = probabilities.getOkOneNodeCommitDaggerProbability();

      if (okProb == 1)
         return Nl; //This is to avoid rounding problems

      for (int i = 1; i <= Nl; i++) {
         sum += granuleAbortProb * Math.pow((1.0D - granuleAbortProb), i - 1) * (i - 1);   //numero di scritture che PRENDONO il lock!!!
      }
      sum += okProb * Nl;
      try {
         Tas2Util.checkBoundaries(sum, 0, Nl, false, false, "Nr");
      } catch (Tas2Exception t) {
         throw t;
      }
      //System.out.println("remoteWrites "+sum);
      return sum;
   }

   protected double expectedLocalWritePerTransaction(Probabilities p, WorkParams scenario) throws Tas2Exception {
      double sum = 0.0D;
      double granuleAbortProb = p.getLocalAbortProbability();
      double prepareProb = p.getPrepareProbability();
      int Nl = (int) scenario.getWriteOpsPerTx();

      if (prepareProb == 1)
         return Nl; //This is to avoid rounding problems

      for (int i = 1; i <= Nl; i++) {
         sum += granuleAbortProb * Math.pow((1.0D - granuleAbortProb), i - 1) * (i - 1);   //numero di scritture che PRENDONO il lock!!!
      }
      sum += prepareProb * Nl;
      //System.out.println("localWrites "+sum);
      try {
         Tas2Util.checkBoundaries(sum, 0, Nl, false, false, "Nl");
      } catch (Tas2Exception t) {
         throw t;
      }
      return sum;

   }


   protected void completeProbabilitiesFromGranules(Probabilities probabilities, WorkParams params) throws Tas2Exception {
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

   }

   //NB: i thread in realtà li uso solo x' so che userò solo il closed solver. Altrimenti non dovrei usarli!
   protected double expectedLocalAbortProbability(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel, boolean oneLessThread) throws Tas2Exception {

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
         Tas2Util.debug(true, "localAbortProb = " + ret + " localLambda " + lockLocalLambda + " localHoldTime " + localHoldTime + " remoteLambda " + lockRemoteLambda + " remoteHoldTime " + remoteHoldTime);
      }
      return ret;

   }

   protected double expectedRemoteAbortProbability(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) throws Tas2Exception {
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

   protected double expectedDaggerProbability(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) throws Tas2Exception {
      double Nr = expectedRemoteWritePerTransaction(probabilities, scenario);
      double Nl = expectedLocalWritePerTransaction(probabilities, scenario);
      double txLocalLambda = txLocalLambda(scenario, probabilities);
      double txRemoteLambda = txRemoteLambda(scenario, probabilities);
      double lockRemoteLambda = Nr * txRemoteLambda;
      double lockLocalLambda = Nl * txLocalLambda;
      double nodes = scenario.getNumNodes();


      double otherRemoteFractio = (nodes - 2.0D) / (nodes - 1);
      Tas2Util.debug(false, "otherRemoteFractio = " + otherRemoteFractio);
      double remoteLambda = lockRemoteLambda * otherRemoteFractio;
      double localHoldTime = expectedLocalHoldTime(probabilities, physicalModel, scenario);
      double remoteHoldTime = expectedRemoteHoldTime(probabilities, physicalModel, scenario);
      double acf = scenario.getApplicationContentionFactor();
      double ret = acf * (lockLocalLambda * localHoldTime + remoteLambda * remoteHoldTime);
      Tas2Util.checkProb(ret, "Pdagger");
      return ret;
   }

   protected double prepareProbability(Probabilities p, WorkParams scenario) throws Tas2Exception {

      double lap = p.getLocalAbortProbability();
      double Nl = scenario.getWriteOpsPerTx();
      double prep = Math.pow((1 - lap), Nl);
      Tas2Util.checkProb(lap, "localAbortProb");
      Tas2Util.debug(false, "localAbortProb " + lap + " Nl " + Nl + " prep " + prep);
      return prep;
   }

   protected double coherProbability(Probabilities p, WorkParams scenario, double nodes) throws Tas2Exception {

      double rap = p.getRemoteAbortProbability();
      double Nl = scenario.getWriteOpsPerTx();
      double okOneNode = Math.pow((1 - rap), Nl);
      Tas2Util.checkProb(okOneNode, "OkOnOneNode(Coher)");
      double coher = Math.pow(okOneNode, nodes);
      Tas2Util.checkProb(coher, "CoherProb");
      return coher;

   }

   protected double oneNodeOkToCommitDagger(Probabilities p, WorkParams scenario) throws Tas2Exception {
      double rap = p.getDaggerProbability();
      double Nl = scenario.getWriteOpsPerTx();
      double daggerOkOnOneNode = Math.pow((1.0D - rap), Nl);
      Tas2Util.checkProb(daggerOkOnOneNode, "DaggerOkOnOneNode");
      return daggerOkOnOneNode;
   }

   protected double expectedLocalCommittedHoldTime(Probabilities probabilities, PhysicalModel physicalModel, WorkParams scenario) {
      double commitR = physicalModel.getCpuCommitResponseTime();
      double prepareR = physicalModel.getCpuPrepareResponseTime();
      double replayR = physicalModel.getCpuReplayResponseTime();
      double prepareNetR = physicalModel.getNetPrepareResponseTime();
      //TODO still missing coordFlush
      double commitHoldTime = commitR + prepareNetR + prepareR + replayR;
      double tNoc = physicalModel.getCpuExecNoContResponseTime();
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

   protected double expectedLocalRemotelyAbortedHoldTime(Probabilities probabilities, PhysicalModel physicalModel, WorkParams scenario) {
      double tNoc = physicalModel.getCpuExecNoContResponseTime();
      double Nl = scenario.getWriteOpsPerTx();
      double localRollback = physicalModel.getCpuRollbackResponseTime();
      double prepareR = physicalModel.getCpuPrepareResponseTime();
      //IF at least one can do the replay, I pay also the replay(?)
      double replayR = physicalModel.getCpuReplayResponseTime();
      double remoteRollback = localRollback + physicalModel.getNetCommitTime() + prepareR;
      double cost = tNoc / Nl;
      double remoteAbortHoldTime = 0;
      for (int i = 1; i <= (int) Nl; i++) {
         remoteAbortHoldTime += cost * i;
      }
      remoteAbortHoldTime = (remoteAbortHoldTime / Nl) + remoteRollback;
      return remoteAbortHoldTime;
   }

   protected double expectedLocalLocallyAbortedHoldTime(Probabilities probabilities, PhysicalModel physicalModel, WorkParams scenario) throws Tas2Exception {
      double Nl = scenario.getWriteOpsPerTx();
      double lab = probabilities.getLocalAbortProbability();
      double partialSum = 0D;

      double localAbortHoldTime = 0;
      double localRollback = physicalModel.getCpuRollbackResponseTime();
      double tNoc = physicalModel.getCpuExecNoContResponseTime();
      double cost = tNoc / Nl;
      double notDeadTilli;
      for (int i = 2; i <= (int) Nl; i++) {
         notDeadTilli = Math.pow((1.0D - lab), (i - 1));
         Tas2Util.checkProb(notDeadTilli, "NotDeadTilli");
         double ithAbort = lab * notDeadTilli;
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


   protected double expectedRemoteHoldTime(Probabilities probabilities, PhysicalModel physicalModel, WorkParams scenario) throws Tas2Exception {

      double remoteCommit = remoteCommittedHoldTime(probabilities, physicalModel, scenario);
      double remoteLocalAbort = remoteAbortedOnTheNodeHoldTime(probabilities, physicalModel, scenario);
      double remoteRemoteAbort = remoteOkOnNodeAbortedRemotelyHoldTime(probabilities, physicalModel, scenario);
      return remoteCommit + remoteLocalAbort + remoteRemoteAbort;
   }

   protected double remoteCommittedHoldTime(Probabilities probabilities, PhysicalModel physicalModel, WorkParams scenario) throws Tas2Exception {
      double okToCommit = probabilities.getOkOneNodeCommitDaggerProbability();
      double globalOk = probabilities.getRemoteCoherProbability();
      double prepareNet = physicalModel.getNetPrepareResponseTime();
      double flushCpu = physicalModel.getCpuFlushResponseTime();
      double ret = okToCommit * globalOk * (prepareNet + flushCpu);
      Tas2Util.debug(false, "CommitedRHT okToCommit " + okToCommit + " globalOk " + globalOk + " prepareNet " + prepareNet + " commitCpu " + flushCpu);
      try {
         Tas2Util.checkPositive(ret, true, "remoteCommittedHoldTime");
      } catch (Tas2Exception t) {
         throw t;
      }
      return ret;
   }

   protected double remoteOkOnNodeAbortedRemotelyHoldTime(Probabilities probabilities, PhysicalModel physicalModel, WorkParams scenario) throws Tas2Exception {
      double okToCommit = probabilities.getOkOneNodeCommitDaggerProbability();
      double globalOk = probabilities.getRemoteCoherProbability();
      double prepareNet = physicalModel.getNetPrepareResponseTime();
      double rollbackCpu = physicalModel.getCpuRollbackResponseTime();
      double ret = okToCommit * (1.0D - globalOk) * (prepareNet + rollbackCpu);
      Tas2Util.debug(false, "RRAHT okToCommit " + okToCommit + " globalOk " + globalOk + " prepareNet " + prepareNet + " rollbackCpu " + rollbackCpu);
      try {
         Tas2Util.checkPositive(ret, globalOk != 1, "remoteOkOnNodeAbortedRemotelyHoldTime");
      } catch (Tas2Exception t) {
         Tas2Util.debug(true, "okToCommit " + okToCommit + " globalOk " + globalOk + " prepareNet " + prepareNet + " rollbackCpu " + rollbackCpu);
         throw t;
      }
      return ret;
   }

   protected double remoteAbortedOnTheNodeHoldTime(Probabilities probabilities, PhysicalModel physicalModel, WorkParams scenario) throws Tas2Exception {
      double okToCommit = probabilities.getOkOneNodeCommitDaggerProbability();
      double rtt = physicalModel.getNetPrepareResponseTime();
      double rollback = physicalModel.getCpuRollbackResponseTime();
      return (1.0D - okToCommit) * (rollback + rtt);
   }

   protected double readOnlyResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) {
      //double rPer = 1.0D - scenario.getWritePercentage();
      double roExec = physicalModel.getCpuReadOnlyResponseTime();
      //return rPer * roExec;
      return roExec;
   }

   protected double successfulWriteResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) {
      double execNoCont = physicalModel.getCpuExecNoContResponseTime();
      double prepareR = physicalModel.getCpuPrepareResponseTime();
      double commitR = physicalModel.getCpuCommitResponseTime();
      double flushR = physicalModel.getCpuFlushResponseTime();
      double replayR = physicalModel.getCpuReplayResponseTime();
      double rtt = physicalModel.getNetPrepareResponseTime();
      double commitN = physicalModel.getNetCommitTime();
      double pCommit = probabilities.getPrepareProbability() * probabilities.getCoherProbability();
      Tas2Util.debug("execNoContR " + execNoCont + " prepareR " + prepareR + " commitR " + commitR + " flushR " + flushR + " replayR " + replayR + " rtt " + rtt + " pCommit " + pCommit);
      return pCommit * (execNoCont + prepareR + commitR + flushR + rtt + commitN + replayR);
   }

   protected double locallyAbortedWriteResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) throws Tas2Exception {


      double granuleAbortProb = probabilities.getLocalAbortProbability();
      if (granuleAbortProb == 0)
         return 0;
      double sum = 0.0D;
      double numOps = scenario.getWriteOpsPerTx();
      double tNoc = physicalModel.getCpuExecNoContResponseTime();
      double quantum = tNoc / numOps;
      double payedCost = 0.0D;
      double ithConditionedDeath = 0.0D;
      Tas2Util.checkProb(granuleAbortProb, " granuleAbortProb");
      double notDeadTilli;
      for (int i = 1; i <= (int) numOps; ++i) {
         payedCost = quantum * i;
         notDeadTilli = Math.pow((1.0D - granuleAbortProb), i - 1);
         ithConditionedDeath = numOps == 1 ? 1D : granuleAbortProb * notDeadTilli;
         sum += payedCost * ithConditionedDeath;

         Tas2Util.checkProb(ithConditionedDeath, "I-th conditioned Death");
         Tas2Util.checkProb(notDeadTilli, "not dead till i ");
      }
      Tas2Util.checkGreaterThan(sum, 0, false, "localLocalAbortTime");
      return sum;
   }

   protected double remotelyAbortedWriteResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) {
      double execNoCont = physicalModel.getCpuExecNoContResponseTime();
      double prepareTime = physicalModel.getCpuPrepareResponseTime();
      double rtt = physicalModel.getNetPrepareResponseTime();
      double rollback = physicalModel.getCpuRollbackResponseTime();
      double rollbackN = physicalModel.getNetCommitTime();
      double prob = probabilities.getPrepareProbability() * (1.0D - probabilities.getCoherProbability());
      return prob * (execNoCont + prepareTime + rollback + rollbackN + rtt);
   }

   //TODO duble check the times
   protected double expectedLocalHoldTime(Probabilities probabilities, PhysicalModel physicalModel, WorkParams scenario) throws Tas2Exception {

      double prepareProb = probabilities.getPrepareProbability();
      double coherProb = probabilities.getCoherProbability();
      double commitProb = prepareProb * coherProb;

      double localAbortHoldTime = expectedLocalLocallyAbortedHoldTime(probabilities, physicalModel, scenario);          //tempo medio di hold per una tx che muore in locale
      double remoteAbortHoldTime = expectedLocalRemotelyAbortedHoldTime(probabilities, physicalModel, scenario);
      double commitHoldTime = expectedLocalCommittedHoldTime(probabilities, physicalModel, scenario);
      Tas2Util.checkPositive(localAbortHoldTime, true, "localAbortHoldTime");
      Tas2Util.checkPositive(remoteAbortHoldTime, true, "remoteAbortHoldTime");
      Tas2Util.checkPositive(commitHoldTime, true, "commitHoldTime");
      return commitHoldTime * commitProb + remoteAbortHoldTime * prepareProb * (1.0D - coherProb) + localAbortHoldTime;

   }


}
