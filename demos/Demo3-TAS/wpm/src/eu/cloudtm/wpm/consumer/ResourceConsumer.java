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
 
package eu.cloudtm.wpm.consumer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import eu.reservoir.monitoring.appl.BasicConsumer;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import eu.reservoir.monitoring.distribution.udp.UDPDataPlaneConsumer;
import eu.reservoir.monitoring.distribution.udp.UDPDataPlaneConsumerNoNames;
import eu.reservoir.monitoring.im.dht.DHTInfoPlaneRoot;

/*
* @author Roberto Palmieri
*/
	public class ResourceConsumer {
		static String Local_DP_IP_Address;
		static int Local_DP_port_number;
		static String Local_IP_IP_Address;
		static int Local_IP_port_number;
		static int Local_ack_port_number;
		static int LogService_Transmission_Period;
		static String LogService_IP_Address;
		static int LogService_port_number;
		static long timeout;
		static String ID;
		
		// The Basic consumer
	    private BasicConsumer consumer;
	    private String id;
	
	    public ResourceConsumer(String identf,String DP_addr, int DP_port,String IP_addr,int IP_port,String logService_addr,int logService_port,int trans_period) {
		// set up a BasicConsumer
		id = identf;
	    consumer = new BasicConsumer();
		// set up an IP address for data
		InetSocketAddress address = new InetSocketAddress(DP_addr,DP_port);
		// set up data plane withoud name
		consumer.setDataPlane(new UDPDataPlaneConsumerNoNames(address));
		// set up data plane with name
		//consumer.setDataPlane(new UDPDataPlaneConsumer(address));
		InfoPlane inf_pl = null;
		try{
			inf_pl = new DHTInfoPlaneRoot(IP_addr, IP_port);
			consumer.setInfoPlane(inf_pl);
			consumer.setReporter(new ResourceReporter(inf_pl,id,trans_period));
			int num_try = 0;
			boolean isConnected = true;
			do{
				isConnected = consumer.connect();
				if(!isConnected){
					if(num_try > 30)
						System.exit(0);
					System.out.println("Consumer already up...");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					num_try++;
				}
			}while(!isConnected);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	    
    public static void main(String [] args) {
    	loadParametersFromRegistry();
    	new SenderConsumer(LogService_IP_Address,LogService_port_number,timeout);
    	//Disable for making the comunication simple 
    	//new AckConsumer(Local_ack_port_number);
    	new ResourceConsumer(ID,Local_DP_IP_Address,Local_DP_port_number,Local_IP_IP_Address,Local_IP_port_number,LogService_IP_Address,LogService_port_number,LogService_Transmission_Period);
	    System.err.println("Consumer UDP listening on "+Local_DP_IP_Address+"/"+Local_DP_port_number);
    }
    
    private static void loadParametersFromRegistry(){
    	String propsFile = "config/resource_consumer.config";
    	Properties props = new Properties();
		try {
			props.load(new FileInputStream(propsFile));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Local_DP_IP_Address = props.getProperty("Local_DP_IP_Address");
		Local_DP_port_number = Integer.parseInt(props.getProperty("Local_DP_port_number"));
		Local_IP_IP_Address = props.getProperty("Local_IP_IP_Address");
		Local_IP_port_number = Integer.parseInt(props.getProperty("Local_IP_port_number"));
		Local_ack_port_number = Integer.parseInt(props.getProperty("Local_ack_port_number"));
		LogService_Transmission_Period = Integer.parseInt(props.getProperty("LogService_Transmission_Period"));
		LogService_IP_Address = props.getProperty("LogService_IP_Address");
		LogService_port_number = Integer.parseInt(props.getProperty("LogService_port_number"));
		timeout = Long.parseLong(props.getProperty("SenderTimeout"));
		ID = props.getProperty("ID");
		if(ID == null || ID.equals("")){
			try {
				InetAddress thisIp = InetAddress.getLocalHost();
				ID = thisIp.getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
    }
}

