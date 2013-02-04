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

package eu.cloudtm.am.server;


import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/*
* @author Sebastiano Peluso
*/
public class ActuatorService implements Runnable{

    private Socket connection;

    private String infinispanServerDirectory = null;



    private String[] nodes;

    private String userInfinispanNodes;

    private int infinispanJmxPort;
    private String infinispanJmxDomain;
    private String infinispanJmxCacheName;
    private String infinispanJmxReplicationType;
    private String infinispanJmxCacheManager;

    public ActuatorService(Socket connection, String infinispanServerDirectory, String listNodes, String userInfinispanNodes, int infinispanJmxPort, String infinispanJmxDomain, String infinispanJmxCacheName,
                String infinispanJmxReplicationType, String infinispanJmxCacheManager ){
        this.connection = connection;
        this.infinispanServerDirectory = infinispanServerDirectory;


        this.userInfinispanNodes = userInfinispanNodes;

        this.infinispanJmxPort =  infinispanJmxPort;
        this.infinispanJmxDomain = infinispanJmxDomain;
        this.infinispanJmxCacheName = infinispanJmxCacheName;
        this.infinispanJmxReplicationType = infinispanJmxReplicationType;
        this.infinispanJmxCacheManager = infinispanJmxCacheManager;

        StringTokenizer tokenizer = new StringTokenizer(listNodes, " ");

        int numTokens = tokenizer.countTokens();

        this.nodes = new String[numTokens];

        int i = 0;
        while(tokenizer.hasMoreTokens()){
            this.nodes[i] = tokenizer.nextToken();

            i++;
        }
    }


