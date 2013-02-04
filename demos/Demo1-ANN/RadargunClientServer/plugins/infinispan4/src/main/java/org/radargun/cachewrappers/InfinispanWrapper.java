package org.radargun.cachewrappers;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.arjuna.objectstore.VolatileStore;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.remoting.transport.Address;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.radargun.CacheWrapper;
import org.radargun.utils.TypedProperties;
import org.radargun.utils.Utils;
import simutools.infinispan.client.InfinispanClient;
import simutools.infinispan.client.InfinispanClientFactory;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.io.IOException;
import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;

public class InfinispanWrapper implements CacheWrapper {

    static {
        // Set up transactional stores for JBoss TS
        arjPropertyManager.getCoordinatorEnvironmentBean().setCommunicationStore(VolatileStore.class.getName());
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(VolatileStore.class.getName());
    }

    private static Log log = LogFactory.getLog(InfinispanWrapper.class);
    protected DefaultCacheManager cacheManager;
    private Cache<Object, Object> cache;
    TransactionManager tm;
    protected boolean started = false;
    String config;
    private volatile boolean enlistExtraXAResource;

    private boolean remoteCache = false;

    private InfinispanClientFactory clientFactory = null;



    public void setUp(String config, boolean isLocal, int nodeIndex, TypedProperties confAttributes, String remoteCaches) throws Exception {

        if(remoteCaches != null) this.remoteCache = true;


        if(this.remoteCache == true){
            clientFactory = new InfinispanClientFactory(remoteCaches, nodeIndex); //This is the address of the Infinispan server
        }
        else{
            this.config = config;
            String configFile  = confAttributes.containsKey("file") ? confAttributes.getProperty("file") : config;
            String cacheName = confAttributes.containsKey("cache") ? confAttributes.getProperty("cache") : "x";

            log.trace("Using config file: " + configFile + " and cache name: " + cacheName);

            if (!started) {
                cacheManager = new DefaultCacheManager(configFile);
                String cacheNames = cacheManager.getDefinedCacheNames();
                if (!cacheNames.contains(cacheName))
                    throw new IllegalStateException("The requested cache(" + cacheName + ") is not defined. Defined cache " +
                            "names are " + cacheNames);
                cache = cacheManager.getCache(cacheName);
                started = true;
                tm = cache.getAdvancedCache().getTransactionManager();
                log.info("Using transaction manager: " + tm);
            }
            log.debug("Loading JGroups from: " + org.jgroups.Version.class.getProtectionDomain().getCodeSource().getLocation());
            log.info("JGroups version: " + org.jgroups.Version.printDescription());
            log.info("Using config attributes: " + confAttributes);
            blockForRehashing(cache);
            injectEvenConsistentHash(cache, confAttributes);

        }


    }



    public boolean populateTpcc(int numWarehouses, int slaveIndex, int numSlaves, long cLastMask, long olIdMask, long cIdMask){


        if(remoteCache == true){
            InfinispanClient client = null;

            boolean clientCreated = false;
            int numAttempt = 0;
            while(!clientCreated){
                try{
                    client = clientFactory.createClient();
                    clientCreated = true;
                } catch (IOException e) {
                    clientCreated = false;
                    numAttempt++;
                    if(numAttempt > 500){
                        e.printStackTrace();
                        return false;
                    }
                }
            }



            boolean result = client.tpccPopulation(numWarehouses, slaveIndex, numSlaves, cLastMask, olIdMask, cIdMask);

            client.close();

            return result;
        }
        else{
            return false;
        }

    }



    public void tearDown() throws Exception {
        if(this.remoteCache == true){

        }
        else{
            List<Address> addressList = cacheManager.getMembers();
            if (started) {
                cacheManager.stop();
                log.trace("Stopped, previous view is " + addressList);
                started = false;
            }
        }
    }

