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

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */

//TODO attento: una volta che hai la cpu potresti non mollarla più, i.e., l'hold time dei lock potrebbe non essere inficiato
public class TPCAnalyticalModelNoRetryOnAbort2 extends AbstractTPCSolver2 {

   private boolean debug = false;

   protected double updateTransactionResponseTime(Probabilities probabilities, WorkParams scenario, PhysicalModel2 physicalModel) throws Tas2Exception {
      double suxWR = updateTransactionCommittedTime(probabilities, scenario, physicalModel);
      double localAbortWR = updateTransactionLocallyAbortedTime(probabilities, scenario, physicalModel);
      double remotelyAbortedWR = updateTransactionRemotelyAbortedTime(probabilities, scenario, physicalModel);
      double p_coher = probabilities.getCoherProbability();
      double p_p = probabilities.getPrepareProbability();
      double p_commit = p_coher * p_p;
      return p_commit * suxWR + (1 - p_p) * localAbortWR + (p_p * (1 - p_coher)) * remotelyAbortedWR;
   }

   protected double txLocalLambda(WorkParams scenario, Probabilities probabilities) {
      return scenario.getLambda() * scenario.getWritePercentage() / scenario.getNumNodes();
   }

   protected double txRemoteLambda(WorkParams scenario, Probabilities probabilities) {
      double nodes = scenario.getNumNodes();
      return probabilities.getPrepareProbability() * txLocalLambda(scenario, probabilities) * (nodes - 1);
   }

}
