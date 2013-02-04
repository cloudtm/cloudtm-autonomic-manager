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
package org.infinispan.tx.totalorder;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.VersioningScheme;
import org.infinispan.container.versioning.ReplWriteSkewTest;
import org.infinispan.test.fwk.CleanupAfterMethod;
import org.infinispan.transaction.TransactionProtocol;
import org.infinispan.util.concurrent.IsolationLevel;
import org.testng.annotations.Test;

import javax.transaction.Transaction;

import static org.testng.Assert.assertEquals;

/**
 * @author Mircea Markus <mircea.markus@jboss.com> (C) 2011 Red Hat Inc.
 * @since 5.2
 */
@Test (groups = "functional", testName = "tx.totalorder.TotalOrderWriteSkewTest")
@CleanupAfterMethod
public class TotalOrderWriteSkewTest extends ReplWriteSkewTest {

   @Override
   protected void decorate(ConfigurationBuilder builder) {
      builder.transaction().transactionProtocol(TransactionProtocol.TOTAL_ORDER);
      builder.locking().isolationLevel(IsolationLevel.REPEATABLE_READ).writeSkewCheck(true)
            .versioning().enable().scheme(VersioningScheme.SIMPLE);
   }

   public void transactionCleanupWithWriteSkew() throws Exception {
      cache(0).put("k","v");
      tm(0).begin();
      assertEquals("v", cache(0).get("k"));
      cache(0).put("k","v2");
      Transaction suspend = tm(0).suspend();

      cache(0).put("k","v3");
      tm(0).resume(suspend);
      try {
         tm(0).commit();
         assert false;
      } catch (Throwable e) {
         //expected
         e.printStackTrace();
      }
      assertEquals("v3", cache(0).get("k"));
      assertEventuallyEquals(1,  "k", "v3");
      assertNoTransactions();
   }
}

