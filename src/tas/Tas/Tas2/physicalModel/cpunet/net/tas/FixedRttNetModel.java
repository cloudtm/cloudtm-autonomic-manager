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

import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.physicalModel.cpunet.net.queue.NetModel;
import Tas2.physicalModel.cpunet.net.queue.NetServiceTimes;
import Tas2.physicalModel.cpunet.parameters.WorkloadParams;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 30/11/12
 */
public class FixedRttNetModel implements NetModel {

   protected FixedRttServiceTimes serviceTimes;
   protected DSTMScenarioTas2 scenario;
   protected WorkloadParams params;


   public FixedRttNetModel(NetServiceTimes serviceTimes, DSTMScenarioTas2 scenario, WorkloadParams params) {
      this.serviceTimes = (FixedRttServiceTimes) serviceTimes;
      this.scenario = scenario;
      this.params = params;
   }

   public final double getNetPrepareResponseTime() {
      //Tas2Util.debug(false,"FixedNetRttModel: asking for the netPrepareResponseTime, but I only have the TasRTT!");
      return serviceTimes.getRtt();
   }

   public final double getUtilization() {
      return -1;
   }

   public final double getTasRtt() {
      return serviceTimes.getRtt();
   }


   public final double getNetCommitTime() {
      return serviceTimes.getCommit();
   }


}