    public void put(String bucket, Object key, Object value) throws Exception {
        if(remoteCache == true){
            InfinispanClient client = null;

            boolean clientCreated = false;
            int numAttempt = 0;
            while(!clientCreated){
                try{
                    client = clientFactory.createClient();
                    clientCreated = true;
                } catch (IOException e) {
                    clientCreated = false;
                    numAttempt++;
                    if(numAttempt > 500){
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }

            //System.out.println("Try PUT "+key+" "+value);

            boolean result = client.put(key, value);

            client.close();

            if(!result){
                throw new Exception("Error during put on remote Infinispan Client");
            }
        }
        else{
            cache.put(key, value);
        }

    }

    public Object get(String bucket, Object key) throws Exception {
        if(remoteCache == true){
            InfinispanClient client = null;
            try{
                client = clientFactory.createClient();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }


            Object result = client.get(key);

            client.close();

            return result;


        }
        else{
            return cache.get(key);
        }

    }

    public void empty() throws Exception {
        if(remoteCache == true) {

        }
        else{
            RpcManager rpcManager = cache.getAdvancedCache().getRpcManager();
            int clusterSize = 0;
            if (rpcManager != null) {
                clusterSize = rpcManager.getTransport().getMembers().size();
            }
            //use keySet().size() rather than size directly as cache.size might not be reliable
            log.info("Cache size before clear (cluster size= " + clusterSize +")" + cache.keySet().size());

            // We don't need locks, etc - we're shutting down.  Clear as much as we can directly;
            // speeds things up when we have a very large key-set.  This operation isn't measured in any benchmark anyway.
            cache.getAdvancedCache().getDataContainer().clear();
            cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).clear();
            log.info("Cache size after clear: " + cache.keySet().size());
        }

    }

    public int getNumMembers() {
        if(remoteCache == true){

            InfinispanClient client = null;
            try{
                client = clientFactory.createClient();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }


            int result = client.getNumMembers();

            client.close();

            return result;

        }
        else{
            ComponentRegistry componentRegistry = cache.getAdvancedCache().getComponentRegistry();
            if (componentRegistry.getStatus().startingUp()) {
                log.trace("We're in the process of starting up.");
            }
            if (cacheManager.getMembers() != null) {
                log.trace("Members are: " + cacheManager.getMembers());
            }
            return cacheManager.getMembers() == null ? 0 : cacheManager.getMembers().size();
        }

    }

    public String getInfo() {
        if(remoteCache == true){
            return "";
        }
        else{
            //Important: don't change this string without validating the ./dist.sh as it relies on its format!!
            return "Running : " + cache.getVersion() +  ", config:" + config + ", cacheName:" + cache.getName();
        }
    }

    public Object getReplicatedData(String bucket, String key) throws Exception {

            return get(bucket, key);

    }

    public CacheWrapper transactify(){

        if(remoteCache == true){
            InfinispanClient client = null;
            try{
                client = clientFactory.createClient();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }



            InfinispanRemoteTransactionalWrapper remote = new InfinispanRemoteTransactionalWrapper(client, clientFactory);

            return remote;

        }
        else{
            return this;
        }
    }

    public void startTransaction() {



        if(remoteCache == true){}
        else{
            assertTm();
            try {
                tm.begin();
                Transaction transaction = tm.getTransaction();
                if (enlistExtraXAResource) {
                    transaction.enlistResource(new DummyXAResource());
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);

            }
        }
    }

    public void endTransaction(boolean successful) {
        if(remoteCache == true){}
        else{
            assertTm();
            try {
                if (successful)
                    tm.commit();
                else
                    tm.rollback();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int size() {
        if(remoteCache == true){

            InfinispanClient client = null;
            try{
                client = clientFactory.createClient();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }


            int result = client.getSize();

            client.close();

            return result;

        }
        else{
            return cache.keySet().size();
        }
    }

    protected void blockForRehashing(Cache aCache) throws InterruptedException {
        if(remoteCache == true){

        }
        else{
            // should we be blocking until all rehashing, etc. has finished?
            long gracePeriod = MINUTES.toMillis(15);
            long giveup = System.currentTimeMillis() + gracePeriod;
            if (aCache.getConfiguration().getCacheMode().isDistributed()) {
                while (!aCache.getAdvancedCache().getDistributionManager().isJoinComplete() && System.currentTimeMillis() < giveup)
                    Thread.sleep(200);
            }

            if (aCache.getConfiguration().getCacheMode().isDistributed() && !aCache.getAdvancedCache().getDistributionManager().isJoinComplete())
                throw new RuntimeException("Caches haven't discovered and joined the cluster even after " + Utils.prettyPrintMillis(gracePeriod));
        }
    }

    protected void injectEvenConsistentHash(Cache aCache, TypedProperties confAttributes) {
        if(remoteCache == true){}
        else{
            if (aCache.getConfiguration().getCacheMode().isDistributed()) {
                ConsistentHash ch = aCache.getAdvancedCache().getDistributionManager().getConsistentHash();
                if (ch instanceof EvenSpreadingConsistentHash) {
                    int threadsPerNode = confAttributes.getIntProperty("threadsPerNode", -1);
                    if (threadsPerNode < 0) throw new IllegalStateException("When EvenSpreadingConsistentHash is used threadsPerNode must also be set.");
                    int keysPerThread = confAttributes.getIntProperty("keysPerThread", -1);
                    if (keysPerThread < 0) throw new IllegalStateException("When EvenSpreadingConsistentHash is used must also be set.");
                    ((EvenSpreadingConsistentHash)ch).init(threadsPerNode, keysPerThread);
                    log.info("Using an even consistent hash!");
                }

            }
        }
    }

    public Cache<Object, Object> getCache() {
        if(remoteCache == true){
            return null;
        }
        else{
            return cache;
        }
    }

    private void assertTm() {
        if(remoteCache == true){}
        else{
            if (tm == null) throw new RuntimeException("No configured TM!");
        }
    }

    public void setEnlistExtraXAResource(boolean enlistExtraXAResource) {
        this.enlistExtraXAResource = enlistExtraXAResource;
    }

}
