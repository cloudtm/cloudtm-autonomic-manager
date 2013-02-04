/*
 * CINI, Consorzio Interuniversitario Nazionale per l'Informatica
 * Copyright 2013 CINI and/or its affiliates and other
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
 
package eu.cloudtm.wpm.sw_probe;

/*
* @author Roberto Palmieri
*/

class InfinispanValue{
	
	//Extended Statistics
	private long avgRemoteTxCompleteNotifyTime;
	private long avgReadOnlyTxDuration;
	private long avgNumNodesRollback;
	private double remoteContentionProbability;
	private long avgWriteTxDuration;
	private long localExecutionTimeWithoutLock;
	private double applicationContentionFactor;
	private long avgClusteredGetCommandSize;
	private long avgCompleteNotificationAsync;
	private long avgNumNodesCommit;
	private long avgCommitAsync;
	private long avgRemoteGetsPerROTransaction;
	private long avgPrepareRtt;
	private long avgPrepareAsync;
	private long avgRemoteCommitTime;
	private long throughput;
	private long avgCommitCommandSize;
	private long avgNumOfLockRemoteTx;
	private long avgNumNodesCompleteNotification;
	private double lockContentionProbability;
	private long avgRemotePrepareTime;
	private long avgGetsPerROTransaction;
	private long avgNumNodesPrepare;
	private long numAbortedTxDueTimeout;
	private long avgNumNodesRemoteGet;
	private long avgCommitTime;
	private long avgRemoteGetsPerWrTransaction;
	private long avgLocalCommitTime;
	private long avgLocalLockHoldTime;
	private long numAbortedTxDueDeadlock;
	private long avgPrepareCommandSize;
	private long avgNumOfLockSuccessLocalTx;
	private long avgCommitRtt;
	private long avgLocalPrepareTime;
	private long avgRemoteGetRtt;
	private long avgWriteTxLocalExecution;
	private long avgRemoteLockHoldTime;
	private long avgRollbackTime;
	private double localContentionProbability;
	private long avgNumPutsBySuccessfulLocalTx;
	private long avgLockHoldTime;
	private double abortRate;
	private long avgNumOfLockLocalTx;
	private long avgGetsPerWrTransaction;
	private long avgLocalRollbackTime;
	private long avgRemoteRollbackTime;
	private long remoteGetExecutionTime;
	private long avgTxArrivalRate;
	private long avgLockWaitingTime;
	private double percentageSuccessWriteTransactions;
	private long avgRollbackAsync;
	private long avgRollbackRtt;
	private double percentageWriteTransactions;
	
