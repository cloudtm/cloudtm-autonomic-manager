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
public class MemoryResourceProbe extends AbstractProbe implements Probe {
	private MemoryInfo monitored_memory;
	//probe_timeout in millisecond
	public MemoryResourceProbe(String name,int probe_timeout){
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
        //this.identification = identification;
        monitored_memory = new MemoryInfo();
        setProbeAttributes();
	}
	private void setProbeAttributes(){
		int attributeKey = 0;
		addProbeAttribute(new DefaultProbeAttribute(attributeKey++, "free", ProbeAttributeType.LONG, "bytes"));
		addProbeAttribute(new DefaultProbeAttribute(attributeKey++, "used", ProbeAttributeType.LONG, "bytes"));
	}
	
	public ProbeMeasurement collect() {
		System.out.println("Start collecting at: "+System.currentTimeMillis());
		int attributeKey = 0;
		// list of proble values
		ArrayList<ProbeValue> list = new ArrayList<ProbeValue>();
		MemoryValue memValue = null;
		//collect data from ram
		memValue = monitored_memory.getMemoryValues();
		try {
			list.add(new DefaultProbeValue(attributeKey++, memValue.getFree()));
			list.add(new DefaultProbeValue(attributeKey++, memValue.getUsed()));
		} catch (TypeException e) {
			e.printStackTrace();
		}
		return new ProducerMeasurement(this, list, MonitorableResources.MEMORY.toString());	
	}
}