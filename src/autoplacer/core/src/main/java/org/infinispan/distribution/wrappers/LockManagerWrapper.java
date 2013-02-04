/*
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
package org.infinispan.distribution.wrappers;

import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.InvocationContext;
import org.infinispan.context.impl.TxInvocationContext;
import org.infinispan.stats.TransactionsStatisticsRegistry;
import org.infinispan.stats.topK.StreamLibContainer;
import org.infinispan.stats.translations.ExposedStatistics.IspnStats;
import org.infinispan.transaction.xa.GlobalTransaction;
import org.infinispan.util.concurrent.TimeoutException;
import org.infinispan.util.concurrent.locks.LockManager;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

import java.util.Collection;

/**
 * @author Mircea Markus <mircea.markus@jboss.com> (C) 2011 Red Hat Inc.
 * @author Diego Didona <didona@gsd.inesc-id.pt>
 * @author Pedro Ruivo
 * @since 5.2
 */
public class LockManagerWrapper implements LockManager {
   private static final Log log = LogFactory.getLog(LockManagerWrapper.class);

   private final LockManager actual;

   public LockManagerWrapper(LockManager actual) {
      this.actual = actual;
   }

   private boolean updateContentionStats(Object key, TxInvocationContext tctx){
      GlobalTransaction holder = (GlobalTransaction)getOwner(key);
      if(holder!=null){
         GlobalTransaction me = tctx.getGlobalTransaction();
         if(holder!=me){
            if(holder.isRemote()){
               TransactionsStatisticsRegistry.incrementValue(IspnStats.LOCK_CONTENTION_TO_REMOTE);
            } else {
               TransactionsStatisticsRegistry.incrementValue(IspnStats.LOCK_CONTENTION_TO_LOCAL);
            }
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean lockAndRecord(Object key, InvocationContext ctx, long timeoutMillis) throws InterruptedException {
      log.tracef("LockManagerWrapper.lockAndRecord");
      return actual.lockAndRecord(key, ctx, timeoutMillis);
   }

   @Override
   public boolean shareLockAndRecord(Object key, InvocationContext ctx, long timeoutMillis) throws InterruptedException {
      log.tracef("LockManagerWrapper.shareLockAndRecord");
      return actual.shareLockAndRecord(key, ctx, timeoutMillis);
   }

   @Override
   public void unlock(Collection<Object> lockedKeys, Object lockOwner) {
      log.tracef("LockManagerWrapper.unlock");
      actual.unlock(lockedKeys, lockOwner);
   }

   @Override
   public void unlockAll(InvocationContext ctx) {
      log.tracef("LockManagerWrapper.unlockAll");
      actual.unlockAll(ctx);
   }

   @Override
   public boolean ownsLock(Object key, Object owner) {
      log.tracef("LockManagerWrapper.ownsLock");
      return actual.ownsLock(key, owner);
   }

   @Override
   public boolean isLocked(Object key) {
      log.tracef("LockManagerWrapper.isExclusiveLocked");
      return actual.isLocked(key);
   }

   @Override
   public Object getOwner(Object key) {
      log.tracef("LockManagerWrapper.getOwner");
      return actual.getOwner(key);
   }

   @Override
   public String printLockInfo() {
      log.tracef("LockManagerWrapper.printLockInfo");
      return actual.printLockInfo();
   }

   @Override
   public boolean possiblyLocked(CacheEntry entry) {
      log.tracef("LockManagerWrapper.possiblyLocked");
      return actual.possiblyLocked(entry);
   }

   @Override
   public int getNumberOfLocksHeld() {
      log.tracef("LockManagerWrapper.getNumberOfLocksHeld");
      return actual.getNumberOfLocksHeld();
   }

   @Override
   public int getLockId(Object key) {
      log.tracef("LockManagerWrapper.getLockId");
      return actual.getLockId(key);
   }

   @Override
   public boolean acquireLock(InvocationContext ctx, Object key, boolean share) throws InterruptedException, TimeoutException {
      log.tracef("LockManagerWrapper.acquireLock");

      long lockingTime = 0;
      boolean locked,
            experiencedContention = false,
            txScope;

      if(txScope = ctx.isInTxScope()){
         experiencedContention = this.updateContentionStats(key,(TxInvocationContext)ctx);
         lockingTime = System.nanoTime();
      }
      try{
         locked = actual.acquireLock(ctx, key, share);  //this returns false if you already have acquired the lock previously
      }
      catch(TimeoutException e){
         StreamLibContainer.getInstance().addLockInformation(key, experiencedContention, true);
         throw e;
      }
      catch(InterruptedException e){
         StreamLibContainer.getInstance().addLockInformation(key, experiencedContention, true);
         throw e;
      }

      StreamLibContainer.getInstance().addLockInformation(key, experiencedContention, false);

      if(txScope && experiencedContention && locked){
         lockingTime = System.nanoTime() - lockingTime;
         TransactionsStatisticsRegistry.addValue(IspnStats.LOCK_WAITING_TIME,lockingTime);
         TransactionsStatisticsRegistry.incrementValue(IspnStats.NUM_WAITED_FOR_LOCKS);
      }
      if(locked){
         TransactionsStatisticsRegistry.addTakenLock(key); //Idempotent
      }


      return locked;
   }

   @Override
   public boolean acquireLock(InvocationContext ctx, Object key, long timeoutMillis, boolean share) throws InterruptedException,
                                                                                            TimeoutException {
      log.tracef("LockManagerWrapper.acquireLock");
      return actual.acquireLock(ctx, key, timeoutMillis, share);
   }

   @Override
   public boolean acquireLockNoCheck(InvocationContext ctx, Object key) throws InterruptedException, TimeoutException {
      log.tracef("LockManagerWrapper.acquireLockNoCheck");
      return actual.acquireLockNoCheck(ctx, key);
   }
}
