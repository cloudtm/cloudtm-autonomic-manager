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

package Tas2.logicalModel.solver.cubist;


import Tas2.config.configs.*;
import Tas2.core.ModelResult;
import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.exception.LastValueException;
import Tas2.exception.Tas2Exception;
import Tas2.logicalModel.solver.closed.ClosedSolver;
import Tas2.physicalModel.cpunet.net.tas.FixedRttServiceTimes;
import Tas2.physicalModel.cpunet.net.tas.RttGenerator.IterativeRttGenerator;
import Tas2.util.Tas2Util;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 02/12/12
 */
public class MinimalErrorCubistSolver extends CubistClosedSolver {


   private MinimalErrorCubistSolverConfig minimalErrorCubistSolverConfig;


   public MinimalErrorCubistSolver(Configuration conf) throws Tas2Exception {
      super(conf);
      this.minimalErrorCubistSolverConfig = ((Tas2Configuration) conf).getCubistConfig().getMinimalErrorCubistSolverConfig();
   }

   @Override
   protected ModelResult __solve(ClosedSolver closedSolver, DSTMScenarioTas2 scenario, CubistConfig config) throws Tas2Exception {

      double tempError, optimalError = 1e9;
      IterativeRttGeneratorConfig iconfig = config.getIterativeRttGeneratorConfig();
      ModelResult modelResult = null, tempResult;
      boolean convex = this.minimalErrorCubistSolverConfig.isConvex();
      IterativeRttGenerator generator = new IterativeRttGenerator(iconfig);
      double rtt = 0;

      do {

         try {
            rtt = generator.newRtt(rtt, rtt);
         } catch (LastValueException l) {
            log.trace("FinalRtt " + modelResult.getMetrics().getPrepareRtt() + " with an error of " + optimalError);
            return modelResult;
         }

         try {
            //scenario.setNetServiceTimes(new FixedRttServiceTimes(rtt));
            scenario.setNetServiceTimes(new FixedRttServiceTimes(rtt, rtt * rttToCommit));
            tempResult = this._solve(scenario);
            tempError = errorWithRtt(rtt, tempResult, scenario);
         } catch (Tas2Exception t) {
            continue;
         }

         if (tempError < optimalError) {
            optimalError = tempError;
            modelResult = tempResult;
         } else {
            if (convex)
               break;
         }

      }
      //while ((rtt = generator.newRtt(rtt, -1)) <= maxRtt);
      while (true);
      if (rtt == 0)
         throw new Tas2Exception("Cubist always returned Rtt = 0");
      return modelResult;
   }


   private double errorWithRtt(double exRtt, ModelResult result, DSTMScenarioTas2 scenario) throws Tas2Exception {
      double newRtt;
      try {
         newRtt = computeNewRtt(result, scenario);
      } catch (Tas2Exception e) {
         throw new Tas2Exception(e.getMessage());
      }
      return Tas2Util.relErr(exRtt, newRtt);
   }

}
