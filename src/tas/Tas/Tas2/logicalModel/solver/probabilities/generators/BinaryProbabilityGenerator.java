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
public class BinaryProbabilityGenerator implements ProbabilityGenerator {

   private boolean initialConflict = false;
   private double THRESHOLD = 1e-2;


   private Probabilities maxProbs = new Tas2Probabilities(1.0D, 1.0D, 1.0D);
   private Probabilities minProbs = new Tas2Probabilities(0.0D, 0.0D, 0.0D);

   private static final Log log = LogFactory.getLog(BinaryProbabilityGenerator.class);

   public BinaryProbabilityGenerator() {
      this.initialConflict = Tas2ConfigurationFactory.getConfiguration().getProbabilityFactoryConfig().getInitialConflict();
      this.THRESHOLD = Tas2ConfigurationFactory.getConfiguration().getOpenSolverConfig().getConvergenceThreshold();
   }

   public Probabilities generateProbabilities(Probabilities oldProbabilities, Probabilities newProbabilities) {
      if (oldProbabilities == null)
         return buildInitialProbs();
      return this.updateStateAndBuildBinaryProbability(oldProbabilities, newProbabilities);
   }


   private Probabilities buildInitialProbs() {
      if (!initialConflict)
         return new Tas2Probabilities();
      return new Tas2Probabilities(.99D, .99D, .99D);
   }

   private Probabilities updateStateAndBuildBinaryProbability(Probabilities oldP, Probabilities newP) {
      log.debug("in " + oldP.toString());
      log.debug("max " + maxProbs.toString());
      log.debug("out " + newP.toString());

      double oldLocalAbort = oldP.getLocalAbortProbability();
      double oldRemoteAbort = oldP.getRemoteAbortProbability();
      double oldDagger = oldP.getDaggerProbability();

      double currentLocalAbortProb = newP.getLocalAbortProbability();
      double currentRemoteAbortProb = newP.getRemoteAbortProbability();
      double currentDagger = newP.getDaggerProbability();

      double minLocalAbortProb = minProbs.getLocalAbortProbability();
      double minRemoteAbortProb = minProbs.getRemoteAbortProbability();
      double minDagger = minProbs.getDaggerProbability();

      double maxLocalAbortProb = maxProbs.getLocalAbortProbability();
      double maxRemoteAbortProb = maxProbs.getRemoteAbortProbability();
      double maxDagger = maxProbs.getDaggerProbability();

      double newLocalAbort = 0;
      double newRemoteAbort = 0;
      double newDagger = 0;

      //If nothing has changed, do no change
      if (oldLocalAbort == currentLocalAbortProb) {
         newLocalAbort = currentLocalAbortProb;
      }
      //If you started with l and come up with l'>l, then the new value is l<l''<l'
      else if (oldLocalAbort < currentLocalAbortProb) {
         minLocalAbortProb = oldLocalAbort;
         newLocalAbort = (minLocalAbortProb + maxLocalAbortProb) * .5D;
      }
      //If you started with l and come up with l'<l, then the new value has to be lowered, l'<l''<l
      else {
         maxLocalAbortProb = oldLocalAbort;
         newLocalAbort = (minLocalAbortProb + maxLocalAbortProb) * .5D;
      }


      //If nothing has changed, do no change
      if (oldRemoteAbort == currentRemoteAbortProb) {
         newRemoteAbort = currentRemoteAbortProb;
      }
      //If you started with l and come up with l'>l, then the new value is l<l''<l'
      else if (oldRemoteAbort < currentRemoteAbortProb) {
         minRemoteAbortProb = oldRemoteAbort;
         newRemoteAbort = (minRemoteAbortProb + maxRemoteAbortProb) * .5D;
      }
      //If you started with l and come up with l'<l, then the new value has to be lowered, l'<l''<l
      else {
         maxRemoteAbortProb = oldRemoteAbort;
         newRemoteAbort = (minRemoteAbortProb + maxRemoteAbortProb) * .5D;
      }

      if (oldDagger == currentDagger) {
         newDagger = currentDagger;
      } else if (oldDagger < currentDagger) {
         minDagger = oldDagger;
         newDagger = (minDagger + maxDagger) * .5D;
      } else {
         maxDagger = oldDagger;
         newDagger = (minDagger + maxDagger) * .5D;
      }


      minProbs.setLocalAbortProbability(minLocalAbortProb);
      minProbs.setRemoteAbortProbability(minRemoteAbortProb);
      maxProbs.setRemoteAbortProbability(maxRemoteAbortProb);
      maxProbs.setLocalAbortProbability(maxLocalAbortProb);
      minProbs.setDaggerProbability(minDagger);
      maxProbs.setDaggerProbability(maxDagger);

      Probabilities ret = new Tas2Probabilities(newLocalAbort, newRemoteAbort, newDagger);
      log.debug("new " + ret.toString());
      return ret;
   }

   public boolean converge(Probabilities oldProb, Probabilities outProb, Probabilities newProb) {
      double exLocal = oldProb.getLocalAbortProbability();
      double newLocal = newProb.getLocalAbortProbability();
      double exRemote = oldProb.getRemoteAbortProbability();
      double newRemote = newProb.getRemoteAbortProbability();
      return Tas2Util.relErr(exLocal, newLocal) < THRESHOLD && Tas2Util.relErr(exRemote, newRemote) < THRESHOLD;
   }
}
