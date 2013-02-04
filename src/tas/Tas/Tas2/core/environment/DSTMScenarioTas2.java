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

/**
 * Created by IntelliJ IDEA.
 * User: diego
 * Date: 09/08/11
 * Time: 15:41
 * To change this template use File | Settings | File Templates.
 */
package Tas2.core.environment;

import Tas2.exception.Tas2Exception;
import Tas2.physicalModel.cpunet.cpu.CpuServiceTimes;
import Tas2.physicalModel.cpunet.net.queue.NetServiceTimes;

public class DSTMScenarioTas2 {

   private CpuServiceTimes cpuServiceTimes;
   private NetServiceTimes netServiceTimes;
   private WorkParams workParams;

   public DSTMScenarioTas2(CpuServiceTimes cpuServiceTimes, NetServiceTimes netServiceTimes, WorkParams workParams) throws Tas2Exception {
      this.cpuServiceTimes = cpuServiceTimes;
      this.netServiceTimes = netServiceTimes;
      this.workParams = workParams;
      this.checkCorrectness();
   }

   private void checkCorrectness() throws Tas2Exception {
      this.workParams.checkCorrectness();
      this.netServiceTimes.checkCorrectness();
      this.cpuServiceTimes.checkCorrectness();
   }

   public WorkParams getWorkParams() {
      return workParams;
   }

   public void setWorkParams(WorkParams workParams) {
      this.workParams = workParams;
   }

   public CpuServiceTimes getCpuServiceTimes() {
      return cpuServiceTimes;
   }

   public void setCpuServiceTimes(CpuServiceTimes cpuServiceTimes) {
      this.cpuServiceTimes = cpuServiceTimes;
   }

   public NetServiceTimes getNetServiceTimes() {
      return netServiceTimes;
   }

   public void setNetServiceTimes(NetServiceTimes netServiceTimes) {
      this.netServiceTimes = netServiceTimes;
   }

   @Override
   public String toString() {
      return "DSTMScenarioTas2{" +
              "cpuServiceTimes=" + cpuServiceTimes +
              ", netServiceTimes=" + netServiceTimes +
              ", workParams=" + workParams +
              '}';
   }
}
