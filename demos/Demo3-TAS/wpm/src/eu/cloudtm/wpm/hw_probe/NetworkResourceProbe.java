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
 
package eu.cloudtm.wpm.hw_probe;

import java.util.ArrayList;

import eu.cloudtm.resources.MonitorableResources;
import eu.reservoir.monitoring.core.AbstractProbe;
import eu.reservoir.monitoring.core.DefaultProbeAttribute;
import eu.reservoir.monitoring.core.DefaultProbeValue;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.ProbeMeasurement;
import eu.reservoir.monitoring.core.ProbeValue;
import eu.reservoir.monitoring.core.ProducerMeasurement;
import eu.reservoir.monitoring.core.Rational;
import eu.reservoir.monitoring.core.TypeException;

/*
* @author Roberto Palmieri
*/
public class NetworkResourceProbe extends AbstractProbe implements Probe {
	private NetworkInfo monitored_network;
	//probe_timeout in millisecond
	public NetworkResourceProbe(String name,int probe_timeout){
		setName(name);
		//Logical group of VM
		//ID gr_id = new ID(group_id);
		//setGroupID(gr_id);
		//Specified in measurements per hour
		int milliseconds_each_hour = 3600000;
		Rational probe_rate = new Rational(milliseconds_each_hour,1000);
		try{
			probe_rate = new Rational(milliseconds_each_hour, probe_timeout);
		}catch(Exception e){
			e.printStackTrace();
		}
        setDataRate(probe_rate);
        monitored_network = new NetworkInfo();
        setProbeAttributes();
	}
	private void setProbeAttributes(){
		int attributeKey = 0;
		//Network attributes
		for(int i=0;i<monitored_network.getNumberInterfaces();i++){
			addProbeAttribute(new DefaultProbeAttribute(attributeKey++, i+"-receivedBytes", ProbeAttributeType.LONG, "byte"));
			addProbeAttribute(new DefaultProbeAttribute(attributeKey++, i+"-transmittedBytes", ProbeAttributeType.LONG, "byte"));
			addProbeAttribute(new DefaultProbeAttribute(attributeKey++, i+"-receivedBytesPerSecond", ProbeAttributeType.DOUBLE, "Byte/sec"));
			addProbeAttribute(new DefaultProbeAttribute(attributeKey++, i+"-transmittedBytesPerSecond", ProbeAttributeType.DOUBLE, "Byte/sec"));
		}
	}
	
	public ProbeMeasurement collect() {
		System.out.println("Start collecting at: "+System.currentTimeMillis());
		int attributeKey = 0;
		// list of proble values
		ArrayList<ProbeValue> list = new ArrayList<ProbeValue>();
		//collect data from network
		ArrayList<NetworkValue> netValue = null;
		netValue = monitored_network.getNetworkValues(); 
		for(NetworkValue net : netValue){
			try {
				list.add(new DefaultProbeValue(attributeKey++, net.getRx_bytes()));
				list.add(new DefaultProbeValue(attributeKey++, net.getTx_bytes()));
				list.add(new DefaultProbeValue(attributeKey++, net.getRx_brandwidth()));
				list.add(new DefaultProbeValue(attributeKey++, net.getTx_brandwidth()));
			} catch (TypeException e) {
				e.printStackTrace();
			}
		}
		return new ProducerMeasurement(this, list, MonitorableResources.NETWORK.toString());	
	}
}