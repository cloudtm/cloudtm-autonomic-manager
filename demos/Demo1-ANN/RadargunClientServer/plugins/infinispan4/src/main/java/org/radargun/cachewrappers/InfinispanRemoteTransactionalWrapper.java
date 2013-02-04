package org.radargun.cachewrappers;

import org.radargun.CacheWrapper;
import org.radargun.utils.TypedProperties;
import simutools.infinispan.client.InfinispanClient;
import simutools.infinispan.client.InfinispanClientFactory;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: sebastiano
 * Date: 11/11/12
 * Time: 12:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class InfinispanRemoteTransactionalWrapper extends InfinispanWrapper {

    private InfinispanClient client = null;

    private InfinispanClientFactory clientFactory = null;


    public InfinispanRemoteTransactionalWrapper(InfinispanClient client, InfinispanClientFactory clientFactory){

        this.client = client;
        this.clientFactory = clientFactory;
    }


    public void setUp(String config, boolean isLocal, int nodeIndex, TypedProperties confAttributes, String remoteCache) throws Exception {

        //This is empty!


    }


    public void put(String bucket, Object key, Object value) throws Exception {

        if(!this.client.put(key, value)){

            throw new Exception("Error during put on remote Infinispan Client");

        }

    }

    public Object get(String bucket, Object key) throws Exception {

        return this.client.get(key);

    }



    public void startTransaction() {

        if(this.client == null){
            try{
                client = clientFactory.createClient();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        if(!this.client.begin()){

            this.client = null;

            throw new RuntimeException("Unable to start remote Transaction.");
        }

    }

    public void endTransaction(boolean successful) {

        boolean ack = false;

        if(successful){

            ack = this.client.commit();

        }
        else{
            ack = this.client.abort();
        }

        if(!successful || ack){//(TO BE) ABORTED OR SUCCESSFULLY COMMITTED
            this.client.close();

            this.client = null;
        }


        if(!ack){
            throw new RuntimeException("Unable to complete transaction on remote Infinispan Client");
        }


    }




}
