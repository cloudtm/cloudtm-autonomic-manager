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

import Tas2.config.configs.BinaryLambdaGeneratorConfig;
import Tas2.exception.Tas2Exception;
import Tas2.util.Tas2Util;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 03/12/12
 */
public class BinaryLambdaGenerator implements LambdaGenerator {

   private double minLambda;
   private double maxLambda;
   private static final double SEC_TO_NANO = 1e-9;

   public BinaryLambdaGenerator(BinaryLambdaGeneratorConfig config) {
      this.minLambda = SEC_TO_NANO * config.getMinLambda();
      this.maxLambda = SEC_TO_NANO * config.getMaxLambda();
   }

   public double initLambda() {
      return minLambda;
   }

   //TODO: check sui limiti: se old è minore del minimo o maggiore del massimo va tutto in mona!
   public double getNewLambda(double oldLambda, double outLambda) throws Tas2Exception {

      if (oldLambda < minLambda || oldLambda > maxLambda)
         throw new Tas2Exception("New Lambda out of current boundaries");
      if (oldLambda == outLambda) {
         return oldLambda;
      }
      //If you started with l and come up with l'>l, then the new value is l<l''<l'
      else if (oldLambda < outLambda) {
         minLambda = oldLambda;
      }
      //If you started with l and come up with l'<l, then the new value has to be lowered, l'<l''<l
      else {
         maxLambda = oldLambda;
      }
      return (minLambda + maxLambda) * .5D;
   }


   public boolean converge(double oldLambda, double outLambda, double newLambda, double threshold) {
      return Tas2Util.relErr(oldLambda, newLambda) <= threshold;
   }
}
