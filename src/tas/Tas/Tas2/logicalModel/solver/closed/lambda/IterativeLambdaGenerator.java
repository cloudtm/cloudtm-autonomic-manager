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

import Tas2.config.configs.IterativeLambdaGeneratorConfig;
import Tas2.exception.Tas2Exception;
import Tas2.util.Tas2Util;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 03/12/12
 */
public class IterativeLambdaGenerator implements LambdaGenerator {

   private double initLambda;
   private double maxLambda;
   private double stepLambda;
   private static double SEC_TO_NANO = 1e-9;

   public IterativeLambdaGenerator(IterativeLambdaGeneratorConfig config) {
      initLambda = SEC_TO_NANO * config.getInitLambda();
      maxLambda = SEC_TO_NANO * config.getMaxLambda();
      stepLambda = SEC_TO_NANO * config.getStepLambda();
   }

   public double initLambda() {
      return initLambda;
   }

   public double getNewLambda(double oldLambda, double outLambda) throws Tas2Exception {
      if (oldLambda == initLambda)
         return stepLambda;
      double newLambda = oldLambda + stepLambda;
      if (newLambda > maxLambda)
         throw new Tas2Exception("Lambda is too high");
      return newLambda;
   }

   public boolean converge(double oldLambda, double outLambda, double newLambda, double threshold) {
      return Tas2Util.relErr(oldLambda, outLambda) <= threshold;
   }
}
