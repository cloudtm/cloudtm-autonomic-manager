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

package Tas2.logicalModel.solver.open;


import Tas2.config.configs.Configuration;
import Tas2.config.configs.OpenSolverConfig;
import Tas2.config.configs.Tas2Configuration;
import Tas2.core.ModelResult;
import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.exception.Tas2Exception;
import Tas2.logicalModel.solver.AbstractSolver;
import Tas2.logicalModel.solver.probabilities.Probabilities;
import Tas2.logicalModel.solver.probabilities.ProbabilityGeneratorFactory;
import Tas2.logicalModel.solver.probabilities.generators.ProbabilityGenerator;
import Tas2.util.Tas2Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */
public abstract class AbstractOpenSolver extends AbstractSolver implements OpenSolver {

   private double THRESHOLD = 1e-2;
   private final static Log log = LogFactory.getLog(AbstractOpenSolver.class);

   protected AbstractOpenSolver(Configuration conf) {
      super(conf);
      OpenSolverConfig oconf = ((Tas2Configuration) conf).getOpenSolverConfig();
      THRESHOLD = oconf.getConvergenceThreshold();
   }

   public final ModelResult solve(DSTMScenarioTas2 scenario) throws Tas2Exception {

      ProbabilityGenerator probabilityGenerator = ProbabilityGeneratorFactory.buildProbabilityGenerator(configuration().getProbabilityFactoryConfig());

      Probabilities exProbabilities = null, tempProbabilities = null, newProbabilities = probabilityGenerator.generateProbabilities(exProbabilities, tempProbabilities);

      ModelResult result;
      int iteration = 0;
      do {

         exProbabilities = newProbabilities;
         result = _solve(scenario, exProbabilities);
         tempProbabilities = result.getProbabilities();
         newProbabilities = probabilityGenerator.generateProbabilities(exProbabilities, tempProbabilities);
         log.trace("End iteration " + iteration + "\nOutProbabilities " + tempProbabilities + " \nNewProbabilities" + newProbabilities.toString());
         iteration++;
      }
      while (!probabilityGenerator.converge(exProbabilities, tempProbabilities, newProbabilities));
      return result;
   }


   private boolean converge(Probabilities oldP, Probabilities newP) {
      double exLocal = oldP.getLocalAbortProbability();
      double newLocal = newP.getLocalAbortProbability();
      if (Tas2Util.relErr(exLocal, newLocal) < THRESHOLD) {
         double exRemote = oldP.getRemoteAbortProbability();
         double newRemote = newP.getRemoteAbortProbability();
         return (Tas2Util.relErr(exRemote, newRemote) < THRESHOLD);
      }
      return false;
   }

   protected abstract ModelResult _solve(DSTMScenarioTas2 scenario, Probabilities probabilities) throws Tas2Exception;


}
