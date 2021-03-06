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
package org.infinispan.configuration.parsing;

import org.infinispan.config.ConfigurationException;
import org.infinispan.configuration.cache.AbstractLoaderConfigurationBuilder;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.FileCacheStoreConfigurationBuilder;
import org.infinispan.configuration.cache.IndexingConfigurationBuilder;
import org.infinispan.configuration.cache.InterceptorConfiguration.Position;
import org.infinispan.configuration.cache.InterceptorConfigurationBuilder;
import org.infinispan.configuration.cache.LoaderConfigurationBuilder;
import org.infinispan.configuration.cache.VersioningScheme;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.ShutdownHookBehavior;
import org.infinispan.configuration.global.TransportConfigurationBuilder;
import org.infinispan.container.DataContainer;
import org.infinispan.dataplacement.lookup.ObjectLookupFactory;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.distribution.group.Grouper;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionThreadPolicy;
import org.infinispan.executors.ExecutorFactory;
import org.infinispan.executors.ScheduledExecutorFactory;
import org.infinispan.interceptors.base.CommandInterceptor;
import org.infinispan.jmx.MBeanServerLookup;
import org.infinispan.loaders.CacheLoader;
import org.infinispan.loaders.file.FileCacheStore;
import org.infinispan.marshall.AdvancedExternalizer;
import org.infinispan.marshall.Marshaller;
import org.infinispan.remoting.ReplicationQueue;
import org.infinispan.remoting.transport.Transport;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.TransactionProtocol;
import org.infinispan.transaction.lookup.TransactionManagerLookup;
import org.infinispan.util.FileLookup;
import org.infinispan.util.FileLookupFactory;
import org.infinispan.util.StringPropertyReplacer;
import org.infinispan.util.Util;
import org.infinispan.util.concurrent.IsolationLevel;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.infinispan.configuration.cache.CacheMode.*;

public class Parser {

   private static final Log log = LogFactory.getLog(Parser.class);

   private static void safeClose(final Closeable closeable) {
      if (closeable != null) try {
         closeable.close();
      } catch (Throwable t) {
         log.failedToCloseResource(t);
      }
   }

   private static String replaceSystemProperties(String value) {
      int dollar = value.indexOf('$');
      if (dollar >= 0 && value.indexOf('{', dollar) > 0 && value.indexOf('}', dollar) > 0) {
         String replacedValue = StringPropertyReplacer.replaceProperties(value);
         if (value.equals(replacedValue)) {
            log.propertyCouldNotBeReplaced(value.substring(value.indexOf('{') + 1, value.indexOf('}')));
         }
         return replacedValue;
      } else {
         return value;
      }
   }

   private final ClassLoader cl;

   public Parser(ClassLoader cl) {
      this.cl = cl;
   }

   public ConfigurationBuilderHolder parseFile(String filename) {
      return parseFiles(Collections.singletonList(filename));
   }

   /**
    * This will parse all the filenames in order overriding at each the global
    * and default cache settings.  Once all the default cache settings are
    * read, then the named caches for each file will be applied as a named cache
    * with the default cache settings as a base going through each file.
    *
    * @param filenames The file names, each might be the name of the file (too
    *        look it up in the class path) or an url to a file.
    * @return ConfigurationBuilderHolder with all the values applied and
    *         overridden according to ordering of files
    */
   public ConfigurationBuilderHolder parseFiles(List<String> filenames) {
      FileLookup fileLookup = FileLookupFactory.newInstance();
      List<InputStream> streams = new ArrayList<InputStream>(filenames.size());
      for (String filename : filenames) {
         streams.add(fileLookup.lookupFile(filename, cl));
      }
      return parse(streams);
   }

   public ConfigurationBuilderHolder parse(InputStream is) {
      return parse(Collections.singletonList(is));
   }

   /**
    * This will parse all the streams in order overriding at each the global
    * and default cache settings.  Once all the default cache settings are
    * read, then the named caches for each stream will be applied as a named cache
    * in order with the default cache settings as a base going through each file.
    *
    * @param streams The streams each containing data pertaining to an infinispan
    *        configuration xml file
    * @return ConfigurationBuilderHolder with all the values applied and
    *         overridden according to ordering of streams
    */
   public ConfigurationBuilderHolder parse(List<? extends InputStream> streams) {
      try {
         List<XMLStreamReader> streamReaders = new ArrayList<XMLStreamReader>(
               streams.size());
         try {
            for (InputStream is : streams) {
               BufferedInputStream input = new BufferedInputStream(is);
               XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(input);
               streamReaders.add(streamReader);
            }
            ConfigurationBuilderHolder holder = doParse(streamReaders);
            for (XMLStreamReader reader : streamReaders) {
               reader.close();
            }
            return holder;
         }
         finally {
            for (InputStream is : streams) {
               safeClose(is);
            }
         }
      } catch (ConfigurationException e) {
         throw e;
      } catch (Exception e) {
         throw new ConfigurationException(e);
      }
   }

