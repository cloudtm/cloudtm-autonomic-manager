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

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.VersioningScheme;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.statetransfer.StateTransferFunctionalTest;
import org.infinispan.test.fwk.TransportFlags;
import org.infinispan.transaction.TransactionProtocol;
import org.infinispan.util.concurrent.IsolationLevel;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Mircea Markus <mircea.markus@jboss.com> (C) 2011 Red Hat Inc.
 * @since 5.2
 */
@Test(groups = "functional", testName = "tx.totalorder.DistTotalOrderStateTransfer2PcTest")
public class DistTotalOrderStateTransfer2PcTest extends StateTransferFunctionalTest {

   private ConfigurationBuilder dcc;

   protected void createCacheManagers() throws Throwable {
      dcc = getDefaultClusteredCacheConfig(CacheMode.DIST_SYNC, true);
      dcc.transaction().transactionProtocol(TransactionProtocol.TOTAL_ORDER);
      dcc.locking().isolationLevel(IsolationLevel.REPEATABLE_READ).writeSkewCheck(true)
            .versioning().enable().scheme(VersioningScheme.SIMPLE);
      dcc.clustering().stateTransfer().fetchInMemoryState(true);
      dcc.clustering().l1().disable();
   }

   protected EmbeddedCacheManager createCacheManager() {
      EmbeddedCacheManager cm = addClusterEnabledCacheManager(new TransportFlags().withMerge(true));
      cm.defineConfiguration(cacheName, dcc.build());
      return cm;
   }

   public void testClusterFormation() {
      EmbeddedCacheManager cm0 = createCacheManager();
      EmbeddedCacheManager cm1 = createCacheManager();
      final Cache<Object, Object> cache0 = cm0.getCache(cacheName);
      final Cache<Object, Object> cache1 = cm1.getCache(cacheName);
      cache0.put("k", "v");
      assertEquals(cache0.get("k"), "v");
      eventually(new Condition() {
         @Override
         public boolean isSatisfied() throws Exception {
            return "v".equals(cache1.get("k"));
         }
      });
   }
}
