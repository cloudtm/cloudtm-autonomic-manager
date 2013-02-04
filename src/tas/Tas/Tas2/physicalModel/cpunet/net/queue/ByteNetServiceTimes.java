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

package Tas2.physicalModel.cpunet.net.queue;

import Tas2.exception.Tas2Exception;
import Tas2.util.Tas2Util;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 24/11/12
 */
public class ByteNetServiceTimes extends NetServiceTimes {

   private double byteCost;

   public ByteNetServiceTimes() {
   }

   public ByteNetServiceTimes(double byteCost) {
      this.byteCost = byteCost;
   }

   public double getByteCost() {
      return this.byteCost;
   }

   @Override
   public void checkCorrectness() throws Tas2Exception {
      Tas2Util.checkPositive(byteCost, true, "ByteCost");
   }
}