   private ConfigurationBuilderHolder doParse(Iterable<? extends XMLStreamReader> readers) throws XMLStreamException {
      ConfigurationBuilderHolder holder = new ConfigurationBuilderHolder();

      for (XMLStreamReader reader : readers) {
         Element root = ParseUtils.nextElement(reader);

         if (!root.getLocalName().equals(Element.ROOT.getLocalName())) {
            throw ParseUtils.missingRequiredElement(reader, Collections.singleton(Element.ROOT));
         }

         boolean onNamedCaches = false;
         while (!onNamedCaches && reader.hasNext() &&
               (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
            Element element = Element.forName(reader.getLocalName());
            switch (element) {
               case DEFAULT: {
                  parseDefaultCache(reader, holder.getDefaultConfigurationBuilder());
                  break;
               }
               case GLOBAL: {
                  parseGlobal(reader, holder.getGlobalConfigurationBuilder());
                  break;
               }
               case NAMED_CACHE: {
                  onNamedCaches = true;
                  break;
               }
               default: {
                  throw ParseUtils.unexpectedElement(reader);
               }
            }
         }
      }

      for (XMLStreamReader reader : readers) {
         // If this reader was previously on a named cache now apply them
         // all after the default was parsed
         if (Element.forName(reader.getLocalName()) == Element.NAMED_CACHE) {
            // Parse the previously found named cache
            parseNamedCache(reader, holder);

            while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
               Element element = Element.forName(reader.getLocalName());
               switch (element) {
                  // We should only have named caches now
                  case NAMED_CACHE: {
                     parseNamedCache(reader, holder);
                     break;
                  }
                  default: {
                     throw ParseUtils.unexpectedElement(reader);
                  }
               }
            }
         }
      }
      return holder;
   }

   private void parseNamedCache(XMLStreamReader reader, ConfigurationBuilderHolder holder) throws XMLStreamException {

      ParseUtils.requireSingleAttribute(reader, Attribute.NAME.getLocalName());

      String name = "";

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case NAME:
               name = value;
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      // Reuse the builder if it was made before
      ConfigurationBuilder builder = holder.getNamedConfigurationBuilders().get(name);
      if (builder == null) {
         builder = holder.newConfigurationBuilder(name);
      }
      parseCache(reader, builder);

   }

   private void parseDefaultCache(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      ParseUtils.requireNoAttributes(reader);
      parseCache(reader, builder);
   }

   private void parseCache(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case CLUSTERING:
               parseClustering(reader, builder);
               break;
            case CUSTOM_INTERCEPTORS:
               parseCustomInterceptors(reader, builder);
               break;
            case DATA_CONTAINER:
               parseDataContainer(reader, builder);
               break;
            case DEADLOCK_DETECTION:
               parseDeadlockDetection(reader, builder);
               break;
            case EVICTION:
               parseEviction(reader, builder);
               break;
            case EXPIRATION:
               parseExpiration(reader, builder);
               break;
            case INDEXING:
               parseIndexing(reader, builder);
               break;
            case INVOCATION_BATCHING:
               parseInvocationBatching(reader, builder);
               break;
            case JMX_STATISTICS:
               parseJmxStatistics(reader, builder);
               break;
            case LOADERS:
               parseLoaders(reader, builder);
               break;
            case LOCKING:
               parseLocking(reader, builder);
               break;
            case LAZY_DESERIALIZATION:
            case STORE_AS_BINARY:
               parseStoreAsBinary(reader, builder);
               break;
            case TRANSACTION:
               parseTransaction(reader, builder);
               break;
            case UNSAFE:
               parseUnsafe(reader, builder);
               break;
            case VERSIONING:
               parseVersioning(reader, builder);
               break;
            case DATA_PLACEMENT:
               parseDataPlacement(reader, builder);
               break;
            case GARBAGE_COLLECTOR:
               parseGarbageCollector(reader, builder);
               break;
            case CONDITIONAL_EXECUTOR_SERVICE:
               parseConditionalExecutorService(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }
   }

