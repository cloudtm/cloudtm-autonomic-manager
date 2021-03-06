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
package org.infinispan.configuration.cache;

public class Configuration {

   private final ClassLoader classLoader; //TODO remove this
   private final ClusteringConfiguration clusteringConfiguration;
   private final CustomInterceptorsConfiguration customInterceptorsConfiguration;
   private final DataContainerConfiguration dataContainerConfiguration;
   private final DeadlockDetectionConfiguration deadlockDetectionConfiguration;
   private final EvictionConfiguration evictionConfiguration;
   private final ExpirationConfiguration expirationConfiguration;
   private final IndexingConfiguration indexingConfiguration;
   private final InvocationBatchingConfiguration invocationBatchingConfiguration;
   private final JMXStatisticsConfiguration jmxStatisticsConfiguration;
   private final LoadersConfiguration loadersConfiguration;
   private final LockingConfiguration lockingConfiguration;
   private final StoreAsBinaryConfiguration storeAsBinaryConfiguration;
   private final TransactionConfiguration transactionConfiguration;
   private final VersioningConfiguration versioningConfiguration;
   private final UnsafeConfiguration unsafeConfiguration;
   private final DataPlacementConfiguration dataPlacementConfiguration;
   private final GarbageCollectorConfiguration garbageCollectorConfiguration;
   private final ConditionalExecutorServiceConfiguration conditionalExecutorServiceConfiguration;

   Configuration(ClusteringConfiguration clusteringConfiguration,
                 CustomInterceptorsConfiguration customInterceptorsConfiguration,
                 DataContainerConfiguration dataContainerConfiguration, DeadlockDetectionConfiguration deadlockDetectionConfiguration,
                 EvictionConfiguration evictionConfiguration, ExpirationConfiguration expirationConfiguration,
                 IndexingConfiguration indexingConfiguration, InvocationBatchingConfiguration invocationBatchingConfiguration,
                 JMXStatisticsConfiguration jmxStatisticsConfiguration,
                 LoadersConfiguration loadersConfiguration,
                 LockingConfiguration lockingConfiguration, StoreAsBinaryConfiguration storeAsBinaryConfiguration,
                 TransactionConfiguration transactionConfiguration, UnsafeConfiguration unsafeConfiguration,
                 VersioningConfiguration versioningConfiguration, ClassLoader cl, DataPlacementConfiguration dataPlacementConfiguration, GarbageCollectorConfiguration garbageCollectorConfiguration, ConditionalExecutorServiceConfiguration conditionalExecutorServiceConfiguration) {
      this.clusteringConfiguration = clusteringConfiguration;
      this.customInterceptorsConfiguration = customInterceptorsConfiguration;
      this.dataContainerConfiguration = dataContainerConfiguration;
      this.deadlockDetectionConfiguration = deadlockDetectionConfiguration;
      this.evictionConfiguration = evictionConfiguration;
      this.expirationConfiguration = expirationConfiguration;
      this.indexingConfiguration = indexingConfiguration;
      this.invocationBatchingConfiguration = invocationBatchingConfiguration;
      this.jmxStatisticsConfiguration = jmxStatisticsConfiguration;
      this.loadersConfiguration = loadersConfiguration;
      this.lockingConfiguration = lockingConfiguration;
      this.storeAsBinaryConfiguration = storeAsBinaryConfiguration;
      this.transactionConfiguration = transactionConfiguration;
      this.unsafeConfiguration = unsafeConfiguration;
      this.versioningConfiguration = versioningConfiguration;
      this.classLoader = cl;
      this.dataPlacementConfiguration = dataPlacementConfiguration;
      this.garbageCollectorConfiguration = garbageCollectorConfiguration;
      this.conditionalExecutorServiceConfiguration = conditionalExecutorServiceConfiguration;
   }

   /**
    * Will be removed with no replacement
    * @return
    */
   @Deprecated
   public ClassLoader classLoader() {
      return classLoader;
   }

   public ClusteringConfiguration clustering() {
      return clusteringConfiguration;
   }

   public CustomInterceptorsConfiguration customInterceptors() {
      return customInterceptorsConfiguration;
   }

