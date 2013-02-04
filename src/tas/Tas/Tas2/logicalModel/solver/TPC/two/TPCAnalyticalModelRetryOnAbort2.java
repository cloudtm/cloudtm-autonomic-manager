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

import Tas2.core.environment.WorkParams;
import Tas2.exception.Tas2Exception;
import Tas2.logicalModel.solver.probabilities.Probabilities;
import Tas2.physicalModel.PhysicalModel2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 10/12/12
 */
public class TPCAnalyticalModelRetryOnAbort2 extends AbstractTPCSolver2 {

   private static final Log log = LogFactory.getLog(TPCAnalyticalModelRetryOnAbort2.class);

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
   protected double updateTransactionResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel) throws Tas2Exception {
      double localAbortedReruns = this.expectedLocallyAbortedTrials(probabilities);  //K * (J-1)
      log.trace("LocalAbortReruns " + localAbortedReruns);
      double localPreparedReruns = this.expectedRemotelyAbortedTrials(probabilities); // (K-1)
      log.trace("LocalPrepareReruns " + localPreparedReruns);
      double localAbortTime = this.updateTransactionLocallyAbortedTime(probabilities, scenario, physicalModel);
      log.trace("LocalAbortTime " + localAbortTime);
      double remoteAbortTime = this.updateTransactionRemotelyAbortedTime(probabilities, scenario, physicalModel);
      log.trace("RemoteAbortTime " + remoteAbortTime);
      double commitTime = this.updateTransactionCommittedTime(probabilities, scenario, physicalModel);
      log.trace("CommittedTime " + commitTime);
      double avg = localAbortedReruns * localAbortTime + localPreparedReruns * remoteAbortTime + commitTime;
      log.trace("Average UpdateTransactionResponseTime " + avg);
      return avg;
   }

   /*
   private double readOnlyTransactionResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel) {
      return 0D;
   }
   */


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
