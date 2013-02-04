package controllerTas.test;   /*
 * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
 * Copyright 2013 INESC-ID and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/02/13
 */

import Tas2.core.ModelResult;
import Tas2.core.Tas2;
import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.core.environment.WorkParams;
import Tas2.exception.Tas2Exception;
import Tas2.physicalModel.cpunet.cpu.CpuServiceTimes;
import Tas2.physicalModel.cpunet.cpu.two.CpuServiceTimes2Impl;
import Tas2.physicalModel.cpunet.net.queue.NetServiceTimes;
import Tas2.physicalModel.cpunet.net.tas.FixedRttServiceTimes;
import controllerTas.common.DSTMScenarioFactory;
import controllerTas.common.PublishAttributeException;
import eu.cloudtm.wpm.logService.remote.events.PublishAttribute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Set;

public class DummyScenarioFactory extends DSTMScenarioFactory {

   private static final Log log = LogFactory.getLog(DummyScenarioFactory.class);

   public void invoke() throws Tas2Exception {
      DSTMScenarioTas2 scenario = buildScenario();
      Tas2 tas = new Tas2();
      ModelResult result = tas.solve(scenario);
      log.trace("Invoked tas " + result.getMetrics().getThroughput());

   }

   @Override
   public DSTMScenarioTas2 buildScenario(Set<HashMap<String, PublishAttribute>> jmx, Set<HashMap<String, PublishAttribute>> mem, double timeWindow, double threads) throws PublishAttributeException, Tas2Exception {
      return buildScenario();
   }

   public DSTMScenarioTas2 buildScenario() throws Tas2Exception {
      DSTMScenarioTas2 scenario = new DSTMScenarioTas2(buildCpuServiceTimes(), buildNetServiceTimes(), buildWorkloadParams());
      log.warn(scenario.toString());
      return scenario;
   }

   private WorkParams buildWorkloadParams() {

      WorkParams workParams = new WorkParams();
      workParams.setRetryOnAbort(true);
      workParams.setWriteOpsPerTx(1);
      workParams.setWritePercentage(1e-9);
      workParams.setLambda(0D);
      workParams.setPrepareMessageSize(4000);
      workParams.setNumNodes(5);
      workParams.setThreadsPerNode(3);
      workParams.setApplicationContentionFactor(1e-5);
      workParams.setMem(3E9);
      return workParams;
   }

   private CpuServiceTimes buildCpuServiceTimes() {


      CpuServiceTimes2Impl cpu = new CpuServiceTimes2Impl();
      cpu.setUpdateTxLocalExecutionS(1E5);
      cpu.setUpdateTxPrepareS(1E5);
      cpu.setUpdateTxCommitS(1E5);
      cpu.setUpdateTxLocalLocalRollbackS(1E5);
      cpu.setUpdateTxLocalRemoteRollbackS(1E5);

      cpu.setReadOnlyTxLocalExecutionS(100E6);
      cpu.setReadOnlyTxPrepareS(1E5);
      cpu.setReadOnlyTxCommitS(1E5);

      cpu.setUpdateTxRemoteExecutionS(1E5);
      cpu.setUpdateTxRemoteCommitS(1E5);
      cpu.setUpdateTxRemoteRollbackS(1E5);


      return cpu;
   }

   private NetServiceTimes buildNetServiceTimes() {
      return new FixedRttServiceTimes(1e6, 1e6);
   }

   public static void main(String args[]) throws Tas2Exception {
      new DummyScenarioFactory().invoke();
   }
}
