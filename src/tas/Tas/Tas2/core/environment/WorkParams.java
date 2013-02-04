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

package Tas2.core.environment;

import Tas2.exception.Tas2Exception;
import Tas2.util.Tas2Util;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */
public class WorkParams {

   private double lambda;
   private double writePercentage;
   private double writeOpsPerTx;
   private double backoff;
   private double timeout;
   private double threadsPerNode;
   private double numNodes;
   private double applicationContentionFactor;
   private double prepareMessageSize;
   private double cpu;
   private double mem;
   private double injectedCommitR;
   private double injectedReadOnlyR;
   private double injectedExecNoContR;


   protected boolean retryOnAbort = false;
   private int protocol;

   public void checkCorrectness() throws Tas2Exception {
      Tas2Util.checkPositive(lambda, false, "Lambda");
      Tas2Util.checkPositive(writePercentage, false, "WrPerc");
      Tas2Util.checkPositive(numNodes, true, "numNodes");
      Tas2Util.checkPositive(threadsPerNode, true, "numThreads");
      Tas2Util.checkProb(applicationContentionFactor, "Acf");
      boolean readOnly = false;
      if (writePercentage > 0)
         readOnly = true;
      Tas2Util.checkPositive(prepareMessageSize, !readOnly, "mexSize");
   }

   public double getInjectedCommitR() {
      return injectedCommitR;
   }

   public void setInjectedCommitR(double injectedCommitR) {
      this.injectedCommitR = injectedCommitR;
   }

   public double getLambda() {
      return lambda;
   }

   public void setLambda(double lambda) {
      this.lambda = lambda;
   }

   public double getWritePercentage() {
      return writePercentage;
   }

   public void setWritePercentage(double writePercentage) {
      this.writePercentage = writePercentage;
   }

   public double getWriteOpsPerTx() {
      return writeOpsPerTx;
   }

   public void setWriteOpsPerTx(double writeOpsPerTx) {
      this.writeOpsPerTx = writeOpsPerTx;
   }

   public double getBackoff() {
      return backoff;
   }

   public void setBackoff(double backoff) {
      this.backoff = backoff;
   }

   public double getTimeout() {
      return timeout;
   }

   public void setTimeout(double timeout) {
      this.timeout = timeout;
   }

   public double getThreadsPerNode() {
      return threadsPerNode;
   }

   public void setThreadsPerNode(double threadsPerNode) {
      this.threadsPerNode = threadsPerNode;
   }

   public double getNumNodes() {
      return numNodes;
   }

   public void setNumNodes(double numNodes) {
      this.numNodes = numNodes;
   }

   public double getApplicationContentionFactor() {
      return applicationContentionFactor;
   }

   public void setApplicationContentionFactor(double applicationContentionFactor) {
      this.applicationContentionFactor = applicationContentionFactor;
   }

   public double getPrepareMessageSize() {
      return prepareMessageSize;
   }

   public void setPrepareMessageSize(double prepareMessageSize) {
      this.prepareMessageSize = prepareMessageSize;
   }

   public double getCpu() {
      return cpu;
   }

   public void setCpu(double cpu) {
      this.cpu = cpu;
   }

   public double getMem() {
      return mem;
   }

   public void setMem(double mem) {
      this.mem = mem;
   }

   public boolean isRetryOnAbort() {
      return retryOnAbort;
   }

   public void setRetryOnAbort(boolean retryOnAbort) {
      this.retryOnAbort = retryOnAbort;
   }

   public int getProtocol() {
      return protocol;
   }

   public void setProtocol(int protocol) {
      this.protocol = protocol;
   }

   public double getInjectedReadOnlyR() {
      return injectedReadOnlyR;
   }

   public void setInjectedReadOnlyR(double injectedReadOnlyR) {
      this.injectedReadOnlyR = injectedReadOnlyR;
   }

   public double getInjectedExecNoContR() {
      return injectedExecNoContR;
   }

   public void setInjectedExecNoContR(double injectedExecNoContR) {
      this.injectedExecNoContR = injectedExecNoContR;
   }

   @Override
   public String toString() {
      return "WorkParams{" +
              "lambda=" + lambda +
              ", writePercentage=" + writePercentage +
              ", writeOpsPerTx=" + writeOpsPerTx +
              ", backoff=" + backoff +
              ", timeout=" + timeout +
              ", threadsPerNode=" + threadsPerNode +
              ", numNodes=" + numNodes +
              ", applicationContentionFactor=" + applicationContentionFactor +
              ", prepareMessageSize=" + prepareMessageSize +
              ", cpu=" + cpu +
              ", mem=" + mem +
              ", injectedCommitR=" + injectedCommitR +
              ", injectedReadOnlyR=" + injectedReadOnlyR +
              ", injectedExecNoContR=" + injectedExecNoContR +
              ", retryOnAbort=" + retryOnAbort +
              ", protocol=" + protocol +
              '}';
   }
}
