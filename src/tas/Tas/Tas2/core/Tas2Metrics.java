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

package Tas2.core;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */
public class Tas2Metrics implements Metrics {

   //Probabilities
   private double commitProbability;
   private double writeCommitProbability;
   private double prepareProbability;
   private double coherProbability;
   private double localAbortProbability;
   private double subReincarnations;
   private double reincarnations;
   private double totalIncarnations;
   //Throughput
   private double throughput;
   private double writeThroughput;
   private double readThroughput;
   private double netThroughput;
   //Response Times
   private double prepareRtt;
   private double commitTime;
   private double avgResponseTime;
   private double execNoContR;
   private double readOnlyR;
   private double commitCommandR;
   private double localHoldTime;
   private double remoteHoldTime;
   private double suxLocalHoldTime;
   private double suxRemoteHoldTime;
   private double replayR;
   private double cpuRollbackR;
   private double updateTxResponseTime;
   private double readOnlyTxResponseTime;
   private double prepareCommitResponseTime;

   //Utilization
   private double cpuUtilization;
   private double netUtilization;


   public double getSubReincarnations() {
      return subReincarnations;
   }

   public double getCommitTime() {
      return commitTime;
   }

   public void setCommitTime(double commitTime) {
      this.commitTime = commitTime;
   }

   public void setSubReincarnations(double subReincarnations) {
      this.subReincarnations = subReincarnations;
   }

   public double getReincarnations() {
      return reincarnations;
   }

   public void setReincarnations(double reincarnations) {
      this.reincarnations = reincarnations;
   }

   public double getTotalIncarnations() {
      return totalIncarnations;
   }

   public void setTotalIncarnations(double totalIncarnations) {
      this.totalIncarnations = totalIncarnations;
   }


   public double getNetThroughput() {
      return netThroughput;
   }

   public void setNetThroughput(double netThroughput) {
      this.netThroughput = netThroughput;
   }

   public double getPrepareCommitResponseTime() {
      return prepareCommitResponseTime;
   }

   public void setPrepareCommitResponseTime(double prepareCommitResponseTime) {
      this.prepareCommitResponseTime = prepareCommitResponseTime;
   }

   public double getUpdateTxResponseTime() {
      return updateTxResponseTime;
   }

   public void setUpdateTxResponseTime(double updateTxResponseTime) {
      this.updateTxResponseTime = updateTxResponseTime;
   }

   public double getReadOnlyTxResponseTime() {
      return readOnlyTxResponseTime;
   }

   public void setReadOnlyTxResponseTime(double readOnlyTxResponseTime) {
      this.readOnlyTxResponseTime = readOnlyTxResponseTime;
   }

   public double getReplayR() {
      return replayR;
   }

   public void setReplayR(double replayR) {
      this.replayR = replayR;
   }

   public double getCpuRollbackR() {
      return cpuRollbackR;
   }

   public void setCpuRollbackR(double cpuRollbackR) {
      this.cpuRollbackR = cpuRollbackR;
   }

   public double getLocalAbortProbability() {
      return localAbortProbability;
   }

   public void setLocalAbortProbability(double localAbortProbability) {
      this.localAbortProbability = localAbortProbability;
   }

   public double getSuxLocalHoldTime() {
      return suxLocalHoldTime;
   }

   public void setSuxLocalHoldTime(double suxLocalHoldTime) {
      this.suxLocalHoldTime = suxLocalHoldTime;
   }

   public double getSuxRemoteHoldTime() {
      return suxRemoteHoldTime;
   }

   public void setSuxRemoteHoldTime(double suxRemoteHoldTime) {
      this.suxRemoteHoldTime = suxRemoteHoldTime;
   }

   public double getCpuUtilization() {
      return cpuUtilization;
   }

   public void setCpuUtilization(double cpuUtilization) {
      this.cpuUtilization = cpuUtilization;
   }

   public double getNetUtilization() {
      return netUtilization;
   }

