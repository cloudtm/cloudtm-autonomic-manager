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

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 06/12/12
 */

/**
 * An Incarnation is either the first time a transaction is born
 * or is every re-birth of a transaction after that it gets to the prepare phase
 * Born-->local Abort--->localAbort--->prepare+remoteAbort--->localAbort--->prepare+remoteAbort--->prepare+commit
 * is a transaction with 1 + 1 +1 = 3 incarnations
 */


/**
 * This class can be used only if we want to model an actual open system in which new transactions
 * join the incarnations of transactions that were not able yet to leave the system
 */
public class TPCAnalyticalRetryOnAbort extends AbstractTPCSolver {


   protected ModelResult __solve(PhysicalModel physicalModel, WorkParams workParams, Probabilities probabilities) throws Tas2Exception {
      return null;
   }

   /**
    * Number of times that a transaction DIES remotely
    *
    * @param prob
    * @return
    */
   private double expectedRemotelyAbortedTrials(Probabilities prob) {
      return expectedIncarnations(prob) - 1D;
   }


   private double expectedIncarnations(Probabilities prob) {
      double coher = prob.getCoherProbability();
      return 1.0D / coher;
   }

   /**
    * Number of times a transaction dies locally
    *
    * @param prob
    * @return
    */
   private double expectedLocallyAbortedTrials(Probabilities prob) {
      double numIncarnationsThatGetToPrepare = expectedIncarnations(prob);
      double perIncarnationLocalRetries = this.perIncarnationLocalRetries(prob);
      return perIncarnationLocalRetries * numIncarnationsThatGetToPrepare;
   }

   /**
    * Number of times a transaction dies locally per incarnation
    *
    * @param prob
    * @return
    */
   private double perIncarnationLocalRetries(Probabilities prob) {
      return expectedSubIncarnations(prob) - 1;
   }


   private double expectedSubIncarnations(Probabilities prob) {
      double prep = prob.getPrepareProbability();
      return (1.0D / prep);
   }


   /**
    * A transaction has K incarnations; K-1 dies; 1 commits
    * Each of the K-1 has J sub-incarnation: J-1 dies, 1 succeeds
    * The K-th incarnation has J-1 local deaths too, so
    * So we pay
    * K times the total local time
    * K * (J-1) time the abortLocalTime
    * K-1 times the abortRemote time
    * 1 time the commitRemote time
    *
    * @param probabilities
    * @param scenario
    * @param physicalModel
    * @return
    */
   private double updateTransactionResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) {
      double localAbortedReruns = this.expectedLocallyAbortedTrials(probabilities);  //K * (J-1)
      double localPreparedReruns = this.expectedRemotelyAbortedTrials(probabilities); // (K-1)
      double localAbortTime = this.updateTransactionLocallyAbortedTime(probabilities, scenario, physicalModel);
      double remoteAbortTime = this.updateTransactionRemotelyAbortedTime(probabilities, scenario, physicalModel);
      double commitTime = this.updateTransactionCommittedTime(probabilities, scenario, physicalModel);

      return localAbortedReruns * localAbortTime + localPreparedReruns * remoteAbortTime + commitTime;
   }

   private double readOnlyTransactionResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) {
      return 0D;
   }

   private double updateTransactionLocallyAbortedTime(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) {
      return 0;
   }

   private double updateTransactionRemotelyAbortedTime(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) {
      return 0;
   }

   private double updateTransactionCommittedTime(Probabilities probabilities, WorkParams scenario, PhysicalModel physicalModel) {
      return 0;
   }

   protected double txLocalLambda(WorkParams scenario, Probabilities probabilities) {
      double subIncarnations = this.expectedSubIncarnations(probabilities);
      double incarnations = this.expectedIncarnations(probabilities);
      double numNodes = scenario.getNumNodes();
      double wrPercentage = scenario.getWritePercentage();
      double localLambda = scenario.getLambda() * wrPercentage / numNodes;
      localLambda *= subIncarnations * incarnations;
      return localLambda;
   }

   protected double txRemoteLambda(WorkParams scenario, Probabilities probabilities) {
      double incarnations = this.expectedIncarnations(probabilities);
      double numNodes = scenario.getNumNodes();
      double wrPercentage = scenario.getWritePercentage();
      double remoteLambda = scenario.getLambda() * wrPercentage * (numNodes - 1) / numNodes;
      remoteLambda *= incarnations;
      return remoteLambda;
   }


}
