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

package Tas2.physicalModel.cpunet.net.tas;

import Tas2.exception.Tas2Exception;
import Tas2.physicalModel.cpunet.net.queue.NetServiceTimes;
import Tas2.util.Tas2Util;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 30/11/12
 */
public class FixedRttServiceTimes extends NetServiceTimes {

   private double rtt;
   private double commit;

   public FixedRttServiceTimes(double rtt) {
      this.rtt = rtt;
   }

   public FixedRttServiceTimes(double rtt, double comm) {
      this.rtt = rtt;
      this.commit = comm;
   }


   @Override
   public void checkCorrectness() throws Tas2Exception {
      Tas2Util.checkPositive(rtt, true, "TasRttt");
   }

   public double getCommit() {
      return commit;
   }

   public void setCommit(double commit) {
      this.commit = commit;
   }

   public double getRtt() {
      return rtt;
   }

   public void setRtt(double rtt) {
      this.rtt = rtt;
   }

   @Override
   public String toString() {
      return "FixedRttServiceTimes{" +
              "rtt=" + rtt +
              ", commit=" + commit +
              '}';
   }
}