   public void setNetUtilization(double netUtilization) {
      this.netUtilization = netUtilization;
   }

   public double getCoherProbability() {
      return coherProbability;
   }

   public void setCoherProbability(double coherProbability) {
      this.coherProbability = coherProbability;
   }

   public double getLocalHoldTime() {
      return localHoldTime;
   }

   public void setLocalHoldTime(double localHoldTime) {
      this.localHoldTime = localHoldTime;
   }

   public double getRemoteHoldTime() {
      return remoteHoldTime;
   }

   public void setRemoteHoldTime(double remoteHoldTime) {
      this.remoteHoldTime = remoteHoldTime;
   }

   public double getCommitCommandR() {
      return commitCommandR;
   }

   public void setCommitCommandR(double commitCommandR) {
      this.commitCommandR = commitCommandR;
   }

   public double getExecNoContR() {
      return execNoContR;
   }

   public void setExecNoContR(double execNoContR) {
      this.execNoContR = execNoContR;
   }

   public double getReadOnlyR() {
      return readOnlyR;
   }

   public void setReadOnlyR(double readOnlyR) {
      this.readOnlyR = readOnlyR;
   }

   public double getPrepareRtt() {
      return prepareRtt;
   }

   public void setPrepareRtt(double prepareRtt) {
      this.prepareRtt = prepareRtt;
   }

   public double getCommitProbability() {
      return commitProbability;
   }

   public double getWriteCommitProbability() {
      return writeCommitProbability;
   }

   public void setWriteCommitProbability(double writeCommitProbability) {
      this.writeCommitProbability = writeCommitProbability;
   }

   public void setCommitProbability(double commitProbability) {
      this.commitProbability = commitProbability;
   }

   public double getThroughput() {
      return throughput;
   }

   public void setThroughput(double throughput) {
      this.throughput = throughput;
   }

   public double getWriteThroughput() {
      return writeThroughput;
   }

   public void setWriteThroughput(double writeThroughput) {
      this.writeThroughput = writeThroughput;
   }

   public double getReadThroughput() {
      return readThroughput;
   }

   public void setReadThroughput(double readThroughput) {
      this.readThroughput = readThroughput;
   }

   public double getPrepareProbability() {
      return prepareProbability;
   }

   public void setPrepareProbability(double prepareProbability) {
      this.prepareProbability = prepareProbability;
   }

   public double getAvgResponseTime() {
      return avgResponseTime;
   }

   public void setAvgResponseTime(double avgResponseTime) {
      this.avgResponseTime = avgResponseTime;
   }


   @Override
   public String toString() {
      return "Tas2Metrics{" +
              "commitProbability=" + commitProbability +
              ", writeCommitProbability=" + writeCommitProbability +
              ", prepareProbability=" + prepareProbability +
              ", coherProbability=" + coherProbability +
              ", localAbortProbability=" + localAbortProbability +
              ", throughput=" + throughput +
              ", writeThroughput=" + writeThroughput +
              ", readThroughput=" + readThroughput +
              ", prepareRtt=" + prepareRtt +
              ", avgResponseTime=" + avgResponseTime +
              ", execNoContR=" + execNoContR +
              ", readOnlyR=" + readOnlyR +
              ", commitCommandR=" + commitCommandR +
              ", localHoldTime=" + localHoldTime +
              ", remoteHoldTime=" + remoteHoldTime +
              ", suxLocalHoldTime=" + suxLocalHoldTime +
              ", suxRemoteHoldTime=" + suxRemoteHoldTime +
              ", replayR=" + replayR +
              ", cpuRollbackR=" + cpuRollbackR +
              ", updateTxResponseTime=" + updateTxResponseTime +
              ", readOnlyTxResponseTime=" + readOnlyTxResponseTime +
              ", prepareCommitResponseTime=" + prepareCommitResponseTime +
              ", cpuUtilization=" + cpuUtilization +
              ", netUtilization=" + netUtilization +
              '}';
   }
}
