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
import Tas2.core.Tas2Metrics;
import Tas2.core.Tas2ModelResult;
import Tas2.core.environment.WorkParams;
import Tas2.exception.Tas2Exception;
import Tas2.logicalModel.solver.probabilities.Probabilities;
import Tas2.logicalModel.solver.probabilities.Tas2Probabilities;
import Tas2.physicalModel.PhysicalModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */

//TODO attento: una volta che hai la cpu potresti non mollarla più, i.e., l'hold time dei lock potrebbe non essere inficiato
public class TPCAnalyticalModelNoRetryOnAbort extends AbstractTPCSolver {

   private static final Log log = LogFactory.getLog(TPCAnalyticalModelNoRetryOnAbort.class);

   //TODO: When I enter, I only enter with local, remote, dagger. From them, I populate the probabilities with
   //TODO prepare, commit and coher---> they are as computed from the previous iteration
   //TODO: I use them for EVERY computation of the values. In the end I use the new localAbort for the new Prepare etc
   public ModelResult __solve(PhysicalModel physicalModel, WorkParams workParams, Probabilities probabilities) throws Tas2Exception {
      Tas2ModelResult result = new Tas2ModelResult();
      Tas2Metrics metrics = new Tas2Metrics();


      double avgLocalWrite = this.expectedLocalWritePerTransaction(probabilities, workParams);
      double avgRemoteWrite = this.expectedRemoteWritePerTransaction(probabilities, workParams);
      double localHoldTime = this.expectedLocalHoldTime(probabilities, physicalModel, workParams);
      double remoteHoldTime = this.expectedRemoteHoldTime(probabilities, physicalModel, workParams);
      double newLocalAbortProb = this.expectedLocalAbortProbability(probabilities, workParams, physicalModel, true);
      double newRemoteAbortProb = this.expectedRemoteAbortProbability(probabilities, workParams, physicalModel);
      double newDaggerProb = this.expectedDaggerProbability(probabilities, workParams, physicalModel);
      double avgResponseTime = this.expectedAverageResponseTime(probabilities, workParams, physicalModel);

      double newTasRtt = physicalModel.getPrepareRtt();
      double commitCpuR = physicalModel.getCpuCommitResponseTime();
      double execNoContR = physicalModel.getCpuExecNoContResponseTime();
      double readOnlyR = physicalModel.getCpuReadOnlyResponseTime();

      double suxLocalHoldTime = expectedLocalCommittedHoldTime(probabilities, physicalModel, workParams);
      double suxRemoteHoldTime = remoteCommittedHoldTime(probabilities, physicalModel, workParams);

      double rollbackR = physicalModel.getCpuRollbackResponseTime();
      double replayR = physicalModel.getCpuReplayResponseTime();
      double updateTxR = successfulWriteResponseTime(probabilities, workParams, physicalModel) / probabilities.getCommitProbability();

      //                       Preparing the output

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
      //TODO this is not well done
      metrics.setPrepareCommitResponseTime(updateTxR - newTasRtt - commitCpuR);


      log.trace("AnalModel: avgResponseTime " + avgResponseTime);
      result.setProbabilities(newProbabilities);
      result.setMetrics(metrics);

      //populate
      return result;
   }


   private double expectedAverageResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) throws Tas2Exception {
      //System.out.println("Injecting commit time R");
      double suxWR = successfulWriteResponseTime(probabilities, scenario, physicalModel);// + scenario.getInjectedCommitR();
      double localAbortWR = locallyAbortedWriteResponseTime(probabilities, scenario, physicalModel);
      double remotelyAbortedWR = remotelyAbortedWriteResponseTime(probabilities, scenario, physicalModel);
      double readOnly = readOnlyResponseTime(probabilities, scenario, physicalModel);
      double wrPercentage = scenario.getWritePercentage();

      log.trace("TPCAnalyticalModelNoRetryOnAbort");
      log.trace("suxWr " + suxWR);
      log.trace("localAbortWr " + localAbortWR);
      log.trace("remotelyAbortedWR " + remotelyAbortedWR);
      log.trace("readOnly " + readOnly);

      return wrPercentage * (suxWR + localAbortWR + remotelyAbortedWR) + (1.0D - wrPercentage) * readOnly;
   }


   //TODO double check the times
   protected double txLocalLambda(WorkParams scenario, Probabilities probabilities) {
      return scenario.getLambda() * scenario.getWritePercentage() / scenario.getNumNodes();
   }

   protected double txRemoteLambda(WorkParams scenario, Probabilities probabilities) {
      double nodes = scenario.getNumNodes();
      return probabilities.getPrepareProbability() * txLocalLambda(scenario, probabilities) * (nodes - 1);
   }


}
