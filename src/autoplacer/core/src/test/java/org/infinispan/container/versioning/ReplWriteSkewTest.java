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

package org.infinispan.container.versioning;

import org.infinispan.Cache;
import org.infinispan.commands.VisitableCommand;
import org.infinispan.commands.tx.CommitCommand;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.context.Flag;
import org.infinispan.context.InvocationContext;
import org.infinispan.context.impl.TxInvocationContext;
import org.infinispan.interceptors.InvocationContextInterceptor;
import org.infinispan.interceptors.base.CommandInterceptor;
import org.infinispan.test.fwk.CleanupAfterMethod;
import org.testng.annotations.Test;

import javax.transaction.RollbackException;
import javax.transaction.Transaction;

import static org.testng.AssertJUnit.assertEquals;
@Test(testName = "container.versioning.ReplWriteSkewTest", groups = "functional")
@CleanupAfterMethod
public class ReplWriteSkewTest extends AbstractClusteredWriteSkewTest {

   @Override
   protected CacheMode getCacheMode() {
      return CacheMode.REPL_SYNC;
   }

   @Override
   protected int clusterSize() {
      return 2;
   }
   
   public void testWriteSkew() throws Exception {
      Cache<Object, Object> cache0 = cache(0);
      Cache<Object, Object> cache1 = cache(1);

      // Auto-commit is true
      cache0.put("hello", "world 1");
      assertEquals(cache0.get("hello"), "world 1");
      assertEventuallyEquals(1, "hello", "world 1");

      tm(0).begin();
      assert "world 1".equals(cache0.get("hello"));
      Transaction t = tm(0).suspend();

      // Induce a write skew
      cache1.put("hello", "world 3");

      assert cache0.get("hello").equals("world 3");
      assert cache1.get("hello").equals("world 3");

      tm(0).resume(t);
      cache0.put("hello", "world 2");

      try {
         tm(0).commit();
         assert false : "Transaction should roll back";
      } catch (RollbackException re) {
         // expected
      }

      assert "world 3".equals(cache0.get("hello"));
      assert "world 3".equals(cache1.get("hello"));

      assertNoTransactions();
   }

   public void testWriteSkewMultiEntries() throws Exception {
      Cache<Object, Object> cache0 = cache(0);
      Cache<Object, Object> cache1 = cache(1);

      tm(0).begin();
      cache0.put("hello", "world 1");
      cache0.put("hello2", "world 1");
      tm(0).commit();

      tm(0).begin();
      cache0.put("hello2", "world 2");
      assert "world 2".equals(cache0.get("hello2"));
      assert "world 1".equals(cache0.get("hello"));
      Transaction t = tm(0).suspend();

      // Induce a write skew
      // Auto-commit is true
      cache1.put("hello", "world 3");

      assert cache0.get("hello").equals("world 3");
      assert cache0.get("hello2").equals("world 1");
      assert cache1.get("hello").equals("world 3");
      assert cache1.get("hello2").equals("world 1");

      tm(0).resume(t);
      cache0.put("hello", "world 2");

      try {
         tm(0).commit();
         assert false : "Transaction should roll back";
      } catch (RollbackException re) {
         // expected
      }

      assert cache0.get("hello").equals("world 3");
      assert cache0.get("hello2").equals("world 1");
      assert cache1.get("hello").equals("world 3");
      assert cache1.get("hello2").equals("world 1");

      assertNoTransactions();
   }

   public void testNullEntries() throws Exception {

      // Auto-commit is true
      cache(0).put("hello", "world");

      assertEquals(cache(0).get("hello"), "world");
      assertEventuallyEquals(1, "hello", "world");


      tm(0).begin();
      assert "world".equals(cache(0).get("hello"));
      Transaction t = tm(0).suspend();

      cache(1).remove("hello");

      assertEventuallyEquals(0, "hello", null);
      assertEquals(cache(0).get("hello"), null);

      tm(0).resume(t);
      cache(0).put("hello", "world2");

      try {
         tm(0).commit();
         assert false : "This transaction should roll back";
      } catch (RollbackException expected) {
         // expected
      }

      assert null == cache(0).get("hello");
      assert null == cache(1).get("hello");

      log.tracef("Local tx for cache %s are ", 0, transactionTable(0).getLocalTransactions());
      log.tracef("Remote tx for cache %s are ", 0, transactionTable(0).getRemoteTransactions());
      log.tracef("Local tx for cache %s are ", 1, transactionTable(1).getLocalTransactions());
      log.tracef("Remote tx for cache %s are ", 0, transactionTable(1).getRemoteTransactions());

      assertNoTransactions();
   }

   public void testResendPrepare() throws Exception {
      Cache<Object, Object> cache0 = cache(0);
      Cache<Object, Object> cache1 = cache(1);

      // Auto-commit is true
      cache0.put("hello", "world");

      // create a write skew
      tm(0).begin();
      assert "world".equals(cache0.get("hello"));
      assertEventuallyEquals(1, "hello", "world");


      Transaction t = tm(0).suspend();
      // Set up cache-1 to force the prepare to retry
      cache(1).getAdvancedCache().addInterceptorAfter(new CommandInterceptor() {
         boolean used = false;
         @Override
         public Object visitCommitCommand(TxInvocationContext ctx, CommitCommand c) throws Throwable {
            if (!used) {
               used = true;
               log.trace("Force resend of prepare!");
               return CommitCommand.RESEND_PREPARE;
            } else {
               return invokeNextInterceptor(ctx, c);
            }
         }
         @Override
         protected Object handleDefault(InvocationContext ctx, VisitableCommand command) throws Throwable {
            return super.handleDefault(ctx, command);
         }
      }, InvocationContextInterceptor.class);

      // Implicit tx.  Prepare should be retried.
      cache(0).put("hello", "world2");

      assert "world2".equals(cache0.get("hello"));
      assertEventuallyEquals(1, "hello", "world2");

      tm(0).resume(t);
      cache0.put("hello", "world3");

      try {
         log.warn("----- Now committing ---- ");
         tm(0).commit();
         assert false : "This transaction should roll back";
      } catch (RollbackException expected) {
         // expected
         expected
               .printStackTrace();
      }

      assert "world2".equals(cache0.get("hello"));
      assert "world2".equals(cache1.get("hello"));

      assertNoTransactions();
   }

   public void testLocalOnlyPut() {
      localOnlyPut(this.<Integer, String>cache(0), 1, "v1");
      localOnlyPut(this.<Integer, String>cache(1), 2, "v2");
      assertNoTransactions();
   }

   private void localOnlyPut(Cache<Integer, String> cache, Integer k, String v) {
      cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).put(k, v);
   }

}
