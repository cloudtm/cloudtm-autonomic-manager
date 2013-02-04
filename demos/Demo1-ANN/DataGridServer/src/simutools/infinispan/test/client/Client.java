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
 
package simutools.infinispan.test.client;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * @author Sebastiano Peluso
 */
public class Client {


    private static final String PUT = "PUT";
    private static final String GET = "GET";
    private static final String COMMIT = "COMMIT";
    private static final String ABORT = "ABORT";
    private static final String BEGIN = "BEGIN";

    private static final String SIZE = "SIZE";

    private static final String LOCAL_SIZE = "LOCAL_SIZE";

    private static final String NUM_MEMBERS = "NUM_MEMBERS";


    public static void main(String[] args){

        // read arguments
        if (args.length!=2) {
            System.out.println("Usage: java Client <port> <server_host>");
            System.exit(-1);
        }
        int port = Integer.parseInt(args[0]);
        String server = args[1];


        Socket clientSocket = null;
        ObjectOutputStream outToServer = null;
        ObjectInputStream inFromServer = null;

        BufferedReader inFromCommandLine = null;

        try {
            clientSocket = new Socket(server, port);
            outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            inFromServer = new ObjectInputStream(clientSocket.getInputStream());

            inFromCommandLine = new BufferedReader(new InputStreamReader(System.in));

            boolean end = false;

            while(!end){

                System.out.println("*** Write a command ***");
                System.out.println();
                String line = inFromCommandLine.readLine();

                StringTokenizer tokenizer = new StringTokenizer(line);

                String command = tokenizer.nextToken();

                if(command!= null && command.equals(BEGIN)){

                    outToServer.writeObject(command);
                    outToServer.flush();

                    boolean ack = inFromServer.readBoolean();

                    if(ack == true)
                        System.out.println("Transaction Begin");
                    else{
                        end = true;
                        System.out.println("Transaction Error");
                    }





                }
                else if(command!= null && command.equals(PUT)){

                    Object key = tokenizer.nextToken();
                    Object value = tokenizer.nextToken();



                    outToServer.writeObject(command);
                    outToServer.writeObject(key);
                    outToServer.writeObject(value);
                    outToServer.flush();

                    boolean ack = inFromServer.readBoolean();

                    if(ack == true)
                        System.out.println("Put OK");
                    else{
                        end = true;
                        System.out.println("Put Error");
                    }



                }
                else if(command!= null && command.equals(GET)){

                    Object key = tokenizer.nextToken();
                    outToServer.writeObject(command);
                    outToServer.writeObject(key);
                    outToServer.flush();

                    Object value = inFromServer.readObject();

                    System.out.println("Get OK: Value -> "+value);

                }
                else if(command!= null && command.equals(COMMIT)){

                    outToServer.writeObject(command);
                    outToServer.flush();

                    boolean ack = inFromServer.readBoolean();

                    if(ack == true)
                        System.out.println("Transaction Commit");
                    else{
                        end = true;
                        System.out.println("Commit Error");
                    }

                    end = true;


                }
                else if(command!= null && command.equals(ABORT)){

                    outToServer.writeObject(command);
                    outToServer.flush();

                    boolean ack = inFromServer.readBoolean();

                    if(ack == true)
                        System.out.println("Transaction Abort");
                    else{
                        end = true;
                        System.out.println("Commit Abort");
                    }

                    end = true;

                }
                else if(command!= null && command.equals(LOCAL_SIZE)){

                    outToServer.writeObject(command);
                    outToServer.flush();

                    int size = inFromServer.readInt();


                    System.out.println(size);

                }
                else if(command!= null && command.equals(NUM_MEMBERS)){

                    outToServer.writeObject(command);
                    outToServer.flush();

                    int size = inFromServer.readInt();


                    System.out.println(size);

                }
                else{
                    end = true;
                    throw new RuntimeException("Unrecognized command!");

                }


            }




        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Get Error");

        } finally{
            if(inFromServer != null){
                try {
                    inFromServer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(outToServer != null){
                try {
                    outToServer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(clientSocket != null){
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
