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
 
package simutools.infinispan.client;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
* @author Sebastiano Peluso
*/

public class InfinispanClient {

    private static Logger log = Logger.getLogger(InfinispanClient.class);

    private static boolean DEBUG = log.isDebugEnabled();

    private static boolean INFO = log.isInfoEnabled();

    private static final String PUT = "PUT";
    private static final String GET = "GET";
    private static final String COMMIT = "COMMIT";
    private static final String ABORT = "ABORT";
    private static final String BEGIN = "BEGIN";

    private static final String NUM_MEMBERS = "NUM_MEMBERS";
    private static final String SIZE = "SIZE";

    private static final String TPCC_POPULATION = "TPCC";

    private String serverAddress;
    private int serverPort;

    private Socket clientSocket;
    private ObjectOutputStream outToServer;
    private ObjectInputStream inFromServer;


    private boolean connectionOpen = false;


    private boolean transactional = false;

    private long lastOp=0L;

    private long interOpSamples=0L;

    private long interOpNumSamples=0L;

    private long lastBegin=0L;

    private long committedTxDurationSamples = 0L;

    private long committedTxDurationNumSamples = 0L;

    private long abortedTxDurationSamples = 0L;

    private long abortedTxDurationNumSamples = 0L;


    private boolean txBegin = false;

    private int statsLength;

    private InfinispanClientFactory factory;



    public InfinispanClient(String serverAddress, int serverPort, int statsLength, InfinispanClientFactory factory) throws IOException {

        this.statsLength = statsLength;

        this.factory = factory;

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        try {
            open();
            connectionOpen = true;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }




    }

    private void open() throws IOException {

        clientSocket = new Socket(serverAddress, serverPort);
        outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
        inFromServer = new ObjectInputStream(clientSocket.getInputStream());

    }

