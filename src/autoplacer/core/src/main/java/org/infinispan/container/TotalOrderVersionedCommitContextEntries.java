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
package org.infinispan.container;

import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.entries.ClusteredRepeatableReadEntry;
import org.infinispan.container.versioning.EntryVersion;
import org.infinispan.container.versioning.IncrementableEntryVersion;
import org.infinispan.container.versioning.VersionGenerator;
import org.infinispan.context.InvocationContext;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 4.0
 */
public class TotalOrderVersionedCommitContextEntries extends NonVersionedCommitContextEntries {

   private final Log log = LogFactory.getLog(TotalOrderVersionedCommitContextEntries.class);
   private VersionGenerator versionGenerator;

   @Inject
   public final void injectVersionGenerator(VersionGenerator versionGenerator) {
      this.versionGenerator = versionGenerator;
   }

   @Override
   protected Log getLog() {
      return log;
   }

   @Override
   protected void commitContextEntry(CacheEntry entry, InvocationContext ctx, boolean skipOwnershipCheck) {
      if (ctx.isInTxScope()) {
         ClusteredRepeatableReadEntry clusterMvccEntry = (ClusteredRepeatableReadEntry) entry;
         EntryVersion existingVersion = clusterMvccEntry.getVersion();

         EntryVersion newVersion;
         if (existingVersion == null) {
            newVersion = versionGenerator.generateNew();
         } else {
            newVersion = versionGenerator.increment((IncrementableEntryVersion) existingVersion);
         }
         commitEntry(entry, newVersion, skipOwnershipCheck);
      } else {
         // This could be a state transfer call!
         commitEntry(entry, entry.getVersion(), skipOwnershipCheck);
      }
   }
}
