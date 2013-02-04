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

package eu.cloudtm.rmi.statistics;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.Thread.State;

import javax.transaction.Transaction;

/*
* @author Roberto Palmieri
*/
public class InfinispanStatistics implements Serializable {
	

	private static final long serialVersionUID = 7450205779990714462L;
	
    private Thread thread = null;
    private State thread_state;
    private Transaction lastTransaction = null;
    private Long totalEvictions = new Long(0);

   //non transactional context
   private Long hitTotalNoTXTime = new Long(0);
   private Long missTotalNoTXTime = new Long(0);
   private Long writeNoTXTotalTime = new Long(0);
   private Long totalHitsNoTX = new Long(0);
   private Long totalMissesNoTX = new Long(0);
   private Long totalWritesNoTx = new Long(0);
   private Long totalRemoveHitsNoTX = new Long(0);
   private Long totalRemoveMissesNoTX = new Long(0);

   //transactional Read Only context
   private Long hitTotalTXROTime = new Long(0);
   private Long missTotalTXROTime = new Long(0);
   private Long writeTXROTotalTime = new Long(0);
   private Long totalHitsTXRO = new Long(0);
   private Long totalMissesTXRO = new Long(0);
   private Long totalWritesTXRO = new Long(0);
   private Long totalRemoveHitsTXRO = new Long(0);
   private Long totalRemoveMissesTXRO = new Long(0);

    //transactional context
    private Long hitTotalTXTime = new Long(0);
    private Long missTotalTXTime = new Long(0);
    private Long writeTXTotalTime = new Long(0);
    private Long totalHitsTX = new Long(0);
    private Long totalMissesTX = new Long(0);
    private Long totalWritesTX = new Long(0);
    private Long totalRemoveHitsTX = new Long(0);
    private Long totalRemoveMissesTX = new Long(0);

    //transactional temporary context
    private Long tempHitTotalTXTime = new Long(0);
    private Long tempMissTotalTXTime = new Long(0);
    private Long tempWriteTXTotalTime = new Long(0);
    private Long tempTotalHitsTX = new Long(0);
    private Long tempTotalMissesTX = new Long(0);
    private Long tempTotalWritesTX = new Long(0);
    private Long tempTotalRemoveHitsTX = new Long(0);
    private Long tempTotalRemoveMissesTX = new Long(0);

    // Needed to check if the transaction is Read Only
    private boolean isTXReadOnly = true;

    //prepares, commits and rollbacks time
   private Long prepareTotalTime = new Long(0);
   private Long commitTotalTime = new Long(0);
   private Long rollbackTotalTime = new Long(0);
   private Long prepareTotalTimeRO = new Long(0);
   private Long commitTotalTimeRO = new Long(0);
   private Long rollbackTotalTimeRO = new Long(0);
   private Long tempPrepareTotalTime = new Long(0);
   private Long tempCommitTotalTime = new Long(0);
   private Long tempRollbackTotalTime = new Long(0);
   
   //prepares, commits and rollback numbers
   private Long totalPrepares = new Long(0);;
   private Long totalCommits = new Long(0);
   private Long totalRollbacks = new Long(0);
   private Long totalPreparesRO = new Long(0);;
   private Long totalCommitsRO = new Long(0);
   private Long totalRollbacksRO = new Long(0);
   private Long tempTotalPrepares = new Long(0);;
   private Long tempTotalCommits = new Long(0);
   private Long tempTotalRollbacks = new Long(0);

   //started transactions
    private Long transactions = new Long(0);
    private Long transactionsRO = new Long(0);
    private Long tempTransactions = new Long(0);
/*
   //started transactions
   private Long lastTransactions = new Long(0);
   */

   public InfinispanStatistics(Thread thread) {
       this.thread=thread;
   }

    // Set and Get operations for ReadOnly boolean
    public boolean getReadOnlyInfo(){
        return isTXReadOnly;
    }
    public void setTXReadOnly(boolean readOnly){
        isTXReadOnly=readOnly;
    }

    public void addTotalPrepareTime(Long time) {
        this.tempPrepareTotalTime+=time;
   }

    public void addTotalCommitTime(Long time) {
        this.tempCommitTotalTime+=time;
    }

    public void addTotalRollbackTime(Long time) {
        this.tempRollbackTotalTime+=time;
    }