	//Percentile
	private double getPercentileLocalRWriteTransaction;
	private double getPercentileLocalReadOnlyTransaction;
	private double getPercentileRemoteWriteTransaction;
	private double getPercentileRemoteReadOnlyTransaction;
	
	
	public long getAvgRemoteTxCompleteNotifyTime() {
		return avgRemoteTxCompleteNotifyTime;
	}
	public void setAvgRemoteTxCompleteNotifyTime(long avgRemoteTxCompleteNotifyTime) {
		this.avgRemoteTxCompleteNotifyTime = avgRemoteTxCompleteNotifyTime;
	}
	public long getAvgReadOnlyTxDuration() {
		return avgReadOnlyTxDuration;
	}
	public void setAvgReadOnlyTxDuration(long avgReadOnlyTxDuration) {
		this.avgReadOnlyTxDuration = avgReadOnlyTxDuration;
	}
	public long getAvgNumNodesRollback() {
		return avgNumNodesRollback;
	}
	public void setAvgNumNodesRollback(long avgNumNodesRollback) {
		this.avgNumNodesRollback = avgNumNodesRollback;
	}
	public double getRemoteContentionProbability() {
		return remoteContentionProbability;
	}
	public void setRemoteContentionProbability(double remoteContentionProbability) {
		this.remoteContentionProbability = remoteContentionProbability;
	}
	public long getAvgWriteTxDuration() {
		return avgWriteTxDuration;
	}
	public void setAvgWriteTxDuration(long avgWriteTxDuration) {
		this.avgWriteTxDuration = avgWriteTxDuration;
	}
	public long getLocalExecutionTimeWithoutLock() {
		return localExecutionTimeWithoutLock;
	}
	public void setLocalExecutionTimeWithoutLock(long localExecutionTimeWithoutLock) {
		this.localExecutionTimeWithoutLock = localExecutionTimeWithoutLock;
	}
	public double getApplicationContentionFactor() {
		return applicationContentionFactor;
	}
	public void setApplicationContentionFactor(double applicationContentionFactor) {
		this.applicationContentionFactor = applicationContentionFactor;
	}
	public long getAvgClusteredGetCommandSize() {
		return avgClusteredGetCommandSize;
	}
	public void setAvgClusteredGetCommandSize(long avgClusteredGetCommandSize) {
		this.avgClusteredGetCommandSize = avgClusteredGetCommandSize;
	}
	public long getAvgCompleteNotificationAsync() {
		return avgCompleteNotificationAsync;
	}
	public void setAvgCompleteNotificationAsync(long avgCompleteNotificationAsync) {
		this.avgCompleteNotificationAsync = avgCompleteNotificationAsync;
	}
	public long getAvgNumNodesCommit() {
		return avgNumNodesCommit;
	}
	public void setAvgNumNodesCommit(long avgNumNodesCommit) {
		this.avgNumNodesCommit = avgNumNodesCommit;
	}
	public long getAvgCommitAsync() {
		return avgCommitAsync;
	}
	public void setAvgCommitAsync(long avgCommitAsync) {
		this.avgCommitAsync = avgCommitAsync;
	}
	public long getAvgRemoteGetsPerROTransaction() {
		return avgRemoteGetsPerROTransaction;
	}
	public void setAvgRemoteGetsPerROTransaction(long avgRemoteGetsPerROTransaction) {
		this.avgRemoteGetsPerROTransaction = avgRemoteGetsPerROTransaction;
	}
	public long getAvgPrepareRtt() {
		return avgPrepareRtt;
	}
	public void setAvgPrepareRtt(long avgPrepareRtt) {
		this.avgPrepareRtt = avgPrepareRtt;
	}
	public long getAvgPrepareAsync() {
		return avgPrepareAsync;
	}
	public void setAvgPrepareAsync(long avgPrepareAsync) {
		this.avgPrepareAsync = avgPrepareAsync;
	}
	public long getAvgRemoteCommitTime() {
		return avgRemoteCommitTime;
	}
	public void setAvgRemoteCommitTime(long avgRemoteCommitTime) {
		this.avgRemoteCommitTime = avgRemoteCommitTime;
	}
	public long getThroughput() {
		return throughput;
	}
	public void setThroughput(long throughput) {
		this.throughput = throughput;
	}
	public long getAvgCommitCommandSize() {
		return avgCommitCommandSize;
	}
	public void setAvgCommitCommandSize(long avgCommitCommandSize) {
		this.avgCommitCommandSize = avgCommitCommandSize;
	}
	public long getAvgNumOfLockRemoteTx() {
		return avgNumOfLockRemoteTx;
	}
	public void setAvgNumOfLockRemoteTx(long avgNumOfLockRemoteTx) {
		this.avgNumOfLockRemoteTx = avgNumOfLockRemoteTx;
	}
	public long getAvgNumNodesCompleteNotification() {
		return avgNumNodesCompleteNotification;
	}
	public void setAvgNumNodesCompleteNotification(
			long avgNumNodesCompleteNotification) {
		this.avgNumNodesCompleteNotification = avgNumNodesCompleteNotification;
	}
	public double getLockContentionProbability() {
		return lockContentionProbability;
	}
	public void setLockContentionProbability(double lockContentionProbability) {
		this.lockContentionProbability = lockContentionProbability;
	}
	public long getAvgRemotePrepareTime() {
		return avgRemotePrepareTime;
	}
	public void setAvgRemotePrepareTime(long avgRemotePrepareTime) {
		this.avgRemotePrepareTime = avgRemotePrepareTime;
	}
	public long getAvgGetsPerROTransaction() {
		return avgGetsPerROTransaction;
	}
	public void setAvgGetsPerROTransaction(long avgGetsPerROTransaction) {
		this.avgGetsPerROTransaction = avgGetsPerROTransaction;
	}
	public long getAvgNumNodesPrepare() {
		return avgNumNodesPrepare;
	}
	public void setAvgNumNodesPrepare(long avgNumNodesPrepare) {
		this.avgNumNodesPrepare = avgNumNodesPrepare;
	}
	public long getNumAbortedTxDueTimeout() {
		return numAbortedTxDueTimeout;
	}
	public void setNumAbortedTxDueTimeout(long numAbortedTxDueTimeout) {
		this.numAbortedTxDueTimeout = numAbortedTxDueTimeout;
	}
	public long getAvgNumNodesRemoteGet() {
		return avgNumNodesRemoteGet;
	}
	public void setAvgNumNodesRemoteGet(long avgNumNodesRemoteGet) {
		this.avgNumNodesRemoteGet = avgNumNodesRemoteGet;
	}
	public long getAvgCommitTime() {
		return avgCommitTime;
	}
	public void setAvgCommitTime(long avgCommitTime) {
		this.avgCommitTime = avgCommitTime;
	}
	public long getAvgRemoteGetsPerWrTransaction() {
		return avgRemoteGetsPerWrTransaction;
	}
	public void setAvgRemoteGetsPerWrTransaction(long avgRemoteGetsPerWrTransaction) {
		this.avgRemoteGetsPerWrTransaction = avgRemoteGetsPerWrTransaction;
	}
	public long getAvgLocalCommitTime() {
		return avgLocalCommitTime;
	}
	public void setAvgLocalCommitTime(long avgLocalCommitTime) {
		this.avgLocalCommitTime = avgLocalCommitTime;
	}
	public long getAvgLocalLockHoldTime() {
		return avgLocalLockHoldTime;
	}
	public void setAvgLocalLockHoldTime(long avgLocalLockHoldTime) {
		this.avgLocalLockHoldTime = avgLocalLockHoldTime;
	}
	public long getNumAbortedTxDueDeadlock() {
		return numAbortedTxDueDeadlock;
	}
	public void setNumAbortedTxDueDeadlock(long numAbortedTxDueDeadlock) {
		this.numAbortedTxDueDeadlock = numAbortedTxDueDeadlock;
	}
	public long getAvgPrepareCommandSize() {
		return avgPrepareCommandSize;
	}
	public void setAvgPrepareCommandSize(long avgPrepareCommandSize) {
		this.avgPrepareCommandSize = avgPrepareCommandSize;
	}
	public long getAvgNumOfLockSuccessLocalTx() {
		return avgNumOfLockSuccessLocalTx;
	}
	public void setAvgNumOfLockSuccessLocalTx(long avgNumOfLockSuccessLocalTx) {
		this.avgNumOfLockSuccessLocalTx = avgNumOfLockSuccessLocalTx;
	}
	public long getAvgCommitRtt() {
		return avgCommitRtt;
	}
	public void setAvgCommitRtt(long avgCommitRtt) {
		this.avgCommitRtt = avgCommitRtt;
	}
	public long getAvgLocalPrepareTime() {
		return avgLocalPrepareTime;
	}
	public void setAvgLocalPrepareTime(long avgLocalPrepareTime) {
		this.avgLocalPrepareTime = avgLocalPrepareTime;
	}
	public long getAvgRemoteGetRtt() {
		return avgRemoteGetRtt;
	}
	public void setAvgRemoteGetRtt(long avgRemoteGetRtt) {
		this.avgRemoteGetRtt = avgRemoteGetRtt;
	}
	public long getAvgWriteTxLocalExecution() {
		return avgWriteTxLocalExecution;
	}
	public void setAvgWriteTxLocalExecution(long avgWriteTxLocalExecution) {
		this.avgWriteTxLocalExecution = avgWriteTxLocalExecution;
	}
	public long getAvgRemoteLockHoldTime() {
		return avgRemoteLockHoldTime;
	}
	public void setAvgRemoteLockHoldTime(long avgRemoteLockHoldTime) {
		this.avgRemoteLockHoldTime = avgRemoteLockHoldTime;
	}
	public long getAvgRollbackTime() {
		return avgRollbackTime;
	}
	public void setAvgRollbackTime(long avgRollbackTime) {
		this.avgRollbackTime = avgRollbackTime;
	}
	public double getLocalContentionProbability() {
		return localContentionProbability;
	}
	public void setLocalContentionProbability(double localContentionProbability) {
		this.localContentionProbability = localContentionProbability;
	}
	public long getAvgNumPutsBySuccessfulLocalTx() {
		return avgNumPutsBySuccessfulLocalTx;
	}
	public void setAvgNumPutsBySuccessfulLocalTx(long avgNumPutsBySuccessfulLocalTx) {
		this.avgNumPutsBySuccessfulLocalTx = avgNumPutsBySuccessfulLocalTx;
	}
	public long getAvgLockHoldTime() {
		return avgLockHoldTime;
	}
	public void setAvgLockHoldTime(long avgLockHoldTime) {
		this.avgLockHoldTime = avgLockHoldTime;
	}
	public double getAbortRate() {
		return abortRate;
	}
	public void setAbortRate(double abortRate) {
		this.abortRate = abortRate;
	}
	public long getAvgNumOfLockLocalTx() {
		return avgNumOfLockLocalTx;
	}
	public void setAvgNumOfLockLocalTx(long avgNumOfLockLocalTx) {
		this.avgNumOfLockLocalTx = avgNumOfLockLocalTx;
	}
	public long getAvgGetsPerWrTransaction() {
		return avgGetsPerWrTransaction;
	}
	public void setAvgGetsPerWrTransaction(long avgGetsPerWrTransaction) {
		this.avgGetsPerWrTransaction = avgGetsPerWrTransaction;
	}
	public long getAvgLocalRollbackTime() {
		return avgLocalRollbackTime;
	}
	public void setAvgLocalRollbackTime(long avgLocalRollbackTime) {
		this.avgLocalRollbackTime = avgLocalRollbackTime;
	}
	public long getAvgRemoteRollbackTime() {
		return avgRemoteRollbackTime;
	}
	public void setAvgRemoteRollbackTime(long avgRemoteRollbackTime) {
		this.avgRemoteRollbackTime = avgRemoteRollbackTime;
	}
	public long getRemoteGetExecutionTime() {
		return remoteGetExecutionTime;
	}
	public void setRemoteGetExecutionTime(long remoteGetExecutionTime) {
		this.remoteGetExecutionTime = remoteGetExecutionTime;
	}
	public long getAvgTxArrivalRate() {
		return avgTxArrivalRate;
	}
	public void setAvgTxArrivalRate(long avgTxArrivalRate) {
		this.avgTxArrivalRate = avgTxArrivalRate;
	}
	public long getAvgLockWaitingTime() {
		return avgLockWaitingTime;
	}
	public void setAvgLockWaitingTime(long avgLockWaitingTime) {
		this.avgLockWaitingTime = avgLockWaitingTime;
	}
	public double getPercentageSuccessWriteTransactions() {
		return percentageSuccessWriteTransactions;
	}
	public void setPercentageSuccessWriteTransactions(
			double percentageSuccessWriteTransactions) {
		this.percentageSuccessWriteTransactions = percentageSuccessWriteTransactions;
	}
	public long getAvgRollbackAsync() {
		return avgRollbackAsync;
	}
	public void setAvgRollbackAsync(long avgRollbackAsync) {
		this.avgRollbackAsync = avgRollbackAsync;
	}
	public long getAvgRollbackRtt() {
		return avgRollbackRtt;
	}
	public void setAvgRollbackRtt(long avgRollbackRtt) {
		this.avgRollbackRtt = avgRollbackRtt;
	}
	public double getPercentageWriteTransactions() {
		return percentageWriteTransactions;
	}
	public void setPercentageWriteTransactions(double percentageWriteTransactions) {
		this.percentageWriteTransactions = percentageWriteTransactions;
	}
	public double getGetPercentileLocalRWriteTransaction() {
		return getPercentileLocalRWriteTransaction;
	}
	public void setGetPercentileLocalRWriteTransaction(
			double getPercentileLocalRWriteTransaction) {
		this.getPercentileLocalRWriteTransaction = getPercentileLocalRWriteTransaction;
	}
	public double getGetPercentileLocalReadOnlyTransaction() {
		return getPercentileLocalReadOnlyTransaction;
	}
	public void setGetPercentileLocalReadOnlyTransaction(
			double getPercentileLocalReadOnlyTransaction) {
		this.getPercentileLocalReadOnlyTransaction = getPercentileLocalReadOnlyTransaction;
	}
	public double getGetPercentileRemoteWriteTransaction() {
		return getPercentileRemoteWriteTransaction;
	}
	public void setGetPercentileRemoteWriteTransaction(
			double getPercentileRemoteWriteTransaction) {
		this.getPercentileRemoteWriteTransaction = getPercentileRemoteWriteTransaction;
	}
	public double getGetPercentileRemoteReadOnlyTransaction() {
		return getPercentileRemoteReadOnlyTransaction;
	}
	public void setGetPercentileRemoteReadOnlyTransaction(
			double getPercentileRemoteReadOnlyTransaction) {
		this.getPercentileRemoteReadOnlyTransaction = getPercentileRemoteReadOnlyTransaction;
	}
	
}