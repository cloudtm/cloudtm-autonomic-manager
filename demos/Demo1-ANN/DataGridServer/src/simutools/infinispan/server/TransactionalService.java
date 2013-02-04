/*
 * CINI, Consorzio Interuniversitario Nazionale per l'Informatica
 * Copyright 2013 CINI and/or its affiliates and other
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

package simutools.infinispan.server;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import simutools.infinispan.test.tpcc.*;

import javax.transaction.*;
import java.io.*;
import java.net.Socket;

/*
* @author Sebastiano Peluso
*/
public class TransactionalService implements Runnable {

    static final String PUT = "PUT";
    static final String GET = "GET";
    static final String COMMIT = "COMMIT";
    static final String ABORT = "ABORT";
    static final String BEGIN = "BEGIN";

    static final String NUM_MEMBERS = "NUM_MEMBERS";

    static final String SIZE = "SIZE";

    static final String LOCAL_SIZE = "LOCAL_SIZE";

    static final String TPCC_POPULATION = "TPCC";


    static final String STOP = "STOP";


    private Socket connection;
    private TransactionManager tm;
    private Cache cache;
    private DefaultCacheManager cacheManager;

    public TransactionalService(Socket connection, TransactionManager tm, Cache cache, DefaultCacheManager cacheManager){

        this.connection = connection;
        this.tm = tm;
        this.cache = cache;
        this.cacheManager = cacheManager;


    }

    public void run() {

        BufferedReader in = null;

        PrintStream pout = null;

        ObjectInputStream objectIn= null;

        ObjectOutputStream objectOut = null;

        String command = null;

        try {

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            objectIn = new ObjectInputStream(connection.getInputStream());

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());

            objectOut = new ObjectOutputStream(connection.getOutputStream());

            pout = new PrintStream(out);

            boolean error = false;

            while(!error && connection!=null && !connection.isClosed()){

                command = (String) objectIn.readObject();

                if(command!= null && command.equals(BEGIN)){



                    boolean ack = false;

                    ack = performBegin();



                    objectOut.writeBoolean(ack);
                    objectOut.flush();





                }
                else if(command!= null && command.equals(PUT)){

                    Object key = objectIn.readObject();

                    Object value = objectIn.readObject();


                    //System.out.println("Performing PUT: Key -> "+key+"; Value -> "+value);

                    performPut(key, value);

                    objectOut.writeBoolean(true);
                    objectOut.flush();



                }
                else if(command!= null && command.equals(GET)){

                    Object key = objectIn.readObject();



                    Object value = performGet(key);

                    objectOut.writeObject(value);
                    objectOut.flush();


                }
                else if(command!= null && command.equals(COMMIT)){

                    boolean ack = false;

                    ack = performCommit();

                    objectOut.writeBoolean(ack);
                    objectOut.flush();


                }
                else if(command!= null && command.equals(ABORT)){

                    boolean ack = false;

                    ack = performAbort();

                    objectOut.writeBoolean(ack);
                    objectOut.flush();


                }
                else if(command!= null && command.equals(NUM_MEMBERS)){

                    int result = getNumMembers();

                    objectOut.writeInt(result);
                    objectOut.flush();

                }
                else if(command!= null && command.equals(SIZE)){

                    int result = getSize();

                    objectOut.writeInt(result);
                    objectOut.flush();

                }
                else if(command!= null && command.equals(LOCAL_SIZE)){

                    int result = getLocalSize();

                    objectOut.writeInt(result);
                    objectOut.flush();

                }
                else if(command != null && command.equals(TPCC_POPULATION)){

                    int numWarehouses = objectIn.readInt();
                    int slaveIndex = objectIn.readInt();
                    int numSlaves = objectIn.readInt();
                    long cLastMask = objectIn.readLong();
                    long olIdMask = objectIn.readLong();
                    long cIdMask = objectIn.readLong();

                    new TpccPopulation(new InfinispanServerCacheWrapper(this.cache), numWarehouses, slaveIndex, numSlaves, cLastMask, olIdMask, cIdMask);

                    objectOut.writeBoolean(true);
                    objectOut.flush();


                }
                else if(command != null && command.equals(STOP)){
                    if(this.cache != null)
                        this.cache.stop();
                    if(this.cacheManager != null){
                        this.cacheManager.stop();
                    }
                    if(this.connection != null)
                        this.connection.close();
                    System.out.println("The Server is stopped.");
                    Runtime.getRuntime().exit(0);
                }
                else{
                    error = true;
                    throw new RuntimeException("Unrecognized command!");

                }

            }


        } catch (IOException e) {
            //System.out.println("Connection closed");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally{

            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {

                }
            }

            if(objectIn != null){
                try {
                    objectIn.close();
                } catch (IOException e) {

                }
            }

            if(objectOut != null){
                try {
                    objectOut.close();
                } catch (IOException e) {

                }
            }

            if(pout != null){
                pout.close();
            }

            if(connection != null){
                try {
                    connection.close();
                } catch (IOException e) {

                }
            }


            if(command != null && command.equals(STOP)){
                System.out.println("The Server is stopped.");
                Runtime.getRuntime().exit(0);
            }

        }




    }


    private boolean performBegin(){

        try {
            this.tm.begin();
            return true;
        } catch (NotSupportedException e) {
            e.printStackTrace();
            return false;
        } catch (SystemException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean performCommit(){

        try {
            this.tm.commit();
            return true;
        } catch (RollbackException e) {
            e.printStackTrace();
            return false;
        } catch (HeuristicMixedException e) {
            e.printStackTrace();
            return false;
        } catch (HeuristicRollbackException e) {
            e.printStackTrace();
            return false;
        } catch (SystemException e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean performAbort(){

        try {
            this.tm.rollback();
            return true;
        } catch (SystemException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Object performGet(Object key){

        Object result = null;

        result = this.cache.get(key);

        return result;

    }

    private void performPut(Object key, Object value){

        this.cache.put(key, value);
    }

    private int getNumMembers(){
        return cacheManager.getMembers() == null ? 0 : cacheManager.getMembers().size();
    }

    private int getSize(){
        return this.cache.keySet().size();
    }

    private int getLocalSize(){
        return this.cache.size();
    }
}