   public DataContainerConfiguration dataContainer() {
      return dataContainerConfiguration;
   }

   public DeadlockDetectionConfiguration deadlockDetection() {
      return deadlockDetectionConfiguration;
   }

   public EvictionConfiguration eviction() {
      return evictionConfiguration;
   }

   public ExpirationConfiguration expiration() {
      return expirationConfiguration;
   }

   public IndexingConfiguration indexing() {
      return indexingConfiguration;
   }

   public InvocationBatchingConfiguration invocationBatching() {
      return invocationBatchingConfiguration;
   }

   public JMXStatisticsConfiguration jmxStatistics() {
      return jmxStatisticsConfiguration;
   }

   public LoadersConfiguration loaders() {
      return loadersConfiguration;
   }

   public LockingConfiguration locking() {
      return lockingConfiguration;
   }

   public StoreAsBinaryConfiguration storeAsBinary() {
      return storeAsBinaryConfiguration;
   }

   public TransactionConfiguration transaction() {
      return transactionConfiguration;
   }

   public UnsafeConfiguration unsafe() {
      return unsafeConfiguration;
   }

   public VersioningConfiguration versioning() {
      return versioningConfiguration;
   }

   public DataPlacementConfiguration dataPlacement() {
      return dataPlacementConfiguration;
   }

   public GarbageCollectorConfiguration garbageCollector() {
      return garbageCollectorConfiguration;
   }

   public ConditionalExecutorServiceConfiguration conditionalExecutorService() {
      return conditionalExecutorServiceConfiguration;
   }

