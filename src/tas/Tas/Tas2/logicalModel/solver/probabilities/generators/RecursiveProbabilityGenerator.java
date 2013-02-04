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

package Tas2.logicalModel.solver.probabilities.generators;

import Tas2.config.Tas2ConfigurationFactory;
import Tas2.logicalModel.solver.probabilities.Probabilities;
import Tas2.logicalModel.solver.probabilities.Tas2Probabilities;
import Tas2.util.Tas2Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 29/11/12
 */
public class RecursiveProbabilityGenerator implements ProbabilityGenerator {
   private boolean initialConflict;
   private double THRESHOLD = 1e-2;

   private static final Log log = LogFactory.getLog(RecursiveProbabilityGenerator.class);

   public Probabilities generateProbabilities(Probabilities oldProbabilities, Probabilities newProbabilities) {
      if (oldProbabilities == null) {
         if (!initialConflict)
            return new Tas2Probabilities();
         return new Tas2Probabilities(.99D, .99D, .99D);
      }
      log.debug("OldProb " + oldProbabilities + "\nNewProbs " + newProbabilities);
      return newProbabilities;
   }

   public RecursiveProbabilityGenerator() {
      this.initialConflict = Tas2ConfigurationFactory.getConfiguration().getProbabilityFactoryConfig().getInitialConflict();
      this.THRESHOLD = Tas2ConfigurationFactory.getConfiguration().getOpenSolverConfig().getConvergenceThreshold();
   }

   public boolean converge(Probabilities oldProb, Probabilities outProb, Probabilities newProb) {
      double exLocal = oldProb.getLocalAbortProbability();
      double newLocal = outProb.getLocalAbortProbability();
      double exRemote = oldProb.getRemoteAbortProbability();
      double newRemote = outProb.getRemoteAbortProbability();
      return Tas2Util.relErr(exLocal, newLocal) < THRESHOLD && Tas2Util.relErr(exRemote, newRemote) < THRESHOLD;
   }
}
