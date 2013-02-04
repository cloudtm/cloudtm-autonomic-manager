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

package Tas2.core;

import Tas2.config.configs.SolverFactoryConfig;
import Tas2.config.configs.Tas2Configuration;
import Tas2.exception.Tas2Exception;
import Tas2.logicalModel.solver.Solver;
import Tas2.logicalModel.solver.TPC.one.TPCClosedSolver;
import Tas2.logicalModel.solver.cubist.CubistClosedSolver;
import Tas2.logicalModel.solver.cubist.MinimalErrorCubistSolver;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 02/12/12
 */
public class SolverFactory {

   public static Solver buildSolver(Tas2Configuration config) throws Tas2Exception {
      SolverFactoryConfig sfc = config.getSolverFactoryConfig();
      if (sfc.getSolverType() == SOLVERS.CUBIST_TAS)
         return new CubistClosedSolver(config);
      if (sfc.getSolverType() == SOLVERS.CUBIST_MIN_ERROR)
         return new MinimalErrorCubistSolver(config);

      return new TPCClosedSolver(config);
      //throw new Tas2Exception("Solver not supported");
   }


   public enum SOLVERS {
      CUBIST_TAS, CUBIST_QUEUE_RTT, FIXED_RTT, CUBIST_MIN_ERROR
   }
}
