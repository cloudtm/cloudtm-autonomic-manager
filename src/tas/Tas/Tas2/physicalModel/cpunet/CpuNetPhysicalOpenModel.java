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

package Tas2.physicalModel.cpunet;

import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.exception.Tas2Exception;
import Tas2.physicalModel.PhysicalModel;
import Tas2.physicalModel.cpunet.cpu.one.CpuModel1;
import Tas2.physicalModel.cpunet.cpu.one.CpuServiceTimes1;
import Tas2.physicalModel.cpunet.net.queue.NetModel;
import Tas2.physicalModel.cpunet.net.queue.NetServiceTimes;
import Tas2.physicalModel.cpunet.parameters.CommonCpuNetPhysicalOpenModel;
import Tas2.physicalModel.cpunet.parameters.ExtendedServiceRates;
import Tas2.physicalModel.cpunet.parameters.WorkloadParams;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 22/11/12
 */

public abstract class CpuNetPhysicalOpenModel extends CommonCpuNetPhysicalOpenModel implements PhysicalModel {


   /*
   The serviceTime may also be fixed. If they are not, each time the model has to be re instantiated
   */
   public CpuNetPhysicalOpenModel(int numCpuCores, ExtendedServiceRates serviceRates, DSTMScenarioTas2 scenario, WorkloadParams params) throws Tas2Exception {
      if (scenario.getWorkParams().isRetryOnAbort())
         throw new RuntimeException("Building a lgecay Cpu Model with retry on abort! Still not supported");
      this.cpuModel = new CpuModel1(numCpuCores, (CpuServiceTimes1) serviceRates.getCpuServiceTimes(), scenario, params);
      this.netModel = buildNetModel(serviceRates.getNetServiceTimes(), scenario, params);
   }


   public double getCpuUtilization() {
      return ((CpuModel1) this.cpuModel).getUtilization();
   }

   public double getNetUtilization() {
      return this.netModel.getUtilization();
   }

   public final double getPrepareRtt() {
      return _getPrepareRtt();
   }

   public double getNetCommitTime() {
      return this.netModel.getNetCommitTime();
   }

   public double getCpuExecNoContResponseTime() {
      return ((CpuModel1) this.cpuModel).getCpuExecNoContResponseTime();
   }

   public double getCpuReplayResponseTime() {
      return ((CpuModel1) this.cpuModel).getCpuReplayResponseTime();
   }

   public double getCpuPrepareResponseTime() {
      return ((CpuModel1) this.cpuModel).getCpuPrepareResponseTime();
   }

   public double getCpuCommitResponseTime() {
      return ((CpuModel1) this.cpuModel).getCpuCommitResponseTime();
   }

   public double getCpuFlushResponseTime() {
      return ((CpuModel1) this.cpuModel).getCpuFlushResponseTime();
   }

   public double getCpuRollbackResponseTime() {
      return ((CpuModel1) this.cpuModel).getCpuRollbackResponseTime();
   }

   public double getCpuReadOnlyResponseTime() {
      return ((CpuModel1) this.cpuModel).getCpuReadOnlyResponseTime();
   }


   protected abstract double _getPrepareRtt();

   protected abstract NetModel buildNetModel(NetServiceTimes netServiceTimes, DSTMScenarioTas2 scenario, WorkloadParams params) throws Tas2Exception;
}
