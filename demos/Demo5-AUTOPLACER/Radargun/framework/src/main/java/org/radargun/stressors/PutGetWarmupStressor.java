package org.radargun.stressors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.CacheWrapper;
import org.radargun.CacheWrapperStressor;
import org.radargun.keygen2.KeyGeneratorFactory;
import org.radargun.keygen2.WarmupEntry;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The warmup that initialize the keys used by the benchmark
 *
 * @author pruivo
 * @since 4.0
 */
public class PutGetWarmupStressor implements CacheWrapperStressor {

   private static Log log = LogFactory.getLog(PutGetWarmupStressor.class);

   //the slave/node index
   private int nodeIndex = 0;

   //true if the cache wrapper uses passive replication    
   private boolean isPassiveReplication = false;

   //the size of the transaction. if size is less or equals 1, than it will be disable
   private int transactionSize = 100;

   private final KeyGeneratorFactory factory;

   public PutGetWarmupStressor() {
      factory = new KeyGeneratorFactory();
   }

   @Override
   public Map<String, String> stress(CacheWrapper wrapper) {
      if (wrapper == null) {
         throw new IllegalStateException("Null wrapper not allowed");
      }

      factory.calculate();

      Iterator<WarmupEntry> iterator;
      if (isPassiveReplication && wrapper.canExecuteWriteTransactions()) {
         iterator = factory.warmupAll();
      } else if (!isPassiveReplication){
         iterator = factory.warmup(nodeIndex);
      } else {
         return null;
      }

      if (isTransactional()) {
         performTransactionalWarmup(wrapper, iterator);
      } else {
         performNonTransactionalWarmup(wrapper, iterator);
      }

      return null;
   }

   private void performTransactionalWarmup(CacheWrapper cacheWrapper, Iterator<WarmupEntry> iterator) {
      log.trace("Performing transactional warmup. Transaction size is " + transactionSize);
      while (iterator.hasNext()) {
         List<WarmupEntry> warmupEntryList = new LinkedList<WarmupEntry>();
         for (int i = 0; i < transactionSize && iterator.hasNext(); ++i) {
            warmupEntryList.add(iterator.next());
         }

         boolean success = false;
         while (!success) {
            cacheWrapper.startTransaction();
            try {
               for (WarmupEntry warmupEntry : warmupEntryList) {
                  cacheWrapper.put(warmupEntry.getBucket(), warmupEntry.getKey(), warmupEntry.getValue());
               }
               success = true;
            } catch (Exception e) {
               success = false;
               log.warn("A put operation has failed. retry the transaction");
            }
            try {
               cacheWrapper.endTransaction(success);
            } catch (Exception e) {
               log.warn("A transaction has rolled back. retry the transaction");
            }
         }
      }
   }

   private void performNonTransactionalWarmup(CacheWrapper cacheWrapper, Iterator<WarmupEntry> iterator) {
      while (iterator.hasNext()) {
         WarmupEntry warmupEntry = iterator.next();
         boolean success = false;
         while (!success) {
            try {
               cacheWrapper.put(warmupEntry.getBucket(), warmupEntry.getKey(), warmupEntry.getValue());
               success = true;
            } catch (Exception e) {
               log.warn("A put operation has failed. retry the put operation");
            }
         }
      }
   }

   @Override
   public void destroy() throws Exception {
      //Do nothing... we don't want to loose the keys
   }

   @Override
   public String toString() {
      return "PutGetWarmupStressor{" +
            "nodeIndex=" + nodeIndex +
            ", isPassiveReplication=" + isPassiveReplication +
            ", transactionSize=" + transactionSize +
            ", keyGeneratorFactory=" + factory +
            '}';
   }

   private boolean isTransactional() {
      return transactionSize > 1;
   }

   public KeyGeneratorFactory getFactory() {
      return factory;
   }

   public void setNodeIndex(int nodeIndex) {
      this.nodeIndex = nodeIndex;
   }

   public void setNumberOfKeys(int numberOfKeys) {
      factory.setNumberOfKeys(numberOfKeys);
   }

   public void setSizeOfValue(int sizeOfValue) {
      factory.setValueSize(sizeOfValue);
   }

   public void setNumOfThreads(int numOfThreads) {
      factory.setNumberOfThreads(numOfThreads);
   }

   public void setBucketPrefix(String bucketPrefix) {
      factory.setBucketPrefix(bucketPrefix);
   }

   public void setPassiveReplication(boolean passiveReplication) {
      isPassiveReplication = passiveReplication;
   }

   public void setTransactionSize(int transactionSize) {
      this.transactionSize = transactionSize;
   }

   public void setNumberOfNodes(int numberOfNodes) {
      factory.setNumberOfNodes(numberOfNodes);
   }
}