    public boolean begin(){

        transactional = true;

        txBegin = true;

        lastBegin = System.nanoTime();

        if(!connectionOpen){

            try {
                open();
                connectionOpen = true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }



        try {
            outToServer.writeObject(BEGIN);
            outToServer.flush();

            boolean ack = inFromServer.readBoolean();

            if(ack == true){

                if(DEBUG)
                    log.debug("BEGIN TX");

                return true;
            }
            else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally{
            if(transactional){
                    lastOp = System.nanoTime();
                }
        }


    }

    public boolean put(Object key, Object value){

        if(transactional){
            interOpSamples += System.nanoTime()-lastOp;
            interOpNumSamples++;
        }

        if(!connectionOpen){

            try {
                open();
                connectionOpen = true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }




        try {
            outToServer.writeObject(PUT);
            outToServer.writeObject(key);
            outToServer.writeObject(value);
            outToServer.flush();

            boolean ack = inFromServer.readBoolean();


            if(DEBUG && txBegin)
                    log.debug("PUT");



            return ack;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally{
            if(transactional){
                lastOp = System.nanoTime();
            }
        }

    }

    public boolean tpccPopulation(int numWarehouses, int slaveIndex, int numSlaves, long cLastMask, long olIdMask, long cIdMask){


        if(!connectionOpen){

            try {
                open();
                connectionOpen = true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }




        try {
            outToServer.writeObject(TPCC_POPULATION);
            outToServer.writeInt(numWarehouses);
            outToServer.writeInt(slaveIndex);
            outToServer.writeInt(numSlaves);
            outToServer.writeLong(cLastMask);
            outToServer.writeLong(olIdMask);
            outToServer.writeLong(cIdMask);
            outToServer.flush();

            boolean ack = inFromServer.readBoolean();


            return ack;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public Object get(Object key){

        if(transactional){
            interOpSamples += System.nanoTime()-lastOp;
            interOpNumSamples++;
        }

        if(!connectionOpen){

            try {
                open();
                connectionOpen = true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }


        try {
            outToServer.writeObject(GET);
            outToServer.writeObject(key);
            outToServer.flush();

            Object value = inFromServer.readObject();

            if(DEBUG && txBegin)
                    log.debug("GET");

            return value;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if(transactional){
                lastOp = System.nanoTime();
            }
        }

    }

    public boolean commit(){

        boolean ok = false;

        if(transactional){
            interOpSamples += System.nanoTime()-lastOp;
            interOpNumSamples++;
        }


        if(!connectionOpen){

            try {
                open();
                connectionOpen = true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }

        try {
            outToServer.writeObject(COMMIT);
            outToServer.flush();

            boolean ack = inFromServer.readBoolean();

            if(DEBUG && txBegin)
                    log.debug("COMMIT: "+ack);

            ok = ack;

            return ack;
        } catch (IOException e) {

            ok = false;

            e.printStackTrace();
            return false;
        }
        finally{

            transactional = false;

            if(txBegin){
              if(ok){
                  this.committedTxDurationSamples+=System.nanoTime()-this.lastBegin;
                  this.committedTxDurationNumSamples++;
              }
              else{
                  this.abortedTxDurationSamples+=System.nanoTime()-this.lastBegin;
                  this.abortedTxDurationNumSamples++;
              }

                txBegin = false;
            }



            if(abortedTxDurationNumSamples+committedTxDurationNumSamples == this.statsLength){

                dump();
            }

            /*
            log.info(">>> InterOps <<<");
            log.info("Total_InterOp_Time: "+this.interOpSamples);
            log.info("Num_InterOp_Samples: "+((this.interOpNumSamples*1.0)/1000.0)+" ms");
            if(this.interOpNumSamples>0){
                  log.info("Mean_InterOp_Time: "+(((this.interOpSamples*1.0)/(interOpNumSamples*1.0))/1000.0)+" ms");
            }
            else{
                 log.info("Mean_InterOp_Time: "+0);
            }
            log.info("******************");
            log.info("");

            transactional = false;
            this.interOpSamples = 0L;
            this.interOpNumSamples = 0L;
            this.lastOp = 0L;



            if(txBegin){
                log.info(">>> Tx <<<");
                if(ok){
                    log.info("Committed Tx Duration: "+(((System.nanoTime()-this.lastBegin)*1.0)/1000.0)+" ms");
                }
                else{
                    log.info("Aborted Tx Duration: "+(((System.nanoTime()-this.lastBegin)*1.0)/1000.0)+" ms");
                }
                log.info("****************");
                log.info("");
                txBegin = false;
            }

            */
        }

    }

    public boolean abort(){

        if(transactional){
            interOpSamples = System.nanoTime()-lastOp;
            interOpNumSamples++;
        }


        if(!connectionOpen){

            try {
                open();
                connectionOpen = true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }

        try {
            outToServer.writeObject(ABORT);
            outToServer.flush();

            boolean ack = inFromServer.readBoolean();

            transactional = false;

            return ack;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally{

            transactional = false;

            if(txBegin){

                  this.abortedTxDurationSamples+=System.nanoTime()-this.lastBegin;
                  this.abortedTxDurationNumSamples++;


                txBegin = false;
            }



            if(abortedTxDurationNumSamples+committedTxDurationNumSamples == this.statsLength){

                dump();
            }



            /*
            log.info(">>> InterOps <<<");
            log.info("Total_InterOp_Time: "+this.interOpSamples);
            log.info("Num_InterOp_Samples: "+((this.interOpNumSamples*1.0)/1000.0)+" ms");
            if(this.interOpNumSamples>0){
                  log.info("Mean_InterOp_Time: "+(((this.interOpSamples*1.0)/(interOpNumSamples*1.0))/1000.0)+" ms");
            }
            else{
                 log.info("Mean_InterOp_Time: "+0);
            }
            log.info("******************");
            log.info("");

            transactional = false;
            this.interOpSamples = 0L;
            this.interOpNumSamples = 0L;
            this.lastOp = 0L;


            if(txBegin){
                log.info(">>> Tx <<<");
                log.info("Aborted Tx Duration: "+(((System.nanoTime()-this.lastBegin)*1.0)/1000.0)+" ms");

                log.info("****************");
                log.info("");
                txBegin = false;
            }

            */

        }


    }

    private void dump(){

        if(log.isInfoEnabled()){
            //String output="\n"+"Total_InterOp_Time"+";"+"Num_InterOp_Samples"+";"+"Mean_InterOp_Time"+";"+"Total_CommittedTxDuration"+";"+"Num_CommittedTxDuration_Samples"+";"+"Mean_CommittedTxDuration"+";"+"Total_AbortedTxDuration"+";"+"Num_AbortedTxDuration_Samples"+";"+"Mean_AbortedTxDuration"+"\n";
            String output = "";
            double Mean_InterOp_Time = 0.0D;
            if(this.interOpNumSamples > 0L){
                Mean_InterOp_Time = (this.interOpSamples * 1.0)/(this.interOpNumSamples * 1.0);
            }


            output+= ""+this.interOpSamples+";"+this.interOpNumSamples+";"+Mean_InterOp_Time+";";


            double Mean_CommittedTxDuration = 0.0D;
            if(this.committedTxDurationNumSamples > 0L){
                Mean_CommittedTxDuration = (this.committedTxDurationSamples * 1.0)/(this.committedTxDurationNumSamples * 1.0);
            }



            output+= ""+this.committedTxDurationSamples+";"+this.committedTxDurationNumSamples+";"+Mean_CommittedTxDuration+";";

            double Mean_AbortedTxDuration = 0.0D;
            if(this.abortedTxDurationNumSamples > 0L){
                Mean_AbortedTxDuration = (this.abortedTxDurationSamples * 1.0)/(this.abortedTxDurationNumSamples * 1.0);
            }



            output+= ""+this.abortedTxDurationSamples+";"+this.abortedTxDurationNumSamples+";"+Mean_AbortedTxDuration+";";

            //log.info(output);
            factory.dumpStatsLine(output);

            this.committedTxDurationNumSamples = 0L;
            this.abortedTxDurationNumSamples = 0L;
            this.committedTxDurationSamples = 0L;
            this.abortedTxDurationSamples = 0L;
            this.interOpSamples = 0L;
            this.interOpNumSamples = 0L;
        }

    }

    public void close(){
        try {
            /*
            if(this.outToServer != null){

                this.outToServer.close();
                this.outToServer = null;

            }
            if(this.inFromServer != null){
                this.inFromServer.close();
                this.inFromServer = null;
            }
            */
            if(this.clientSocket != null){
                this.clientSocket.close();
                this.clientSocket = null;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            connectionOpen = false;
        }
    }

    public int getNumMembers(){
        if(!connectionOpen){

            try {
                open();
                connectionOpen = true;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }

        }

        try {
            outToServer.writeObject(NUM_MEMBERS);
            outToServer.flush();

            int ack = inFromServer.readInt();

            return ack;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public int getSize(){


        if(!connectionOpen){

            try {
                open();
                connectionOpen = true;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }

        }

        try {
            outToServer.writeObject(SIZE);
            outToServer.flush();

            int ack = inFromServer.readInt();

            return ack;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
