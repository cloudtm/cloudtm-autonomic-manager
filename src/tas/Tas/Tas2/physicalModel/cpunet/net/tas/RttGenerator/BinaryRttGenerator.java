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

import Tas2.config.configs.BinaryRttGeneratorConfig;
import Tas2.exception.Tas2Exception;
import Tas2.util.Tas2Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 03/12/12
 */
public class BinaryRttGenerator implements RttGenerator {

   private double minRtt;
   private double maxRtt;
   private double numIt;
   private final static Log log = LogFactory.getLog(BinaryRttGenerator.class);


   public BinaryRttGenerator(BinaryRttGeneratorConfig config) {
      this.minRtt = config.getMinRtt();
      this.maxRtt = config.getMaxRtt();
      numIt = 0;
   }

   public double initRtt() {
      return minRtt;
   }

   public double newRtt(double oldRtt, double outRtt) throws Tas2Exception {

      numIt++;
      //If you are out of the boundaries, return the boundary (or throw an exception, you choose)

      if (outRtt < minRtt || outRtt > maxRtt)
         throw new Tas2Exception("BinarySearch was unlucky after " + numIt + " iterations");

      //if you are in, you can bisect

      if (oldRtt == outRtt) {
         return oldRtt;
      }
      //If you started with l and come up with l'>l, then the new value is l<l''<l'
      else if (oldRtt < outRtt) {

         minRtt = oldRtt;
      }
      //If you started with l and come up with l'<l, then the new value has to be lowered, l'<l''<l
      else {
         maxRtt = oldRtt;
      }
      double retV = (minRtt + maxRtt) * .5D;
      log.debug("oldRtt " + oldRtt + " outRtt " + outRtt + " currentMin " + minRtt + " currentMax " + maxRtt + " retV " + retV);
      return retV;
   }

   public boolean converge(double inRtt, double outRtt, double newRtt, double threshold) {
      return Tas2Util.relErr(inRtt, newRtt) <= threshold; //The rel distance between the input and the output GIVEN BY THE GENERATOR ITSELF
   }
}
