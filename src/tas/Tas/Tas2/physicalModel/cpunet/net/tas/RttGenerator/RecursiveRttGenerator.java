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

package Tas2.physicalModel.cpunet.net.tas.RttGenerator;

import Tas2.config.configs.RecursiveRttGeneratorConfig;
import Tas2.exception.LastValueException;
import Tas2.util.Tas2Util;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 03/12/12
 */
public class RecursiveRttGenerator implements RttGenerator {
   private double minRtt;
   private double maxIterations;
   private double currentIteration;


   public RecursiveRttGenerator(RecursiveRttGeneratorConfig config) {
      minRtt = config.getMinRtt();
      maxIterations = config.getMaxIterations();
      currentIteration = maxIterations;
   }

   public double newRtt(double in, double out) throws LastValueException {
      if (currentIteration-- == 0)
         throw new LastValueException("Recursion is not converging after " + maxIterations);
      return out;
   }

   public double initRtt() {
      return minRtt;
   }

   public boolean converge(double inRtt, double outRtt, double newRtt, double threshold) {
      return Tas2Util.relErr(inRtt, outRtt) <= threshold;
   }
}
