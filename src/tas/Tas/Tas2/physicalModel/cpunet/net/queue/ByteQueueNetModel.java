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


import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.exception.Tas2Exception;
import Tas2.physicalModel.cpunet.parameters.WorkloadParams;
import Tas2.physicalModel.queues.QueueFactory;
import Tas2.util.Tas2Util;
import open.queues.OpenClazz;
import open.queues.Queue;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 24/11/12
 */

//TODO ASSICURATI CHE I LAMBDA SIANO MOLTIPLICATI BENE PER LE PROBABILITA' E PERCENTUALI!
public class ByteQueueNetModel implements NetModel {

   final static int NET_PREPARE = 0;
   final static int NET_MSG = 1;

   private NetServiceTimes serviceRates;
   private WorkloadParams params;

   private Queue netQueue;

   public ByteQueueNetModel(NetServiceTimes serviceRates, DSTMScenarioTas2 scenario, WorkloadParams params) throws Tas2Exception {
      this.serviceRates = serviceRates;
      this.params = params;
      this.netQueue = initNetQueue();
   }


   public double getUtilization() {
      return this.netQueue.getRo();
   }


   private Queue initNetQueue() throws Tas2Exception {
      OpenClazz[] netClazz = new OpenClazz[2];
      netClazz[NET_PREPARE] = initNetPrepare(serviceRates, params);
      netClazz[NET_MSG] = initNetMsg(serviceRates, params);
      Tas2Util.debug("NetModel\n" + netClazz[NET_PREPARE] + "\n" + netClazz[NET_MSG]);
      return QueueFactory.buildQueue(1, "NET", netClazz);
   }

   private OpenClazz initNetPrepare(NetServiceTimes serviceRates, WorkloadParams params) {
      return singleNetVisitByteLambdaNetPrepare(serviceRates, params);
   }

   private OpenClazz singleNetVisitByteLambdaNetPrepare(NetServiceTimes serviceRates, WorkloadParams params) {

      double mexSize = params.getMessageSize();
      double recipients = params.getNumberOfNodes() - 1.0D;
      double netSPrepare = ((ByteNetServiceTimes) serviceRates).getByteCost();
      double netLambda = params.getNanoTxNetLambda() * mexSize * recipients;
      OpenClazz prepareClazz = new OpenClazz(NET_PREPARE, netSPrepare, netLambda);
      return prepareClazz;
   }

   private OpenClazz initNetMsg(NetServiceTimes serviceRates, WorkloadParams params) {
      return new OpenClazz(NET_MSG, 0, 0);
   }


   public double getNetPrepareResponseTime() {
      return this.byteNetPrepareResponseTime(serviceRates, params);
   }

   /*
    I get the responseTime for a single byte; I multiply it by the number of bytes I send
    This is because the lambda is in byte/sec
    */
   private double byteNetPrepareResponseTime(NetServiceTimes serviceTimes, WorkloadParams params) {

      ByteNetServiceTimes st = (ByteNetServiceTimes) serviceTimes;
      double byteCost = st.getByteCost();
      double netByteR = this.netQueue.getResponseTimeByServiceTime(byteCost);
      double recipients = params.getNumberOfNodes() - 1.0D;
      double mexSize = params.getMessageSize();
      double prepR = netByteR * recipients * mexSize;
      //Tas2Util.debug("ByteCost "+byteCost+" netByteR "+netByteR+" mexSize "+(mexSize * recipients)+" responseTime "+prepR);
      return prepR;
   }

   public double getNetCommitTime() {
      return 0d;
   }
}