    public void addHitTotalNoTXTime(Long time) {
        this.hitTotalNoTXTime+=time;
    }
    public void addHitTotalTXTime(Long time) {
        this.tempHitTotalTXTime+=time;
    }
    public void addMissTotalNoTXTime(Long time) {
        this.missTotalNoTXTime+=time;
    }
    public void addMissTotalTXTime(Long time) {
        this.tempMissTotalTXTime+=time;
    }
    public void addWritesNoTXTotalTime(Long time) {
        this.writeNoTXTotalTime+=time;
    }
    public void addWritesTXTotalTime(Long time) {
        this.tempWriteTXTotalTime+=time;
    }

    public void incrementTotalPrepares() {
        this.tempTotalPrepares++;
    }
    public void incrementTotalCommits() {
        this.tempTotalCommits++;
    }
    public void incrementTotalRollbacks() {
        this.tempTotalRollbacks++;
    }

    public void incrementTotalHitsNoTX() {
        this.totalHitsNoTX++;
    }
    public void incrementTotalHitsTX() {
        this.tempTotalHitsTX++;
    }
    public void incrementTotalRemoveMissesNoTX() {
        this.totalRemoveMissesNoTX++;
    }
    public void incrementTotalRemoveMissesTX() {
        this.tempTotalRemoveMissesTX++;
    }
    public void incrementRemoveHitsNoTX() {
        this.totalRemoveHitsNoTX++;
    }
    public void incrementRemoveHitsTX() {
        this.tempTotalRemoveHitsTX++;
    }
    public void incrementTotalMissesNoTX() {
        this.totalMissesNoTX++;
    }
    public void incrementTotalMissesTX() {
        this.tempTotalMissesTX++;
    }
    public void incrementTotalWritesNoTX() {
        this.totalWritesNoTx++;
    }
    public void incrementTotalWritesTX() {
        this.tempTotalWritesTX++;
    }
    public void incrementEvictions() {
        this.totalEvictions++;
    }
    public void incrementTransactions() {
        this.tempTransactions++;
    }
    

    //temporary operations
    public void updateTXStats(){

        if (isTXReadOnly){
            hitTotalTXROTime += tempHitTotalTXTime;
            missTotalTXROTime += tempMissTotalTXTime;
            writeTXROTotalTime += tempWriteTXTotalTime;
            totalHitsTXRO += tempTotalHitsTX;
            totalMissesTXRO += tempTotalMissesTX;
            totalWritesTXRO += tempTotalWritesTX;
            totalRemoveHitsTXRO += tempTotalRemoveHitsTX;
            totalRemoveMissesTXRO += tempTotalRemoveMissesTX;

            prepareTotalTimeRO += tempPrepareTotalTime;
            commitTotalTimeRO += tempCommitTotalTime;
            rollbackTotalTimeRO += tempRollbackTotalTime;
            totalPreparesRO += tempTotalPrepares;
            totalCommitsRO += tempTotalCommits;
            totalRollbacksRO += tempTotalRollbacks;

            transactionsRO += tempTransactions;
        }
        else{
            hitTotalTXTime += tempHitTotalTXTime;
            missTotalTXTime += tempMissTotalTXTime;
            writeTXTotalTime += tempWriteTXTotalTime;
            totalHitsTX += tempTotalHitsTX;
            totalMissesTX += tempTotalMissesTX;
            totalWritesTX += tempTotalWritesTX;
            totalRemoveHitsTX += tempTotalRemoveHitsTX;
            totalRemoveMissesTX += tempTotalRemoveMissesTX;

            prepareTotalTime += tempPrepareTotalTime;
            commitTotalTime += tempCommitTotalTime;
            rollbackTotalTime += tempRollbackTotalTime;
            totalPrepares += tempTotalPrepares;
            totalCommits += tempTotalCommits;
            totalRollbacks += tempTotalRollbacks;

            transactions += tempTransactions;
        }
        resetTempValues();
        isTXReadOnly = true;
    }

