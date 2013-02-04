package controllerTas.common;   /*
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


import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.core.environment.WorkParams;
import Tas2.exception.Tas2Exception;
import Tas2.physicalModel.cpunet.cpu.CpuServiceTimes;
import Tas2.physicalModel.cpunet.cpu.two.CpuServiceTimes2Impl;
import Tas2.physicalModel.cpunet.net.queue.NetServiceTimes;
import Tas2.physicalModel.cpunet.net.tas.FixedRttServiceTimes;
import eu.cloudtm.wpm.logService.remote.events.PublishAttribute;
import eu.cloudtm.wpm.logService.remote.events.PublishMeasurement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class DSTMScenarioFactory {

   private static final Log log = LogFactory.getLog(DSTMScenarioFactory.class);
   private static double THREADS = 3.0D;
   private static double TIME_WINDOW = 60D;
   private static final double CLOSED_SYSTEM = 0D;
   private static final boolean ROA = true;

   public DSTMScenarioTas2 buildScenario(Set<HashMap<String, PublishAttribute>> jmx, Set<HashMap<String, PublishAttribute>> mem, double timeWindow, double threads) throws PublishAttributeException, Tas2Exception {
      THREADS = threads;
      TIME_WINDOW = timeWindow;
      CpuServiceTimes2Impl cpu = (CpuServiceTimes2Impl) buildCpuServiceTimes(jmx);
      log.trace(cpu);
      WorkParams workParams = buildWorkloadParams(jmx, mem);
      log.trace(workParams);
      NetServiceTimes net = buildNetServiceTimes(jmx);
      log.trace(net);
      log.info("Most important features: wrPerc = "+workParams.getWritePercentage()+", wrPerTx "+workParams.getWriteOpsPerTx()+ " updateTxS "+cpu.getUpdateTxLocalExecutionS()+ " readOnlyTxS "+cpu.getReadOnlyTxLocalExecutionS()+ " acf "+acf(jmx));
      return new DSTMScenarioTas2(cpu, net, workParams);
   }


   private Set<HashMap<String, PublishAttribute>> toHashMapSet(Set<PublishMeasurement> measurements) {
      Set<HashMap<String, PublishAttribute>> set = new HashSet<HashMap<String, PublishAttribute>>();
      for (PublishMeasurement m : measurements) {
         set.add(m.getValues());
      }
      return set;
   }


   private double getAvgAttribute(String attribute, Set<HashMap<String, PublishAttribute>> values) throws PublishAttributeException {
      double num = values.size(), temp = 0;
      Object actualValue;
      for (HashMap<String, PublishAttribute> h : values) {
         if (h == null) {
            throw new PublishAttributeException("I had a null set of values");
         }
         log.trace("Asking for " + attribute);
         //the getName may give a nullPointerException, actually
         if ((actualValue = h.get(attribute).getValue()) == null) {
            throw new PublishAttributeException(h.get(attribute).getName() + " is null");
         }
         try {
            temp += cast(actualValue);
         } catch (ClassCastException c) {
            throw new PublishAttributeException(h.get(attribute).getName() + " is not a double/long/int and cannot be averaged. It appears to be " + actualValue.getClass());

         }
      }
      return temp / num;
   }

   private double cast(Object o) throws ClassCastException {
      try {
         return (Long) o;
      } catch (ClassCastException c) {
         try {
            return (Double) o;
         } catch (ClassCastException cc) {
            return (Integer) o;
         }
      }
   }


   private CpuServiceTimes buildCpuServiceTimes(Set<HashMap<String, PublishAttribute>> values) throws PublishAttributeException {
      //Local Update
      double updateLocalTxLocalExec = getAvgAttribute("LocalUpdateTxLocalServiceTime", values);
      double updateLocalTxPrepare = getAvgAttribute("LocalUpdateTxPrepareServiceTime", values);
      double updateLocalTxCommit = getAvgAttribute("LocalUpdateTxCommitServiceTime", values);
      double updateLocalTxLocalRollback = getAvgAttribute("LocalUpdateTxLocalRollbackResponseTime", values);
      double updateLocalTxRemoteRollback = getAvgAttribute("LocalUpdateTxRemoteRollbackServiceTime", values);
      //Local Read Only
      //TODO this should be the service time. I only have the response time due to a bug. It's +/- the same if I don't vary the threads and keep the load low
      double readOnlyTxLocalExec = getAvgAttribute("AvgLocalReadOnlyExecutionTime",values);//getAvgAttribute("LocalReadOnlyTxLocalResponseTime", values);
      double readOnlyTxPrepare = getAvgAttribute("LocalReadOnlyTxPrepareServiceTime", values);//("ReadOnlyCommitCpuTime");
      double readOnlyTxCommit = getAvgAttribute("LocalReadOnlyTxCommitServiceTime", values);
      //Remote Update
      double updateRemoteTxLocalExec = getAvgAttribute("RemoteUpdateTxPrepareServiceTime", values);
      double updateRemoteTxCommit = getAvgAttribute("RemoteUpdateTxCommitServiceTime", values);
      double updateRemoteTxRollback = getAvgAttribute("RemoteUpdateTxRollbackServiceTime", values);

      CpuServiceTimes2Impl cpu = new CpuServiceTimes2Impl();
      cpu.setUpdateTxLocalExecutionS(updateLocalTxLocalExec);
      cpu.setUpdateTxPrepareS(updateLocalTxPrepare);
      cpu.setUpdateTxCommitS(updateLocalTxCommit);
      cpu.setUpdateTxLocalLocalRollbackS(updateLocalTxLocalRollback);
      cpu.setUpdateTxLocalRemoteRollbackS(updateLocalTxRemoteRollback);

      cpu.setReadOnlyTxLocalExecutionS(readOnlyTxLocalExec);
      cpu.setReadOnlyTxPrepareS(readOnlyTxPrepare);
      cpu.setReadOnlyTxCommitS(readOnlyTxCommit);

      cpu.setUpdateTxRemoteExecutionS(updateRemoteTxLocalExec);
      cpu.setUpdateTxRemoteCommitS(updateRemoteTxCommit);
      cpu.setUpdateTxRemoteRollbackS(updateRemoteTxRollback);


      return cpu;
   }

   private WorkParams buildWorkloadParams(Set<HashMap<String, PublishAttribute>> JMXvalues, Set<HashMap<String, PublishAttribute>> MEMvalues) throws PublishAttributeException {
      //TODO: check
      double wrOps = (double)((int)getAvgAttribute("SuxNumPuts", JMXvalues));
      boolean RoA = ROA;
      double wrPer = getAvgAttribute("RetryWritePercentage", JMXvalues);
      double lambda = CLOSED_SYSTEM;
      double mexSize = getAvgAttribute("PrepareCommandBytes", JMXvalues);
      double nodes = JMXvalues.size();
      double numThreads = THREADS;
      double acf = acf(JMXvalues);//rrp.getAcfFromInversePrepareProb(numThreads,wrOps);//rrp.getClosedAcf(numThreads);


      double mem = getAvgAttribute("MemoryInfo.used", MEMvalues);

      WorkParams workParams = new WorkParams();
      workParams.setRetryOnAbort(RoA);
      workParams.setWriteOpsPerTx(wrOps);
      workParams.setWritePercentage(wrPer);
      workParams.setLambda(lambda);
      workParams.setPrepareMessageSize(mexSize);
      workParams.setNumNodes(nodes);
      workParams.setThreadsPerNode(numThreads);
      workParams.setApplicationContentionFactor(acf);
      workParams.setMem(mem);

      return workParams;
   }

   private double acf(Set<HashMap<String, PublishAttribute>> JMXvalues) throws PublishAttributeException {
      return closedAcf_Impl2(JMXvalues, THREADS, TIME_WINDOW);
   }

   private double getLocalAbortProb(Set<HashMap<String, PublishAttribute>> JMXvalues) throws PublishAttributeException {
      double puts = getAvgAttribute("NumPuts", JMXvalues);
      log.trace("Attempted put " + puts);
      double okPuts = getAvgAttribute("PaoloLocalTakenLocks", JMXvalues);
      log.trace("Ok put " + okPuts);
      if (puts != 0) {
         return (puts - okPuts) / puts;
      }
      return 0;
   }

   private double closedAcf_Impl2(Set<HashMap<String, PublishAttribute>> JMXvalues, double threads, double timeWindow) throws PublishAttributeException {

      double otherThreads = threads - 1.0D;
      double pCont = getLocalAbortProb(JMXvalues);
      log.trace("pCont is " + pCont);
      if (pCont == 0)
         return 0D;
      double otherThreadsFraction = otherThreads / threads;
      double paoloLocalLocks = getAvgAttribute("PaoloLocalTakenLocks", JMXvalues);
      double paoloRemoteLocks = getAvgAttribute("PaoloRemoteTakenLocks", JMXvalues);
      double otherLocalLocks = paoloLocalLocks * otherThreadsFraction;
      log.trace("OtherLocalLocks " + otherLocalLocks);
      double ll = 1e-9 * otherLocalLocks / timeWindow;
      log.trace("localLambda " + ll);
      double rl = 1e-9 * paoloRemoteLocks / timeWindow;
      log.trace("remoteLambda " + rl);
      double lh = pLocalHoldTime(JMXvalues);
      double rh = pRemoteHoldTime(JMXvalues);
      double lm = ll * lh + rl * rh;
      if (lm == 0)
         return 0;

      return pCont / (lm);
   }

   private double pLocalHoldTime(Set<HashMap<String, PublishAttribute>> JMXvalues) throws PublishAttributeException {
      double pLocalHoldTime = getAvgAttribute("PaoloLocalTakenHoldTime", JMXvalues);
      double pLocalLocks = getAvgAttribute("PaoloLocalTakenLocks", JMXvalues);
      if (pLocalLocks == 0)
         return 0;
      return pLocalHoldTime / pLocalLocks;
   }

   public double pRemoteHoldTime(Set<HashMap<String, PublishAttribute>> JMXvalues) throws PublishAttributeException {
      double pRemoteHoldTime = getAvgAttribute("PaoloRemoteTakenHoldTime", JMXvalues);
      double pRemoteLocks = getAvgAttribute("PaoloRemoteTakenLocks", JMXvalues);
      if (pRemoteLocks == 0)
         return 0;
      return pRemoteHoldTime / pRemoteLocks;

   }

   private NetServiceTimes buildNetServiceTimes(Set<HashMap<String, PublishAttribute>> JMXvalues) {
      return new FixedRttServiceTimes(1, 1);
   }
}
