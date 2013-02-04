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

package Tas2.logicalModel.solver.closed.lambda;

import Tas2.config.configs.ClosedSolverConfig;
import Tas2.exception.Tas2Exception;

import java.util.Arrays;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 03/12/12
 */
public class LambdaGeneratorFactory {

   private LambdaGeneratorFactory() {

   }

   //The config just has the "initial" method. If one fails I have to replace it without changing the initial one
   //For the next iterations, that's why the type is passed as a separate argument

   public static LambdaGenerator buildLambdaGenerator(ClosedSolverConfig config) throws Tas2Exception {
      switch (config.getSolvingMethod()) {
         case PURE_RECURSION:
            return new RecursiveLambdaGenerator(config.getRecursiveLambdaGeneratorConfig());
         case ITERATIVE:
            return new IterativeLambdaGenerator(config.getIterativeLambdaGeneratorConfig());
         case BINARY_SEARCH:
            return new BinaryLambdaGenerator(config.getBinaryLambdaGeneratorConfig());
         default:
            throw new Tas2Exception("Invalid ClosedSolver solving type " + config.getSolvingMethod() + " known are " + Arrays.toString(ClosedSolverConfig.SOLVING_METHOD.values()));
      }
   }

}
