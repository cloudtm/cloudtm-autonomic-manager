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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;

/*
* @author Sebastiano Peluso
*/ 

public class InfinispanClientFactory {

    //private String serverAddress;

    private String[] serverAddressList;

    public static int serverPort = 2222;

    private int statsLength;

    private int clientFactoryIndex;


    private PrintWriter out = null;



    public InfinispanClientFactory(String serverAddressList, int clientFactoryIndex){

        this.clientFactoryIndex = clientFactoryIndex;

        StringTokenizer tokenizer = new StringTokenizer(serverAddressList, " ");

        int numTokens = tokenizer.countTokens();

        this.serverAddressList = new String[numTokens];

        int i = 0;
        while(tokenizer.hasMoreTokens()){
            this.serverAddressList[i] = tokenizer.nextToken();

            i++;
        }



        Properties props = new Properties();


        try {


            //URL props_url = InfinispanClientFactory.class.getResource("infinispan_client.properties");
            //if(props_url == null)
             //   throw new IOException("Could not find properties file: infinispan_client.properties");




            //InputStream  is = props_url.openStream();



            InputStream is = this.getClass().getClassLoader().getResourceAsStream("infinispan_client.properties");
            if(is==null){
            is = this.getClass().getResourceAsStream("/infinispan_client.properties");
            }



            props.load(is);
            is.close();


            File statsFile = new File("clientResults.txt");

            this.out = new PrintWriter(statsFile);

            if(this.out!= null){

                this.out.println("Total_InterOp_Time"+";"+"Num_InterOp_Samples"+";"+"Mean_InterOp_Time"+";"+"Total_CommittedTxDuration"+";"+"Num_CommittedTxDuration_Samples"+";"+"Mean_CommittedTxDuration"+";"+"Total_AbortedTxDuration"+";"+"Num_AbortedTxDuration_Samples"+";"+"Mean_AbortedTxDuration");
                this.out.flush();

            }



        } catch (IOException e) {
            e.printStackTrace();
        }

        if(props.get("SamplingLength") == null){
            this.statsLength = 100;
        }
        else{
            this.statsLength = Integer.parseInt((String) props.get("SamplingLength"));
        }


    }


    public InfinispanClient createClient() throws IOException {

        int numActiveServers = 0;

        Socket clientSocket = null;

        boolean[] active = new boolean[this.serverAddressList.length];

        for(int i = 0; i< this.serverAddressList.length; i++){

            try {
                clientSocket = new Socket(this.serverAddressList[i], serverPort);
                numActiveServers++;
                active[i] = true;
            } catch (IOException e) {
                active[i]=false;
            }
            finally {
                if(clientSocket != null){
                    try {
                        clientSocket.close();
                        clientSocket = null;
                    } catch (IOException e) {

                    }
                }
            }


        }

        String selectedAddress = null;

        if(numActiveServers == 0){
            return null;
        }
        else{
            int serverIndex = this.clientFactoryIndex % numActiveServers;
            int offset, i;
            for (i = 0, offset = 0; i < numActiveServers; offset++){

                if(active[offset]){
                    if(serverIndex == i){
                        selectedAddress = this.serverAddressList[offset];
                    }
                    i++;
                }

            }

            System.out.println("Connecting to "+selectedAddress+"...");

            return new InfinispanClient(selectedAddress, serverPort, this.statsLength, this);


        }


    }



    public synchronized void dumpStatsLine(String line){

        if(out != null){
            out.println(line);
            out.flush();
        }


    }



}
