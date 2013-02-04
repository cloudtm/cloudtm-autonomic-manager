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
import Tas2.physicalModel.PhysicalModel2;
import Tas2.physicalModel.cpunet.cpu.two.CpuModel2;
import Tas2.physicalModel.cpunet.cpu.two.CpuModel2NotRoA;
import Tas2.physicalModel.cpunet.cpu.two.CpuModel2RoA;
import Tas2.physicalModel.cpunet.cpu.two.CpuServiceTimes2;
import Tas2.physicalModel.cpunet.net.tas.FixedRttNetModel;
import Tas2.physicalModel.cpunet.parameters.CommonCpuNetPhysicalOpenModel;
import Tas2.physicalModel.cpunet.parameters.ExtendedServiceRates;
import Tas2.physicalModel.cpunet.parameters.WorkloadParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 07/12/12
 */
//TODO there is *no* version with the M/M/1 queue for the network. Just the one with Cubist
public class CpuNetPhysicalOpenModel2 extends CommonCpuNetPhysicalOpenModel implements PhysicalModel2 {

   private static final Log log = LogFactory.getLog(CpuNetPhysicalOpenModel2.class);


   public CpuNetPhysicalOpenModel2(int numCpuCores, ExtendedServiceRates serviceRates, DSTMScenarioTas2 scenario, WorkloadParams params) throws Tas2Exception {

      this.cpuModel = buildCpuModels(numCpuCores, serviceRates, scenario, params);
      log.trace("CpuModel has been successfully created!");
      this.netModel = new FixedRttNetModel(serviceRates.getNetServiceTimes(), scenario, params);
      log.trace("NetModel has been successfully create!");
   }


   private CpuModel2 buildCpuModels(int numCpuCores, ExtendedServiceRates serviceRates, DSTMScenarioTas2 scenario, WorkloadParams params) throws Tas2Exception {
      if (!scenario.getWorkParams().isRetryOnAbort())
         return new CpuModel2NotRoA(numCpuCores, (CpuServiceTimes2) serviceRates.getCpuServiceTimes(), scenario, params);
      return new CpuModel2RoA(numCpuCores, (CpuServiceTimes2) serviceRates.getCpuServiceTimes(), scenario, params);
   }


   public double getUpdateLocalTxLocalExecutionR() {
      return ((CpuModel2) this.cpuModel).getUpdateLocalTxLocalExecutionR();
   }

   public double getUpdateLocalTxPrepareR() {
      return ((CpuModel2) this.cpuModel).getUpdateLocalTxPrepareR();
   }

   public double getUpdateLocalTxCommitR() {
      return ((CpuModel2) this.cpuModel).getUpdateLocalTxCommitR();
   }

   public double getUpdateLocalTxLocalLocalRollbackR() {
      return ((CpuModel2) this.cpuModel).getUpdateLocalTxLocalLocalRollbackR();
   }

   public double getUpdateLocalTxLocalRemoteRollbackR() {
      return ((CpuModel2) this.cpuModel).getUpdateLocalTxLocalRemoteRollbackR();
   }

   public double getUpdateRemoteTxExecutionR() {
      return ((CpuModel2) this.cpuModel).getUpdateRemoteTxExecutionR();
   }

   public double getUpdateRemoteTxCommitR() {
      return ((CpuModel2) this.cpuModel).getUpdateRemoteTxCommitR();
   }

   public double getUpdateRemoteTxRollbackR() {
      return ((CpuModel2) this.cpuModel).getUpdateRemoteTxRollbackR();
   }

   public double getReadOnlyTxLocalExecutionR() {
      return ((CpuModel2) this.cpuModel).getReadOnlyTxLocalExecutionR();
   }

   public double getReadOnlyTxPrepareR() {
      return ((CpuModel2) this.cpuModel).getReadOnlyTxPrepareR();
   }

   public double getReadOnlyTxCommitR() {
      return ((CpuModel2) this.cpuModel).getReadOnlyTxCommitR();
   }

   public double getNetPrepareResponseTime() {
      return this.netModel.getNetPrepareResponseTime();
   }

   public double getNetCommitTime() throws Tas2Exception {
      return netModel.getNetCommitTime();/*
      double rtt = this.netModel.getNetPrepareResponseTime();
      CpuModel2 cpu = (CpuModel2)this.cpuModel;
      double prepare = cpu.getUpdateLocalTxPrepareR() + cpu.getUpdateRemoteTxExecutionR();
       double commit = cpu.getUpdateLocalTxCommitR();
      double net = rtt - prepare - commit;
      if(net<0)
         throw new Tas2Exception("Rtt = "+rtt+" prepareR : "+prepare+" netCommit : "+net/2);
      return net/2;
      //return this.netModel.getNetCommitTime();
      */
   }

   public double getPrepareRtt() {
      return ((FixedRttNetModel) this.netModel).getTasRtt();
   }

   public double getCpuUtilization() {
      return ((CpuModel2) cpuModel).getUtilization();
   }

   public double getNetUtilization() {
      return this.netModel.getUtilization();
   }
}
