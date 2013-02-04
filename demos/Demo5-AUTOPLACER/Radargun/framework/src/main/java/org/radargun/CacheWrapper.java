package org.radargun;


import org.radargun.keygen2.RadargunKey;
import org.radargun.reporting.DataPlacementStats;
import org.radargun.utils.BucketsKeysTreeSet;

import javax.transaction.RollbackException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Map;

/**
 * CacheWrappers wrap caching products tp provide RadarGun with a standard way of
 * accessing and manipulating a cache.
 *
 * @author Manik Surtani (manik@surtani.org)
 */
public interface CacheWrapper
{
   /**
    * Initialises the cache.  Typically this step will configure the
    * cacheing product with various params passed in, described in
    * benchmark.xml for a particular cacheing product, which is
    * usually the name or path to a config file specific to the
    * cacheing product being tested.
    *
    * @param config
    * @param isLocal
    * @param nodeIndex
    * @throws Exception
    */
   void setUp(String config, boolean isLocal, int nodeIndex) throws Exception;

   /**
    * This is called at the very end of all tests on this cache, and is used for clean-up
    * operations.
    * @throws Exception
    */
   void tearDown() throws Exception;

   /**
    * This method is called when the framework needs to put an object in cache.  This method is treated
    * as a black box, and is what is timed, so it should be implemented in the most efficient (or most
    * realistic) way possible.
    *
    * @param bucket a bucket is a group of keys. Some implementations might ignore the bucket
    * so in order to avoid key collisions, one should make sure that the keys are unique even between different buckets.
    * @param key
    * @param value
    * @throws Exception
    */
   void put(String bucket, Object key, Object value) throws Exception;

   /**
    * @param bucket
    * @param key
    * @see #put(String, Object, Object)
    * @return
    * @throws Exception
    */
   Object get(String bucket, Object key) throws Exception;

   /**
    * This is called after each test type (if emptyCacheBetweenTests is set to true in benchmark.xml) and is
    * used to flush the cache.
    * @throws Exception
    */
   void empty() throws Exception;

   /**
    * @return the number of members in the cache's cluster
    */
   int getNumMembers();

   /**
    * @return Some info about the cache contents, perhaps just a count of objects.
    */
   String getInfo();

   /**
    * Some caches (e.g. JBossCache with  buddy replication) do not store replicated data directlly in the main
    * structure, but use some additional structure to do this (replication tree, in the case of buddy replication).
    * This method is a hook for handling this situations.
    * @param bucket
    * @param key
    */
   Object getReplicatedData(String bucket, String key) throws Exception;

   Object startTransaction();

   void endTransaction(boolean successful) throws RollbackException;


   //Added by Pedro
   /**
    *
    * @return
    */
   boolean isCoordinator();

   /**
    *
    * @param bucket
    * @param key
    * @return
    */
   boolean isKeyLocal(String bucket, String key);

   /**
    *
    * @return
    */
   Map<String, String> getAdditionalStats();

   /**
    * save the keys stressed by this slave. this is used in the GetKeysStage, where it obtains the values
    * of the keys. in the end, it is possible to check if the values are equals in each node...
    * @param keys the set of keys
    */
   void saveKeysStressed(BucketsKeysTreeSet keys);

   /**
    * see above
    * @return the set of keys
    */
   BucketsKeysTreeSet getStressedKeys();

   /**
    * returns the number of keys in this cache
    * @return the number of keys in this cache
    */
   int getCacheSize();

   /**
    * check if this cache can execute read-only transactions
    * @return true if the cache can execute read-only transactions
    */
   boolean canExecuteReadOnlyTransactions();

   /**
    * check if this cache can execute write transactions
    * @return true if the cache can execute write transactions
    */
   boolean canExecuteWriteTransactions();

   /**
    * it resets the additional stats
    */
   void resetAdditionalStats();

   <T> Collection<? extends T> getLocalKeys(Class<T> type);

   void collectDataPlacementStats(ObjectInputStream objectsToMove, Collection<RadargunKey> keys,
                                  DataPlacementStats stats) throws Exception;

   void convertTotString(ObjectInputStream objectsToMove, BufferedWriter writer) throws Exception;
}
