/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */

package org.infinispan.util.concurrent.locks.containers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public abstract class AbstractLockContainer<L extends Lock> implements LockContainer<L> {

   /**
    * Releases a lock and swallows any IllegalMonitorStateExceptions - so it is safe to call this method even if the
    * lock is not locked, or not locked by the current thread.
    *
    * @param toRelease lock to release
    */
   protected void safeExclusiveRelease(L toRelease, Object lockOwner) {
      if (toRelease != null) {
         try {
            unlockExclusive(toRelease, lockOwner);
         } catch (IllegalMonitorStateException imse) {
            // Perhaps the caller hadn't acquired the lock after all.
         }
      }
   }

   protected void safeShareRelease(L toRelease, Object lockOwner) {
      if (toRelease != null) {
         try {
            unlockShare(toRelease, lockOwner);
         } catch (IllegalMonitorStateException imse) {
            // Perhaps the caller hadn't acquired the lock after all.
         }
      }
   }

   protected abstract void unlockExclusive(L toRelease, Object owner);

   protected abstract void unlockShare(L toRelease, Object owner);

   protected abstract boolean tryExclusiveLock(L lock, long timeout, TimeUnit unit, Object lockOwner) throws InterruptedException;      

   protected abstract boolean tryShareLock(L lock, long timeout, TimeUnit unit, Object lockOwner) throws InterruptedException;      

   @Override
   public boolean ownsShareLock(Object key, Object owner) {
      return false;
   }

   @Override
   public boolean isSharedLocked(Object key) {
      return false;
   }

   @Override
   public L getShareLock(Object key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public L acquireShareLock(Object lockOwner, Object key, long timeout, TimeUnit unit) throws InterruptedException {
      throw new UnsupportedOperationException();
   }

   @Override
   public void releaseShareLock(Object lockOwner, Object key) {
      //no-op
   }
}
