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

package eu.cloudtm.am.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
* @author Sebastiano Peluso
*/
public class ActuatorClient {




    private String ip;

    private int port = 8888;



    public ActuatorClient(String serverAddress){



        this.ip = serverAddress;

    }

    public ActuatorClient(String serverAddress, int port){

        this.ip = serverAddress;

        this.port = port;

    }

    public boolean setConfiguration(int numNodes, int replicationDegree){

        if(numNodes < 0 || replicationDegree < 0) {

            return false;
        }

        Socket clientSocket = null;
        ObjectOutputStream outToServer = null;
        ObjectInputStream inFromServer = null;

        boolean result = true;

        try {
            clientSocket = new Socket(this.ip, this.port);
            outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            inFromServer = new ObjectInputStream(clientSocket.getInputStream());

            outToServer.writeObject(eu.cloudtm.am.server.ActuatorCommand.SET_CONFIGURATION);
            outToServer.flush();

            outToServer.writeInt(numNodes);
            outToServer.writeInt(replicationDegree);
            outToServer.flush();

            result = (Boolean) inFromServer.readObject();


        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            result = false;
            e.printStackTrace();
        }


        return result;

    }



    public boolean addNewInfinispanNode(){

        Socket clientSocket = null;
        ObjectOutputStream outToServer = null;
        ObjectInputStream inFromServer = null;

        boolean result = true;

        try {
            clientSocket = new Socket(this.ip, this.port);
            outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            inFromServer = new ObjectInputStream(clientSocket.getInputStream());

            outToServer.writeObject(eu.cloudtm.am.server.ActuatorCommand.ADD_NEW_INFINISPAN_NODE);
            outToServer.flush();

            result = (Boolean) inFromServer.readObject();





        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            result = false;
            e.printStackTrace();
        }


        return result;
    }

    public boolean removeInfinispanNode(){

        Socket clientSocket = null;
        ObjectOutputStream outToServer = null;
        ObjectInputStream inFromServer = null;

        boolean result = true;

        try {
            clientSocket = new Socket(this.ip, this.port);
            outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            inFromServer = new ObjectInputStream(clientSocket.getInputStream());

            outToServer.writeObject(eu.cloudtm.am.server.ActuatorCommand.REMOVE_INFINISPAN_NODE);
            outToServer.flush();

            result = (Boolean) inFromServer.readObject();





        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            result = false;
            e.printStackTrace();
        }


        return result;
    }

    public boolean changeReplicationDegree(int replicationDegree){

        Socket clientSocket = null;
        ObjectOutputStream outToServer = null;
        ObjectInputStream inFromServer = null;

        boolean result = true;

        try {
            clientSocket = new Socket(this.ip, this.port);
            outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            inFromServer = new ObjectInputStream(clientSocket.getInputStream());

            outToServer.writeObject(eu.cloudtm.am.server.ActuatorCommand.SET_REPLICATION_DEGREE);
            outToServer.flush();

            outToServer.writeInt(replicationDegree);
            outToServer.flush();

            result = (Boolean) inFromServer.readObject();


        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            result = false;
            e.printStackTrace();
        }


        return result;
    }
}



