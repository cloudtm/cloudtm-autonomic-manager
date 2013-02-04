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
 
package eu.cloudtm.wpm.producer;


import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import eu.cloudtm.resources.MonitorableResources;
import eu.reservoir.monitoring.appl.DynamicControl;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.Rational;
import eu.reservoir.monitoring.distribution.udp.UDPDataPlaneProducerNoNames;
import eu.reservoir.monitoring.im.dht.DHTInfoPlane;

/*
* @author Roberto Palmieri
*/
public class ResourcesController extends DynamicControl {
	// the start time
    private long startTime = 0;
    private String cpuComponentID;
    private String memComponentID;
    private String dskComponentID;
    private String netComponentID;
    private String jmxComponentID;
    private String IP_Address;
    private String groupId;
    private String providerId;
    private int collection_timeout;

    // the DataSource
    private ResourcesDataSource dataSource;

    public ResourcesController(String consumer_DP_addr, int consumer_DP_port, String consumer_IP_addr, int consumer_IP_local_port, int consumer_IP_remote_port,String hostname) {
		super("Probes Controller");
		//set up counter
		//set up data source
		InetSocketAddress address = new InetSocketAddress(consumer_DP_addr, consumer_DP_port);
		
		// set up multicast addresses
		//MulticastAddress dataGroup = new MulticastAddress(addr, port);
		//dataSource.setDataPlane(new MulticastDataPlaneProducer(dataGroup));
		//without attributes
		//dataSource.setDataPlane(new UDPDataPlaneProducerNoNames(address));
		//with attributes
		boolean isConnected = true;
		dataSource = new ResourcesDataSource(hostname);
		//With names
		//dataSource.setDataPlane(new UDPDataPlaneProducer(address));
		//Without names
		dataSource.setDataPlane(new UDPDataPlaneProducerNoNames(address));
		
		dataSource.setInfoPlane(new DHTInfoPlane(consumer_IP_addr, consumer_IP_remote_port, consumer_IP_local_port));
		int num_try = 0;
		do{
			isConnected = dataSource.connect();
			if(!isConnected){
				if(num_try > 30)
					System.exit(0);
				System.out.println("Waiting for Consumer Process or Producer already up...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				num_try++;
			}
		}while(!isConnected);
		
    }

    /**
     * Initialize.
     */
    protected void controlInitialize() {
    	System.out.println("Inizializing control plane");
    	startTime = System.currentTimeMillis();
    	loadParametersFromRegistry();
    }

    /**
     * Actually evaluate something.
     */
    protected void controlEvaluate() {
		long now = System.currentTimeMillis();
		long diff = (now - startTime) / 1000;
		System.out.println(diff + ": " + this + " seen " + dataSource.getProbes().size());
		
		if(dataSource.getProbes().size() == 0){
			dataSource.addProbe(cpuComponentID+":"+IP_Address+":"+groupId+":"+providerId,collection_timeout,MonitorableResources.CPU);
			dataSource.addProbe(memComponentID+":"+IP_Address+":"+groupId+":"+providerId,collection_timeout,MonitorableResources.MEMORY);
			dataSource.addProbe(netComponentID+":"+IP_Address+":"+groupId+":"+providerId,collection_timeout,MonitorableResources.NETWORK);
			dataSource.addProbe(dskComponentID+":"+IP_Address+":"+groupId+":"+providerId,collection_timeout,MonitorableResources.DISK);
			dataSource.addProbe(jmxComponentID+":"+IP_Address+":"+groupId+":"+providerId,collection_timeout,MonitorableResources.JMX);
		}
    }

    /**
     * Cleanup
     */
    protected void controlCleanup() {
    	System.out.println("Clean Control");
    	//this.activateControl();
    }
    
    public void setProbeDataRate(String name, int probe_timeout) {
    	Probe probe = dataSource.getProbeByName(name);
    	if(probe == null)
    		return;
    	int milliseconds_each_hour = 3600000;
		Rational probe_rate = new Rational(milliseconds_each_hour,1000);
		try{
			probe_rate = new Rational(milliseconds_each_hour, probe_timeout);
		}catch(Exception e){
			e.printStackTrace();
		}
		probe.setDataRate(probe_rate);
    }
    
    private void loadParametersFromRegistry(){
    	String propsFile = "config/resource_controller.config";
    	Properties props = new Properties();
		try {
			props.load(new FileInputStream(propsFile));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		cpuComponentID = props.getProperty("CPU_Component_ID");
		memComponentID = props.getProperty("MEM_Component_ID");
		netComponentID = props.getProperty("NET_Component_ID");
		dskComponentID = props.getProperty("DSK_Component_ID");
		jmxComponentID = props.getProperty("JMX_Component_ID");
		IP_Address = props.getProperty("Producer_IP_Address");
		if(IP_Address == null || IP_Address.equals("")){
			try {
				InetAddress thisIp = InetAddress.getLocalHost();
				IP_Address = thisIp.getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		groupId = props.getProperty("Producer_Group");
		providerId = props.getProperty("Producer_Provider");
		collection_timeout = Integer.parseInt(props.getProperty("Collect_Timeout"));
    }
    
}