    public Long getPrepareTotalTime() {
        return prepareTotalTime;
    }
    public Long getCommitTotalTime() {
        return commitTotalTime;
    }
    public Long getRollbackTotalTime() {
        return rollbackTotalTime;
    }
    public Long getTotalPrepares() {
        return totalPrepares;
    }
    public Long getTotalCommits() {
        return totalCommits;
    }
    public Long getTotalRollbacks() {
        return totalRollbacks;
    }
    public Long getHitTotalNoTXTime() {
        return hitTotalNoTXTime;
    }
    public Long getHitTotalTXTime() {
        return hitTotalTXTime;
    }
    public Long getMissTotalTimeNoTX() {
        return missTotalNoTXTime;
    }
    public Long getMissTotalTimeTX() {
        return missTotalTXTime;
    }
    public Long getWriteNoTXTotalTime() {
        return writeNoTXTotalTime;
    }
    public Long getWriteTXTotalTime() {
        return writeTXTotalTime;
    }
    public Long getTotalHitsNoTX() {
        return totalHitsNoTX;
    }
    public Long getTotalHitsTX() {
        return totalHitsTX;
    }
    public Long getTotalMissesNoTX() {
        return totalMissesNoTX;
    }
    public Long getTotalMissesTX() {
        return totalMissesTX;
    }
    public Long getTotalWritesNoTX() {
    	return totalWritesNoTx;
    }
    public Long getTotalWritesTX() {
        return totalWritesTX;
    }
    public Long getEvictions() {
        return totalEvictions;
    }
    public Long getTotalRemoveHitsNoTX() {
        return totalRemoveHitsNoTX;
    }
    public Long getTotalRemoveHitsTX() {
        return totalRemoveHitsTX;
    }
    public Long getTotalRemoveMissesNoTX() {
        return totalRemoveMissesNoTX;
    }
    public Long getTotalRemoveMissesTX() {
        return totalRemoveMissesTX;
    }
    public Long getTotalTransactions() {
        return transactions;
    }

    public Long getTotalHitsTXRO() {
		return totalHitsTXRO;
	}

	public Long getTotalMissesTXRO() {
		return totalMissesTXRO;
	}

	public Long getTotalWritesTXRO() {
		return totalWritesTXRO;
	}

	public Long getTotalRemoveHitsTXRO() {
		return totalRemoveHitsTXRO;
	}

	public Long getTotalRemoveMissesTXRO() {
		return totalRemoveMissesTXRO;
	}

	public Transaction getLastTransaction() {
        return lastTransaction;
    }

    public Long getTotalEvictions() {
		return totalEvictions;
	}

	public Long getTotalPreparesRO() {
		return totalPreparesRO;
	}

	public Long getTotalCommitsRO() {
		return totalCommitsRO;
	}

	public Long getTotalRollbacksRO() {
		return totalRollbacksRO;
	}

	public Long getTransactionsRO() {
		return transactionsRO;
	}

	public Long getMissTotalNoTXTime() {
		return missTotalNoTXTime;
	}

	public Long getTotalWritesNoTx() {
		return totalWritesNoTx;
	}

	public Long getHitTotalTXROTime() {
		return hitTotalTXROTime;
	}

	public Long getMissTotalTXROTime() {
		return missTotalTXROTime;
	}

	public Long getWriteTXROTotalTime() {
		return writeTXROTotalTime;
	}

	public Long getMissTotalTXTime() {
		return missTotalTXTime;
	}

	public Long getPrepareTotalTimeRO() {
		return prepareTotalTimeRO;
	}

	public Long getCommitTotalTimeRO() {
		return commitTotalTimeRO;
	}

	public Long getRollbackTotalTimeRO() {
		return rollbackTotalTimeRO;
	}

	public Long getTransactions() {
		return transactions;
	}

	public void setLastTransaction(Transaction lastTransaction) {
        this.lastTransaction = lastTransaction;
    }

    public Thread getThread() {
        return thread;
    }

    public State getThread_state() {
		return thread_state;
	}

	private void resetTempValues(){
        this.tempHitTotalTXTime = (long) 0;
        this.tempMissTotalTXTime = (long) 0;
        this.tempWriteTXTotalTime = (long) 0;
        this.tempTotalHitsTX = (long) 0;
        this.tempTotalMissesTX = (long) 0;
        this.tempTotalWritesTX = (long) 0;
        this.tempTotalRemoveHitsTX = (long) 0;
        this.tempTotalRemoveMissesTX = (long) 0;

        this.tempPrepareTotalTime = (long) 0;
        this.tempCommitTotalTime = (long) 0;
        this.tempRollbackTotalTime = (long) 0;
        this.tempTotalPrepares = (long) 0;
        this.tempTotalCommits = (long) 0;
        this.tempTotalRollbacks = (long) 0;

        this.tempTransactions = (long) 0;

    }
    