   @Override
   public String toString() {
      return "Configuration{" +
            "classLoader=" + classLoader +
            ", clustering=" + clusteringConfiguration +
            ", customInterceptors=" + customInterceptorsConfiguration +
            ", dataContainer=" + dataContainerConfiguration +
            ", deadlockDetection=" + deadlockDetectionConfiguration +
            ", eviction=" + evictionConfiguration +
            ", expiration=" + expirationConfiguration +
            ", indexing=" + indexingConfiguration +
            ", invocationBatching=" + invocationBatchingConfiguration +
            ", jmxStatistics=" + jmxStatisticsConfiguration +
            ", loaders=" + loadersConfiguration +
            ", locking=" + lockingConfiguration +
            ", storeAsBinary=" + storeAsBinaryConfiguration +
            ", transaction=" + transactionConfiguration +
            ", versioning=" + versioningConfiguration +
            ", unsafe=" + unsafeConfiguration +
            ", dataPlacement=" + dataPlacementConfiguration +
            ", garbageCollector=" + garbageCollectorConfiguration +
            ", conditionalExecutorServiceConfiguration=" + conditionalExecutorServiceConfiguration +
            '}';
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Configuration that = (Configuration) o;

      if (classLoader != null ? !classLoader.equals(that.classLoader) : that.classLoader != null)
         return false;
      if (clusteringConfiguration != null ? !clusteringConfiguration.equals(that.clusteringConfiguration) : that.clusteringConfiguration != null)
         return false;
      if (customInterceptorsConfiguration != null ? !customInterceptorsConfiguration.equals(that.customInterceptorsConfiguration) : that.customInterceptorsConfiguration != null)
         return false;
      if (dataContainerConfiguration != null ? !dataContainerConfiguration.equals(that.dataContainerConfiguration) : that.dataContainerConfiguration != null)
         return false;
      if (deadlockDetectionConfiguration != null ? !deadlockDetectionConfiguration.equals(that.deadlockDetectionConfiguration) : that.deadlockDetectionConfiguration != null)
         return false;
      if (evictionConfiguration != null ? !evictionConfiguration.equals(that.evictionConfiguration) : that.evictionConfiguration != null)
         return false;
      if (expirationConfiguration != null ? !expirationConfiguration.equals(that.expirationConfiguration) : that.expirationConfiguration != null)
         return false;
      if (indexingConfiguration != null ? !indexingConfiguration.equals(that.indexingConfiguration) : that.indexingConfiguration != null)
         return false;
      if (invocationBatchingConfiguration != null ? !invocationBatchingConfiguration.equals(that.invocationBatchingConfiguration) : that.invocationBatchingConfiguration != null)
         return false;
      if (jmxStatisticsConfiguration != null ? !jmxStatisticsConfiguration.equals(that.jmxStatisticsConfiguration) : that.jmxStatisticsConfiguration != null)
         return false;
      if (loadersConfiguration != null ? !loadersConfiguration.equals(that.loadersConfiguration) : that.loadersConfiguration != null)
         return false;
      if (lockingConfiguration != null ? !lockingConfiguration.equals(that.lockingConfiguration) : that.lockingConfiguration != null)
         return false;
      if (storeAsBinaryConfiguration != null ? !storeAsBinaryConfiguration.equals(that.storeAsBinaryConfiguration) : that.storeAsBinaryConfiguration != null)
         return false;
      if (transactionConfiguration != null ? !transactionConfiguration.equals(that.transactionConfiguration) : that.transactionConfiguration != null)
         return false;
      if (unsafeConfiguration != null ? !unsafeConfiguration.equals(that.unsafeConfiguration) : that.unsafeConfiguration != null)
         return false;
      if (versioningConfiguration != null ? !versioningConfiguration.equals(that.versioningConfiguration) : that.versioningConfiguration != null)
         return false;
      if (dataPlacementConfiguration != null ? !dataPlacementConfiguration.equals(that.dataPlacementConfiguration) :
            that.dataContainerConfiguration != null)
         return false;
      if (garbageCollectorConfiguration != null ? !garbageCollectorConfiguration.equals(that.garbageCollectorConfiguration) :
      that.garbageCollectorConfiguration != null)
         return false;
      if (conditionalExecutorServiceConfiguration != null ?
            !conditionalExecutorServiceConfiguration.equals(that.conditionalExecutorServiceConfiguration) :
            that.conditionalExecutorServiceConfiguration != null)
         return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = classLoader != null ? classLoader.hashCode() : 0;
      result = 31 * result + (clusteringConfiguration != null ? clusteringConfiguration.hashCode() : 0);
      result = 31 * result + (customInterceptorsConfiguration != null ? customInterceptorsConfiguration.hashCode() : 0);
      result = 31 * result + (dataContainerConfiguration != null ? dataContainerConfiguration.hashCode() : 0);
      result = 31 * result + (deadlockDetectionConfiguration != null ? deadlockDetectionConfiguration.hashCode() : 0);
      result = 31 * result + (evictionConfiguration != null ? evictionConfiguration.hashCode() : 0);
      result = 31 * result + (expirationConfiguration != null ? expirationConfiguration.hashCode() : 0);
      result = 31 * result + (indexingConfiguration != null ? indexingConfiguration.hashCode() : 0);
      result = 31 * result + (invocationBatchingConfiguration != null ? invocationBatchingConfiguration.hashCode() : 0);
      result = 31 * result + (jmxStatisticsConfiguration != null ? jmxStatisticsConfiguration.hashCode() : 0);
      result = 31 * result + (loadersConfiguration != null ? loadersConfiguration.hashCode() : 0);
      result = 31 * result + (lockingConfiguration != null ? lockingConfiguration.hashCode() : 0);
      result = 31 * result + (storeAsBinaryConfiguration != null ? storeAsBinaryConfiguration.hashCode() : 0);
      result = 31 * result + (transactionConfiguration != null ? transactionConfiguration.hashCode() : 0);
      result = 31 * result + (versioningConfiguration != null ? versioningConfiguration.hashCode() : 0);
      result = 31 * result + (unsafeConfiguration != null ? unsafeConfiguration.hashCode() : 0);
      result = 31 * result + (dataPlacementConfiguration != null ? dataPlacementConfiguration.hashCode() : 0);
      result = 31 * result + (garbageCollectorConfiguration != null ? garbageCollectorConfiguration.hashCode() : 0);
      result = 31 * result + (conditionalExecutorServiceConfiguration != null ?
                                    conditionalExecutorServiceConfiguration.hashCode() : 0);
      return result;
   }

}
