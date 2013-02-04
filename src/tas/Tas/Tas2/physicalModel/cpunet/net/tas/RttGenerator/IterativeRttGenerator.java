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

import Tas2.config.configs.IterativeRttGeneratorConfig;
import Tas2.exception.LastValueException;
import Tas2.util.Tas2Util;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 03/12/12
 */
public class IterativeRttGenerator implements RttGenerator {

   private double minRtt;
   private double maxRtt;
   private double stepRtt;
   private boolean first = true;

   public IterativeRttGenerator(IterativeRttGeneratorConfig config) {
      minRtt = config.getMinRtt();
      maxRtt = config.getMaxRtt();
      stepRtt = config.getStepRtt();
   }

   public double initRtt() {
      return minRtt;
   }

   public double newRtt(double oldRtt, double outRtt) throws LastValueException {
      if (first) {
         first = false;
         return minRtt;
      }
      //if (oldRtt == minRtt && stepRtt != minRtt)
      // return stepRtt;
      double newRtt = oldRtt + stepRtt;
      if (newRtt > maxRtt)
         throw new LastValueException("Last rtt value already reached " + maxRtt);
      return newRtt;
   }

   public boolean converge(double inRtt, double outRtt, double newRtt, double threshold) {
      return Tas2Util.relErr(inRtt, outRtt) <= threshold;
   }
}