    private void writeObject(ObjectOutputStream out) throws IOException{
		//out.defaultWriteObject();
    	out.writeObject(thread.getState());
    	out.writeObject(hitTotalNoTXTime);
		out.writeObject(missTotalNoTXTime);
		out.writeObject(writeNoTXTotalTime);
		out.writeObject(totalHitsNoTX);
		out.writeObject(totalMissesNoTX);
		out.writeObject(totalWritesNoTx);
		out.writeObject(totalRemoveHitsNoTX);
		out.writeObject(totalRemoveMissesNoTX);
		out.writeObject(hitTotalTXTime);
		out.writeObject(missTotalTXTime);
		out.writeObject(writeTXTotalTime);
		out.writeObject(totalHitsTX);
		out.writeObject(totalMissesTX);
		out.writeObject(totalWritesTX);
		out.writeObject(totalRemoveHitsTX);
		out.writeObject(totalRemoveMissesTX);

        out.writeObject(prepareTotalTime);
        out.writeObject(commitTotalTime);
        out.writeObject(rollbackTotalTime);
        out.writeObject(totalPrepares);
        out.writeObject(totalCommits);
        out.writeObject(totalRollbacks);
        out.writeObject(transactions);
        out.writeObject(hitTotalTXROTime);
        out.writeObject(missTotalTXROTime);
        out.writeObject(writeTXROTotalTime);
        out.writeObject(totalHitsTXRO);
        out.writeObject(totalMissesTXRO);
        out.writeObject(totalWritesTXRO);
        out.writeObject(totalRemoveHitsTXRO);
        out.writeObject(totalRemoveMissesTXRO);
        out.writeObject(prepareTotalTimeRO);
        out.writeObject(commitTotalTimeRO);
        out.writeObject(rollbackTotalTimeRO);
        out.writeObject(totalPreparesRO);
        out.writeObject(totalCommitsRO);
        out.writeObject(totalRollbacksRO);
        out.writeObject(transactionsRO);
        out.writeObject(totalEvictions);

		System.out.println("Write InifnispanStatistics Object");
	}
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		//in.defaultReadObject();
		System.out.println("Start Read InifnispanStatistics Object");
		this.thread_state = (State) in.readObject();
		this.hitTotalNoTXTime = (Long) in.readObject();
		this.missTotalNoTXTime = (Long) in.readObject();
		this.writeNoTXTotalTime = (Long) in.readObject();
		this.totalHitsNoTX = (Long) in.readObject();
		this.totalMissesNoTX = (Long) in.readObject();
		this.totalWritesNoTx = (Long) in.readObject();
		this.totalRemoveHitsNoTX = (Long) in.readObject();
		this.totalRemoveMissesNoTX = (Long) in.readObject();
		this.hitTotalTXTime = (Long) in.readObject();
		this.missTotalTXTime = (Long) in.readObject();
		this.writeTXTotalTime = (Long) in.readObject();
		this.totalHitsTX = (Long) in.readObject();
		this.totalMissesTX = (Long) in.readObject();
		this.totalWritesTX = (Long) in.readObject();
		this.totalRemoveHitsTX = (Long) in.readObject();
		this.totalRemoveMissesTX = (Long) in.readObject();

        this.prepareTotalTime = (Long) in.readObject();
        this.commitTotalTime = (Long) in.readObject();
        this.rollbackTotalTime = (Long) in.readObject();
        this.totalPrepares = (Long) in.readObject();
        this.totalCommits = (Long) in.readObject();
        this.totalRollbacks = (Long) in.readObject();
        this.transactions = (Long) in.readObject();
        this.hitTotalTXROTime = (Long) in.readObject();
        this.missTotalTXROTime = (Long) in.readObject();
        this.writeTXROTotalTime = (Long) in.readObject();
        this.totalHitsTXRO = (Long) in.readObject();
        this.totalMissesTXRO = (Long) in.readObject();
        this.totalWritesTXRO = (Long) in.readObject();
        this.totalRemoveHitsTXRO = (Long) in.readObject();
        this.totalRemoveMissesTXRO = (Long) in.readObject();
        this.prepareTotalTimeRO = (Long) in.readObject();
        this.commitTotalTimeRO = (Long) in.readObject();
        this.rollbackTotalTimeRO = (Long) in.readObject();
        this.totalPreparesRO = (Long) in.readObject();
        this.totalCommitsRO = (Long) in.readObject();
        this.totalRollbacksRO = (Long) in.readObject();
        this.transactionsRO = (Long) in.readObject();
        this.totalEvictions = (Long) in.readObject();

		System.out.println("Read InifnispanStatistics Object");
	}
}