    public void run() {

        if(this.connection != null){

            BufferedReader in = null;

            ObjectInputStream objectIn = null;

            ObjectOutputStream objectOut = null;

            try {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                objectIn = new ObjectInputStream(connection.getInputStream());

                objectOut = new ObjectOutputStream(connection.getOutputStream());

                String command = (String) objectIn.readObject();

                String selectedIpAddress=null;

                if(ActuatorCommand.ADD_NEW_INFINISPAN_NODE.equals(command)){


                    selectedIpAddress = selectInactiveNode();

                    System.out.println("Add new Infinispan Server on "+selectedIpAddress+"...");

                    ProcessBuilder pb = new ProcessBuilder("bin/newInfinispanNode.sh", selectedIpAddress, infinispanServerDirectory, userInfinispanNodes);

                    /*This is for changing environment variables and working directory*/
                    //Map<String, String> env = pb.environment();
                    //env.put("VAR1", "myValue");
                    //env.remove("OTHERVAR");
                    //env.put("VAR2", env.get("VAR1") + "suffix");
                    //pb.directory(new File("myDir"));
                    Process p = pb.start();

                    /*This is for getting the script's output*/
                    String output = loadStream(p.getInputStream());
                    System.out.println("\nStandard Output:\n");
                    System.out.println(output);

                    p.waitFor();


                    objectOut.writeObject(true);
                    objectOut.flush();

                }
                else if(ActuatorCommand.REMOVE_INFINISPAN_NODE.equals(command)){


                    selectedIpAddress = selectActiveNode();


                    System.out.println("Remove Infinispan Server on "+selectedIpAddress+"...");

                    if(selectedIpAddress != null){


                        ProcessBuilder pb = new ProcessBuilder("bin/removeInfinispanNode.sh", selectedIpAddress, infinispanServerDirectory, userInfinispanNodes);
                        /*This is for changing environment variables and working directory*/
                        //Map<String, String> env = pb.environment();
                        //env.put("VAR1", "myValue");
                        //env.remove("OTHERVAR");
                        //env.put("VAR2", env.get("VAR1") + "suffix");
                        //pb.directory(new File("myDir"));
                        Process p = pb.start();

                        /*This is for getting the script's output*/
                        String output = loadStream(p.getInputStream());
                        System.out.println("\nStandard Output:\n");
                        System.out.println(output);

                        p.waitFor();


                        objectOut.writeObject(true);
                        objectOut.flush();


                    }
                    else{
                        objectOut.writeObject(false);
                        objectOut.flush();
                    }
                }
                else if(ActuatorCommand.SET_REPLICATION_DEGREE.equals(command)){
                    int replDeg =  objectIn.readInt();
                    String activeNode = selectActiveNode();
                    changeReplicationDegree(replDeg, activeNode);

                    objectOut.writeObject(true);
                    objectOut.flush();
                }
                else if(ActuatorCommand.SET_CONFIGURATION.equals(command)){
                    int numNodes = objectIn.readInt();

                    int replDegree = objectIn.readInt();

                    boolean result = true;

                    if(numNodes < 0 || replDegree < 0){
                        result = false;
                    }
                    else{

                        String[] allActiveNodes = selectAllActiveNodes();

                        int activeSize = 0;


                        if(allActiveNodes != null){
                            activeSize = allActiveNodes.length;
                        }

                        int op = numNodes - activeSize;
                        int currentOp = op;
                        int i = 0;

                        if(op > 0){//Create nodes

                            while(currentOp != 0){

                                selectedIpAddress = selectInactiveNode();

                                System.out.println("Add new Infinispan Server on "+selectedIpAddress+"...");

                                ProcessBuilder pb = new ProcessBuilder("bin/newInfinispanNode.sh", selectedIpAddress, infinispanServerDirectory, userInfinispanNodes);

                                /*This is for changing environment variables and working directory*/
                                //Map<String, String> env = pb.environment();
                                //env.put("VAR1", "myValue");
                                //env.remove("OTHERVAR");
                                //env.put("VAR2", env.get("VAR1") + "suffix");
                                //pb.directory(new File("myDir"));
                                Process p = pb.start();

                                /*This is for getting the script's output*/
                                String output = loadStream(p.getInputStream());
                                System.out.println("\nStandard Output:\n");
                                System.out.println(output);

                                p.waitFor();


                                currentOp --;
                            }

                        }
                        else if (op < 0){//Remove nodes

                            while(currentOp != 0){

                                selectedIpAddress = allActiveNodes[i];

                                System.out.println("Remove Infinispan Server on "+selectedIpAddress+"...");

                                ProcessBuilder pb = new ProcessBuilder("bin/removeInfinispanNode.sh", selectedIpAddress, infinispanServerDirectory, userInfinispanNodes);
                                /*This is for changing environment variables and working directory*/
                                //Map<String, String> env = pb.environment();
                                //env.put("VAR1", "myValue");
                                //env.remove("OTHERVAR");
                                //env.put("VAR2", env.get("VAR1") + "suffix");
                                //pb.directory(new File("myDir"));
                                Process p = pb.start();

                                /*This is for getting the script's output*/
                                String output = loadStream(p.getInputStream());
                                System.out.println("\nStandard Output:\n");
                                System.out.println(output);

                                p.waitFor();


                                currentOp ++;
                                i++;
                            }

                        }

                        System.out.println("Change the replication degree...");

                        String activeNode = selectActiveNode();

                        int newReplicationDegree = replDegree+1;

                        while(newReplicationDegree != newReplicationDegree){
                            Thread.sleep(1);

                            changeReplicationDegree(replDegree, activeNode);

                            newReplicationDegree = getReplicationDegree(activeNode);


                        }


                        System.out.println("Replication degree changed.");



                    }

                    objectOut.writeObject(result);
                    objectOut.flush();

                }
                else{
                    throw new Exception("Command not recognized");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
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

                if(this.connection != null){
                    try {
                        this.connection.close();
                        this.connection = null;
                    } catch (IOException e) {
                        this.connection = null;
                    }

                }
            }

        }
    }

    private static String loadStream(InputStream s) throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=br.readLine()) != null)
            sb.append(line).append("\n");
        return sb.toString();
    }

    private String selectActiveNode(){

        Socket clientSocket = null;
        String result = null;

        for(int i = 0; i< this.nodes.length; i++){

            try {

                //System.out.println("Ping: "+this.nodes[i]);
                clientSocket = new Socket(this.nodes[i], 2222);

                result = nodes[i];

            } catch (IOException e) {

            }
            finally {
                if(clientSocket != null){
                    try {
                        clientSocket.close();
                        clientSocket = null;
                    } catch (IOException e) {

                    }
                }

                if(result != null){
                    break;
                }
            }


        }


        return result;


    }

    private String[] selectAllActiveNodes(){

        Socket clientSocket = null;

        List<String> list = new LinkedList<String>();

        for(int i = 0; i< this.nodes.length; i++){

            try {

                //System.out.println("Ping: "+this.nodes[i]);
                clientSocket = new Socket(this.nodes[i], 2222);

                list.add(this.nodes[i]);

            } catch (IOException e) {

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

        if(!list.isEmpty()){
           return list.toArray(new String[0]); //0 is OK! Don't worry
        }

        return null;


    }

    private String selectInactiveNode(){

        Socket clientSocket = null;
        String result = null;

        for(int i = 0; i< this.nodes.length; i++){

            try {

                //System.out.println("Ping: "+this.nodes[i]);
                clientSocket = new Socket(this.nodes[i], 2222);



            } catch (IOException e) {
                 result = this.nodes[i];
            }
            finally {
                if(clientSocket != null){
                    try {
                        clientSocket.close();
                        clientSocket = null;
                    } catch (IOException e) {

                    }
                }

                if(result != null){
                    break;
                }
            }


        }


        return result;


    }


    private boolean changeReplicationDegree(int replDeg, String activeNode){

        JMXServiceURL url = null;
    	JMXConnector jmxc = null;
        MBeanServerConnection connection;
        boolean result = false;

        System.out.println("Change replication degree to " + replDeg + " on " + activeNode);

        try {
            url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+activeNode+":"+infinispanJmxPort+"/jmxrmi");
            jmxc = JMXConnectorFactory.connect(url, null);

            connection = jmxc.getMBeanServerConnection();

            String commonName = new StringBuilder("org.infinispan:type=Cache")
                .append(",name=").append(ObjectName.quote(this.infinispanJmxCacheName + "(" + this.infinispanJmxReplicationType + ")"))
                .append(",manager=").append(ObjectName.quote(this.infinispanJmxCacheManager))
                .append(",component=").toString();

            ObjectName dpManager = new ObjectName(commonName + "DataPlacementManager");


            connection.invoke(dpManager, "setReplicationDegree", new Object[]{replDeg}, new String[] {"int"});



            result = true;
        } catch (MalformedURLException e) {
            result = false;
            e.printStackTrace();
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }


        return result;
    }


    private int getReplicationDegree(String activeNode){

        JMXServiceURL url = null;
    	JMXConnector jmxc = null;
        MBeanServerConnection connection;
        int result = -1;

        System.out.println("Get replication degree to on " + activeNode);

        try {
            url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+activeNode+":"+infinispanJmxPort+"/jmxrmi");
            jmxc = JMXConnectorFactory.connect(url, null);

            connection = jmxc.getMBeanServerConnection();

            String commonName = new StringBuilder("org.infinispan:type=Cache")
                .append(",name=").append(ObjectName.quote(this.infinispanJmxCacheName + "(" + this.infinispanJmxReplicationType + ")"))
                .append(",manager=").append(ObjectName.quote(this.infinispanJmxCacheManager))
                .append(",component=").toString();


            ObjectName customStats = new ObjectName(commonName + "ExtendedStatistics");



            result = (Integer) connection.getAttribute(customStats, "ReplicationDegree");


        } catch (MalformedURLException e) {
            result = -1;
            e.printStackTrace();
        } catch (IOException e) {
            result = -1;
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            result = -1;
            e.printStackTrace();
        } catch (MBeanException e) {
            result = -1;
            e.printStackTrace();
        } catch (ReflectionException e) {
            result = -1;
            e.printStackTrace();

        } catch (InstanceNotFoundException e) {
            result = -1;
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            result = -1;
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return result;
    }

}