   private void parseConditionalExecutorService(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case CORE_POOL_SIZE:
               builder.conditionalExecutorService().corePoolSize(Integer.parseInt(value));
               break;
            case MAX_POOL_SIZE:
               builder.conditionalExecutorService().maxPoolSize(Integer.parseInt(value));
               break;
            case KEEP_ALIVE_TIME:
               builder.conditionalExecutorService().keepAliveTime(Long.parseLong(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      ParseUtils.requireNoContent(reader);
   }

   private void parseGarbageCollector(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               builder.garbageCollector().enabled(Boolean.valueOf(value));
               break;
            case TRANSACTION_THRESHOLD:
               builder.garbageCollector().transactionThreshold(Integer.parseInt(value));
               break;
            case VERSION_GC_MAX_IDLE:
               builder.garbageCollector().versionGCMaxIdle(Integer.parseInt(value));
               break;
            case L1_GC_INTERVAL:
               builder.garbageCollector().l1GCInterval(Integer.parseInt(value));
               break;
            case VIEW_GC_BACK_OFF:
               builder.garbageCollector().viewGCBackOff(Integer.parseInt(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      ParseUtils.requireNoContent(reader);
   }

   private void parseDataPlacement(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case OBJECT_LOOKUP_FACTORY:
               ObjectLookupFactory objectLookupFactory = Util.getInstance(value, cl);
               builder.dataPlacement().objectLookupFactory(objectLookupFactory);
               break;
            case ENABLED:
               if (Boolean.parseBoolean(value)) {
                  builder.dataPlacement().enable();
               } else {
                  builder.dataPlacement().disable();
               }
               break;
            case COOL_DOWN_TIME:
               try {
                  int coolDownTime = Integer.parseInt(value);
                  builder.dataPlacement().coolDownTime(coolDownTime);
               }  catch (NumberFormatException nfe) {
                  log.warn("Cannot parse the cool down time value. Setting to default");
               }
               break;
            case MAX_KEYS_TO_REQUEST:
               try {
                  int maxNumberOfKeys = Integer.parseInt(value);
                  builder.dataPlacement().maxNumberOfKeysToRequest(maxNumberOfKeys);
               }  catch (NumberFormatException nfe) {
                  log.warn("Cannot parse the max number of key to request value. Setting to default");
               }
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      Properties dataPlacementProperties = null;
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES:
               dataPlacementProperties = parseProperties(reader);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }

      if (dataPlacementProperties != null) {
         builder.dataPlacement().withProperties(dataPlacementProperties);
      }
   }

   private void parseVersioning(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      builder.versioning().disable(); // Disabled by default.
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case VERSIONING_SCHEME:
               builder.versioning().scheme(VersioningScheme.valueOf(value));
               break;
            case ENABLED:
               builder.versioning().enable();
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);
   }
   private void parseTransaction(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      boolean forceSetTransactional = false;
      boolean transactionModeSpecified = false;
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case AUTO_COMMIT:
               builder.transaction().autoCommit(Boolean.parseBoolean(value));
               break;
            case CACHE_STOP_TIMEOUT:
               builder.transaction().cacheStopTimeout(Long.parseLong(value));
               break;
            case EAGER_LOCK_SINGLE_NODE:
               builder.transaction().eagerLockingSingleNode(Boolean.parseBoolean(value));
               break;
            case LOCKING_MODE:
               builder.transaction().lockingMode(LockingMode.valueOf(value));
               break;
            case SYNC_COMMIT_PHASE:
               builder.transaction().syncCommitPhase(Boolean.parseBoolean(value));
               break;
            case SYNC_ROLLBACK_PHASE:
               builder.transaction().syncRollbackPhase(Boolean.parseBoolean(value));
               break;
            case TRANSACTION_MANAGER_LOOKUP_CLASS:
               builder.transaction().transactionManagerLookup(Util.<TransactionManagerLookup>getInstance(value, cl));
               forceSetTransactional = true;
               break;
            case TRANSACTION_MODE:
               builder.transaction().transactionMode(TransactionMode.valueOf(value));
               transactionModeSpecified = true;
               break;
            case USE_EAGER_LOCKING:
               builder.transaction().useEagerLocking(Boolean.parseBoolean(value));
               break;
            case USE_SYNCHRONIZAION:
               builder.transaction().useSynchronization(Boolean.parseBoolean(value));
               break;
            case USE_1PC_FOR_AUTOCOMMIT_TX:
               builder.transaction().use1PcForAutoCommitTransactions(Boolean.parseBoolean(value));
               break;
            case TRANSACTION_PROTOCOL:
               builder.transaction().transactionProtocol(TransactionProtocol.valueOf(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      if (!transactionModeSpecified && forceSetTransactional) builder.transaction().transactionMode(TransactionMode.TRANSACTIONAL);

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case RECOVERY:
               parseRecovery(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }

   }

   private void parseRecovery(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.parseBoolean(value))
                  builder.transaction().recovery().enable();
               else
                  builder.transaction().recovery().disable();
               break;
            case RECOVERY_INFO_CACHE_NAME:
               builder.transaction().recovery().recoveryInfoCacheName(value);
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);
   }

   private void parseUnsafe(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case UNRELIABLE_RETURN_VALUES:
               builder.unsafe().unreliableReturnValues(Boolean.parseBoolean(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseStoreAsBinary(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.parseBoolean(value))
                  builder.storeAsBinary().enable();
               else
                  builder.storeAsBinary().disable();
               break;
            case STORE_KEYS_AS_BINARY:
               builder.storeAsBinary().storeKeysAsBinary(Boolean.parseBoolean(value));
               break;
            case STORE_VALUES_AS_BINARY:
               builder.storeAsBinary().storeValuesAsBinary(Boolean.parseBoolean(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseLocking(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case CONCURRENCY_LEVEL:
               builder.locking().concurrencyLevel(Integer.parseInt(value));
               break;
            case ISOLATION_LEVEL:
               builder.locking().isolationLevel(IsolationLevel.valueOf(value));
               break;
            case LOCK_ACQUISITION_TIMEOUT:
               builder.locking().lockAcquisitionTimeout(Long.parseLong(value));
               break;
            case USE_LOCK_STRIPING:
               builder.locking().useLockStriping(Boolean.parseBoolean(value));
               break;
            case WRITE_SKEW_CHECK:
               builder.locking().writeSkewCheck(Boolean.parseBoolean(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseLoaders(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case PASSIVATION:
               builder.loaders().passivation(Boolean.parseBoolean(value));
               break;
            case PRELOAD:
               builder.loaders().preload(Boolean.parseBoolean(value));
               break;
            case SHARED:
               builder.loaders().shared(Boolean.parseBoolean(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case LOADER:
               parseLoader(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }
   }

   private void parseLoader(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      CacheLoader loader = null;
      Boolean fetchPersistentState = null;
      Boolean ignoreModifications = null;
      Boolean purgeOnStartup = null;
      Integer purgerThreads = null;
      Boolean purgeSynchronously = null;

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case CLASS:
               loader = Util.getInstance(value, cl);
               break;
            case FETCH_PERSISTENT_STATE:
               fetchPersistentState = Boolean.valueOf(value);
               break;
            case IGNORE_MODIFICATIONS:
               ignoreModifications = Boolean.valueOf(value);
               break;
            case PURGE_ON_STARTUP:
               purgeOnStartup = Boolean.valueOf(value);
               break;
            case PURGER_THREADS:
               purgerThreads = Integer.valueOf(value);
               break;
            case PURGE_SYNCHRONOUSLY:
               purgeSynchronously = Boolean.valueOf(value);
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      if (loader != null) {
         if (loader instanceof FileCacheStore) {
            FileCacheStoreConfigurationBuilder fcscb = builder.loaders().addFileCacheStore();
            if (fetchPersistentState != null)
               fcscb.fetchPersistentState(fetchPersistentState);
            if (ignoreModifications != null)
               fcscb.ignoreModifications(ignoreModifications);
            if (purgeOnStartup != null)
               fcscb.purgeOnStartup(purgeOnStartup);
            if (purgeSynchronously != null)
               fcscb.purgeSynchronously(purgeSynchronously);
            parseLoaderChildren(reader, fcscb);
         } else {
            LoaderConfigurationBuilder lcb = builder.loaders().addCacheLoader();
            lcb.cacheLoader(loader);
            if (fetchPersistentState != null)
               lcb.fetchPersistentState(fetchPersistentState);
            if (ignoreModifications != null)
               lcb.ignoreModifications(ignoreModifications);
            if (purgerThreads != null)
               lcb.purgerThreads(purgerThreads);
            if (purgeOnStartup != null)
               lcb.purgeOnStartup(purgeOnStartup);
            if (purgeSynchronously != null)
               lcb.purgeSynchronously(purgeSynchronously);
            parseLoaderChildren(reader, lcb);
         }

      }

   }

   private void parseLoaderChildren(XMLStreamReader reader, AbstractLoaderConfigurationBuilder<?> loaderBuilder) throws XMLStreamException {
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case ASYNC:
               parseAsyncLoader(reader, loaderBuilder);
               break;
            case PROPERTIES:
               loaderBuilder.withProperties(parseProperties(reader));
               break;
            case SINGLETON_STORE:
               parseSingletonStore(reader, loaderBuilder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }
   }

   private void parseSingletonStore(XMLStreamReader reader, AbstractLoaderConfigurationBuilder<?> loaderBuilder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.parseBoolean(value))
                  loaderBuilder.singletonStore().enable();
               else
                  loaderBuilder.singletonStore().disable();
               break;
            case PUSH_STATE_TIMEOUT:
               loaderBuilder.singletonStore().pushStateTimeout(Long.parseLong(value));
               break;
            case PUSH_STATE_WHEN_COORDINATOR:
               loaderBuilder.singletonStore().pushStateWhenCoordinator(Boolean.parseBoolean(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);
   }

   private void parseAsyncLoader(XMLStreamReader reader, AbstractLoaderConfigurationBuilder<?> loaderBuilder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.parseBoolean(value))
                  loaderBuilder.async().enable();
               else
                  loaderBuilder.async().disable();
               break;
            case FLUSH_LOCK_TIMEOUT:
               loaderBuilder.async().flushLockTimeout(Long.parseLong(value));
               break;
            case MODIFICTION_QUEUE_SIZE:
               loaderBuilder.async().modificationQueueSize(Integer.parseInt(value));
               break;
            case SHUTDOWN_TIMEOUT:
               loaderBuilder.async().shutdownTimeout(Long.parseLong(value));
               break;
            case THREAD_POOL_SIZE:
               loaderBuilder.async().threadPoolSize(Integer.parseInt(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseJmxStatistics(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.parseBoolean(value))
                  builder.jmxStatistics().enable();
               else
                  builder.jmxStatistics().disable();
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);
   }

   private void parseInvocationBatching(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.parseBoolean(value))
                  builder.invocationBatching().enable();
               else
                  builder.invocationBatching().disable();
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseIndexing(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.parseBoolean(value))
                  builder.indexing().enable();
               else
                  builder.indexing().disable();
               break;
            case INDEX_LOCAL_ONLY:
               builder.indexing().indexLocalOnly(Boolean.parseBoolean(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }
      Properties indexingProperties = null;
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               indexingProperties = parseProperties(reader);
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
      IndexingConfigurationBuilder indexing = builder.indexing();
      if (indexingProperties != null) {
         indexing.withProperties(indexingProperties);
      }
   }

   private void parseExpiration(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case LIFESPAN:
               builder.expiration().lifespan(Long.parseLong(value));
               break;
            case MAX_IDLE:
               builder.expiration().maxIdle(Long.parseLong(value));
               break;
            case REAPER_ENABLED:
               if (Boolean.parseBoolean(value))
                  builder.expiration().enableReaper();
               else
                  builder.expiration().disableReaper();
               break;
            case WAKE_UP_INTERVAL:
               builder.expiration().wakeUpInterval(Long.parseLong(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseEviction(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case MAX_ENTRIES:
               builder.eviction().maxEntries(Integer.parseInt(value));
               break;
            case STRATEGY:
               builder.eviction().strategy(EvictionStrategy.valueOf(value));
               break;
            case THREAD_POLICY:
               builder.eviction().threadPolicy(EvictionThreadPolicy.valueOf(value));
               break;
            case WAKE_UP_INTERVAL:
               final Long wakeUpInterval = Long.valueOf(value);
               log.evictionWakeUpIntervalDeprecated(wakeUpInterval);
               builder.expiration().wakeUpInterval(wakeUpInterval);
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseDeadlockDetection(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.parseBoolean(value))
                  builder.deadlockDetection().enable();
               else
                  builder.deadlockDetection().disable();
               break;
            case SPIN_DURATION:
               builder.deadlockDetection().spinDuration(Long.parseLong(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseDataContainer(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case CLASS:
               builder.dataContainer().dataContainer(Util.<DataContainer>getInstance(value, cl));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES:
               builder.dataContainer().withProperties(parseProperties(reader));
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }
   }

   private void parseCustomInterceptors(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      ParseUtils.requireNoAttributes(reader);

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case INTERCEPTOR:
               parseInterceptor(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }

   }

   private void parseInterceptor(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {

      InterceptorConfigurationBuilder interceptorBuilder = builder.customInterceptors().addInterceptor();

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case AFTER:
               interceptorBuilder.after(Util.<CommandInterceptor>loadClass(value, cl));
               break;
            case BEFORE:
               interceptorBuilder.before(Util.<CommandInterceptor>loadClass(value, cl));
               break;
            case CLASS:
               interceptorBuilder.interceptor(Util.<CommandInterceptor>getInstance(value, cl));
               break;
            case INDEX:
               interceptorBuilder.index(Integer.parseInt(value));
               break;
            case POSITION:
               interceptorBuilder.position(Position.valueOf(value.toUpperCase()));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);
   }

   private void parseClustering(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {

      String clusteringMode = null;
      boolean synchronous = false;
      boolean asynchronous = false;

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case MODE:
               clusteringMode = value;
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case ASYNC:
               asynchronous = true;
               setMode(builder, clusteringMode, asynchronous, synchronous, reader);
               parseAsync(reader, builder);
               break;
            case HASH:
               parseHash(reader, builder);
               break;
            case L1:
               parseL1reader(reader, builder);
               break;
            case STATE_RETRIEVAL:
               parseStateRetrieval(reader, builder);
               break;
            case STATE_TRANSFER:
               parseStateTransfer(reader, builder);
               break;
            case SYNC:
               synchronous = true;
               setMode(builder, clusteringMode, asynchronous, synchronous, reader);
               parseSync(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }

      if (!synchronous && !asynchronous)
         setMode(builder, clusteringMode, asynchronous, asynchronous, reader);


   }

   private void setMode(ConfigurationBuilder builder, String clusteringMode, boolean asynchronous, boolean synchronous, XMLStreamReader reader) {
      if (synchronous && asynchronous)
         throw new ConfigurationException("Cannot configure <sync> and <async> on the same cluster, " + reader.getLocation());

      if (clusteringMode != null) {
         String mode = clusteringMode.toUpperCase();
         if (ParsedCacheMode.REPL.matches(mode)) {
            if (!asynchronous)
               builder.clustering().cacheMode(REPL_SYNC);
            else
               builder.clustering().cacheMode(REPL_ASYNC);
         } else if (ParsedCacheMode.INVALIDATION.matches(mode)) {
            if (!asynchronous)
               builder.clustering().cacheMode(INVALIDATION_SYNC);
            else
               builder.clustering().cacheMode(INVALIDATION_ASYNC);
         } else if (ParsedCacheMode.DIST.matches(mode)) {
            if (!asynchronous)
               builder.clustering().cacheMode(DIST_SYNC);
            else
               builder.clustering().cacheMode(DIST_ASYNC);
         } else if (ParsedCacheMode.LOCAL.matches(mode)) {
            builder.clustering().cacheMode(LOCAL);
         } else {
            throw new ConfigurationException("Invalid clustering mode " + clusteringMode + ", " + reader.getLocation());
         }
      } else {
         // If no cache mode is given but sync or async is specified, default to DIST
         if (synchronous)
            builder.clustering().cacheMode(DIST_SYNC);
         else if (asynchronous)
            builder.clustering().cacheMode(DIST_ASYNC);
      }
   }

   private void parseSync(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case REPL_TIMEOUT:
               builder.clustering().sync().replTimeout(Long.parseLong(value));
               break;

            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseStateRetrieval(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException{

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         log.stateRetrievalConfigurationDeprecated();
         switch (attribute) {
            case ALWAYS_PROVIDE_IN_MEMORY_STATE:
               log.alwaysProvideInMemoryStateDeprecated();
               break;
            case FETCH_IN_MEMORY_STATE:
               builder.clustering().stateTransfer().fetchInMemoryState(Boolean.parseBoolean(value));
               break;
            case INITIAL_RETRY_WAIT_TIME:
               log.initialRetryWaitTimeDeprecated();
               break;
            case LOG_FLUSH_TIMEOUT:
               log.logFlushTimeoutDeprecated();
               break;
            case MAX_NON_PROGRESSING_LOG_WRITES:
               log.maxProgressingLogWritesDeprecated();
               break;
            case NUM_RETRIES:
               log.numRetriesDeprecated();
               break;
            case RETRY_WAIT_TIME_INCREASE_FACTOR:
               log.retryWaitTimeIncreaseFactorDeprecated();
               break;
            case TIMEOUT:
               builder.clustering().stateTransfer().timeout(Long.parseLong(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseStateTransfer(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException{

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case FETCH_IN_MEMORY_STATE:
               builder.clustering().stateTransfer().fetchInMemoryState(Boolean.parseBoolean(value));
               break;
            case TIMEOUT:
               builder.clustering().stateTransfer().timeout(Long.parseLong(value));
               break;
            case CHUNK_SIZE:
               builder.clustering().stateTransfer().chunkSize(Integer.parseInt(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseL1reader(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.parseBoolean(value))
                  builder.clustering().l1().enable();
               else
                  builder.clustering().l1().disable();
               break;
            case INVALIDATION_THRESHOLD:
               builder.clustering().l1().invalidationThreshold(Integer.parseInt(value));
               break;
            case LIFESPAN:
               builder.clustering().l1().lifespan(Long.parseLong(value));
               break;
            case INVALIDATION_CLEANUP_TASK_FREQUENCY:
               builder.clustering().l1().cleanupTaskFrequency(Long.parseLong(value));
               break;
            case ON_REHASH:
               if (Boolean.parseBoolean(value))
                  builder.clustering().l1().enableOnRehash();
               else
                  builder.clustering().l1().disableOnRehash();
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseHash(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case CLASS:
            case HASH_FUNCTION_CLASS:
               builder.clustering().hash().consistentHash(Util.<ConsistentHash> getInstance(value, cl));
               break;
            case NUM_OWNERS:
               builder.clustering().hash().numOwners(Integer.parseInt(value));
               break;
            case NUM_VIRTUAL_NODES:
               builder.clustering().hash().numVirtualNodes(Integer.parseInt(value));
               break;
            case REHASH_ENABLED:
               log.hashRehashEnabledDeprecated();
               builder.clustering().stateTransfer().fetchInMemoryState(Boolean.parseBoolean(value));
               break;
            case REHASH_RPC_TIMEOUT:
               log.hashRehashRpcTimeoutDeprecated();
               builder.clustering().stateTransfer().timeout(Long.parseLong(value));
               break;
            case REHASH_WAIT:
               log.hashRehashWaitDeprecated();
               builder.clustering().stateTransfer().timeout(Long.parseLong(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case GROUPS:
               parseGroups(reader, builder);
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }

   }

   private void parseGroups(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {

      ParseUtils.requireSingleAttribute(reader, "enabled");

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ENABLED:
               if (Boolean.parseBoolean(value))
                  builder.clustering().hash().groups().enabled();
               else
                  builder.clustering().hash().groups().disabled();
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case GROUPER:
               String value = ParseUtils.readStringAttributeElement(reader, "class");
               builder.clustering().hash().groups().addGrouper(Util.<Grouper<?>>getInstance(value, cl));
               break;
            default:
               throw ParseUtils.unexpectedElement(reader);
         }
      }

   }

   private void parseAsync(XMLStreamReader reader, ConfigurationBuilder builder) throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case ASYNC_MARSHALLING:
               if (Boolean.parseBoolean(value))
                  builder.clustering().async().asyncMarshalling();
               else
                  builder.clustering().async().syncMarshalling();
               break;
            case REPL_QUEUE_CLASS:
               builder.clustering().async().replQueue(Util.<ReplicationQueue> getInstance(value, cl));
               break;
            case REPL_QUEUE_INTERVAL:
               builder.clustering().async().replQueueInterval(Long.parseLong(value));
               break;
            case REPL_QUEUE_MAX_ELEMENTS:
               builder.clustering().async().replQueueMaxElements(Integer.parseInt(value));
               break;
            case USE_REPL_QUEUE:
               builder.clustering().async().useReplQueue(Boolean.parseBoolean(value));
               break;
            default:
               throw ParseUtils.unexpectedAttribute(reader, i);
         }
      }

      ParseUtils.requireNoContent(reader);

   }

   private void parseGlobal(XMLStreamReader reader, GlobalConfigurationBuilder builder) throws XMLStreamException {

      ParseUtils.requireNoAttributes(reader);
      boolean transportParsed = false;
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case ASYNC_LISTENER_EXECUTOR: {
               parseAsyncListenerExectuor(reader, builder);
               break;
            }
            case ASYNC_TRANSPORT_EXECUTOR: {
               parseAsyncTransportExecutor(reader, builder);
               break;
            }
            case EVICTION_SCHEDULED_EXECUTOR: {
               parseEvictionScheduledExecutor(reader, builder);
               break;
            }
            case GLOBAL_JMX_STATISTICS: {
               parseGlobalJMXStatistics(reader, builder);
               break;
            }
            case REPLICATION_QUEUE_SCHEDULED_EXECUTOR: {
               parseReplicationQueueScheduledExecutor(reader, builder);
               break;
            }
            case SERIALIZATION: {
               parseSerialization(reader, builder);
               break;
            }
            case SHUTDOWN: {
               parseShutdown(reader, builder);
               break;
            }
            case TRANSPORT: {
               parseTransport(reader, builder);
               transportParsed = true;
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
      if (!transportParsed) {
         // make sure there is no "default" transport
         builder.transport().transport(null);
      } else {
         // The transport *has* been parsed.  If we don't have a transport set, make sure we set the default.
         if (builder.transport().getTransport() == null) {
            builder.transport().transport(Util.getInstance(TransportConfigurationBuilder.DEFAULT_TRANSPORT));
         }
      }
   }

   private void parseTransport(XMLStreamReader reader, GlobalConfigurationBuilder builder) throws XMLStreamException {

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case CLUSTER_NAME: {
               builder.transport().clusterName(value);
               break;
            }
            case DISTRIBUTED_SYNC_TIMEOUT: {
               builder.transport().distributedSyncTimeout(Long.parseLong(value));
               break;
            }
            case MACHINE_ID: {
               builder.transport().machineId(value);
               break;
            }
            case NODE_NAME: {
               builder.transport().nodeName(value);
               break;
            }
            case RACK_ID: {
               builder.transport().rackId(value);
               break;
            }
            case SITE_ID: {
               builder.transport().siteId(value);
               break;
            }
            case STRICT_PEER_TO_PEER: {
               builder.transport().strictPeerToPeer(Boolean.valueOf(value));
               break;
            }
            case TRANSPORT_CLASS: {
               builder.transport().transport(Util.<Transport> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.transport().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseShutdown(XMLStreamReader reader, GlobalConfigurationBuilder builder) throws XMLStreamException {

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case HOOK_BEHAVIOR: {
               builder.shutdown().hookBehavior(ShutdownHookBehavior.valueOf(value));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }

      ParseUtils.requireNoContent(reader);
   }

   private void parseSerialization(XMLStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case MARSHALLER_CLASS: {
               builder.serialization().marshaller(Util.<Marshaller>getInstance(value, cl));
               break;
            }
            case VERSION: {
               builder.serialization().version(value);
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case ADVANCED_EXTERNALIZERS: {
               parseAdvancedExternalizers(reader, builder);
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }

   }

   private void parseAdvancedExternalizers(XMLStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {

      ParseUtils.requireNoAttributes(reader);

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case ADVANCED_EXTERNALIZER: {
               int attributes = reader.getAttributeCount();
               AdvancedExternalizer<?> advancedExternalizer = null;
               Integer id = null;
               ParseUtils.requireAttributes(reader, Attribute.EXTERNALIZER_CLASS.getLocalName());
               for (int i = 0; i < attributes; i++) {
                  String value = replaceSystemProperties(reader.getAttributeValue(i));
                  Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
                  switch (attribute) {
                     case EXTERNALIZER_CLASS: {
                        advancedExternalizer = Util.getInstance(value, cl);
                        break;
                     }
                     case ID: {
                        id = Integer.valueOf(value);
                        break;
                     }
                     default: {
                        throw ParseUtils.unexpectedAttribute(reader, i);
                     }
                  }
               }

               ParseUtils.requireNoContent(reader);

               if (id != null)
                  builder.serialization().addAdvancedExternalizer(id, advancedExternalizer);
               else
                  builder.serialization().addAdvancedExternalizer(advancedExternalizer);
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseReplicationQueueScheduledExecutor(XMLStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case FACTORY: {
               builder.replicationQueueScheduledExecutor().factory(Util.<ScheduledExecutorFactory> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.replicationQueueScheduledExecutor().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseGlobalJMXStatistics(XMLStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         // allowDuplicateDomains="true" cacheManagerName="" enabled="true" jmxDomain=""
         // mBeanServerLookup
         switch (attribute) {
            case ALLOW_DUPLICATE_DOMAINS: {
               builder.globalJmxStatistics().allowDuplicateDomains(Boolean.valueOf(value));
               break;
            }
            case CACHE_MANAGER_NAME: {
               builder.globalJmxStatistics().cacheManagerName(value);
               break;
            }
            case ENABLED: {
               if (!Boolean.parseBoolean(value))
                  builder.globalJmxStatistics().disable();
               else
                  builder.globalJmxStatistics().enable();
               break;
            }
            case JMX_DOMAIN: {
               builder.globalJmxStatistics().jmxDomain(value);
               break;
            }
            case MBEAN_SERVER_LOOKUP: {
               builder.globalJmxStatistics().mBeanServerLookup(Util.<MBeanServerLookup> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.globalJmxStatistics().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseEvictionScheduledExecutor(XMLStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case FACTORY: {
               builder.evictionScheduledExecutor().factory(Util.<ScheduledExecutorFactory> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.evictionScheduledExecutor().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseAsyncTransportExecutor(XMLStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {
      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case FACTORY: {
               builder.asyncTransportExecutor().factory(Util.<ExecutorFactory> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.asyncTransportExecutor().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
   }

   private void parseAsyncListenerExectuor(XMLStreamReader reader, GlobalConfigurationBuilder builder)
         throws XMLStreamException {

      for (int i = 0; i < reader.getAttributeCount(); i++) {
         ParseUtils.requireNoNamespaceAttribute(reader, i);
         String value = replaceSystemProperties(reader.getAttributeValue(i));
         Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute) {
            case FACTORY: {
               builder.asyncListenerExecutor().factory(Util.<ExecutorFactory> getInstance(value, cl));
               break;
            }
            default: {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
      }

      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTIES: {
               builder.asyncListenerExecutor().withProperties(parseProperties(reader));
               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }

   }

   public static Properties parseProperties(XMLStreamReader reader) throws XMLStreamException {

      ParseUtils.requireNoAttributes(reader);

      Properties p = new Properties();
      while (reader.hasNext() && (reader.nextTag() != XMLStreamConstants.END_ELEMENT)) {
         Element element = Element.forName(reader.getLocalName());
         switch (element) {
            case PROPERTY: {
               int attributes = reader.getAttributeCount();
               ParseUtils.requireAttributes(reader, Attribute.NAME.getLocalName(), Attribute.VALUE.getLocalName());
               String key = null;
               String propertyValue = null;
               for (int i = 0; i < attributes; i++) {
                  String value = replaceSystemProperties(reader.getAttributeValue(i));
                  Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
                  switch (attribute) {
                     case NAME: {
                        key = value;
                        break;
                     } case VALUE: {
                        propertyValue = value;
                        break;
                     }
                     default: {
                        throw ParseUtils.unexpectedAttribute(reader, i);
                     }
                  }
               }
               p.put(key, propertyValue);

               ParseUtils.requireNoContent(reader);

               break;
            }
            default: {
               throw ParseUtils.unexpectedElement(reader);
            }
         }
      }
      return p;
   }

}
