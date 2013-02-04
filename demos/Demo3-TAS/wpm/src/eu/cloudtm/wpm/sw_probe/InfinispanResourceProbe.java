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
 
package eu.cloudtm.wpm.sw_probe;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

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
public class InfinispanResourceProbe extends AbstractProbe implements Probe {
	static InfinispanInfo monitored_infinispan;
	static String addr;
	static int port_num; 
	static String cache_name;
	static String jmxDomain_primary;
	static String jmxDomain_secondary;
	static String replicationType;
	static String cacheManager;
	static boolean JBossEnabled;
	static boolean resetStatis;
	
	//probe_timeout in millisecond
	public InfinispanResourceProbe(String name,int probe_timeout){
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
        //retrive JMX and infinispan parameters
        loadParametersFromRegistry();
        monitored_infinispan = new InfinispanInfo(addr, port_num, cache_name, jmxDomain_primary,replicationType,cacheManager,JBossEnabled,resetStatis,jmxDomain_secondary);
        setProbeAttributesRuntime();
        //setProbeAttributes(cache_name);
	}
	
	private void setProbeAttributesRuntime(){
		try {
			int attributeKey = 0;
			ObjectName obj_mbeans;
			obj_mbeans = new ObjectName(jmxDomain_primary+":type=Cache,*");
			Set<ObjectName> mbeans = monitored_infinispan.mbsc.queryNames(obj_mbeans, null);
			if(mbeans == null || mbeans.size() == 0){
				obj_mbeans = new ObjectName(jmxDomain_secondary+":type=Cache,*");
				mbeans = monitored_infinispan.mbsc.queryNames(obj_mbeans, null);
			}
	        for(ObjectName on : mbeans) {
	        	if(on.getCanonicalName().contains("component=Cache"))
	        		continue;
	        	MBeanAttributeInfo[] atts = monitored_infinispan.mbsc.getMBeanInfo(on).getAttributes();
	        	for(MBeanAttributeInfo att : atts){
	        		//control added to be safe in case the name is null
	        		if(att.getName() == null){
	        			continue;
	        		}
	        		String nameAttribute =  att.getName().substring(0,1).toUpperCase() + att.getName().substring(1);
	        		if(attributeKey == 33){
        				int a = 0;
        			}
	        		if(nameAttribute.equalsIgnoreCase("statisticsEnabled"))
	        			continue;
					if(att.getType().equals("double")){
						addProbeAttribute(new DefaultProbeAttribute(attributeKey++, nameAttribute, ProbeAttributeType.DOUBLE, ""));
					}else if(att.getType().equals("long")){
						addProbeAttribute(new DefaultProbeAttribute(attributeKey++, nameAttribute, ProbeAttributeType.LONG, ""));
					}else if(att.getType().equals("int")){
						addProbeAttribute(new DefaultProbeAttribute(attributeKey++, nameAttribute, ProbeAttributeType.INTEGER, ""));
					}else if(att.getType().equals("java.lang.String")){
						addProbeAttribute(new DefaultProbeAttribute(attributeKey++, nameAttribute, ProbeAttributeType.STRING, ""));
					}else if(att.getType().equals("java.util.Map")){
						addProbeAttribute(new DefaultProbeAttribute(attributeKey++, nameAttribute, ProbeAttributeType.STRING, ""));
					}else if(att.getType().equals("boolean")){
						addProbeAttribute(new DefaultProbeAttribute(attributeKey++, nameAttribute, ProbeAttributeType.BOOLEAN, ""));
					}else if(att.getType().equals("short")){
						addProbeAttribute(new DefaultProbeAttribute(attributeKey++, nameAttribute, ProbeAttributeType.SHORT, ""));
					}else{
						addProbeAttribute(new DefaultProbeAttribute(attributeKey++, nameAttribute, ProbeAttributeType.STRING, ""));
						System.out.println(att.getType());
						//throw new RuntimeException("Format not supported!");
					}
					//System.out.println("Added "+att.getName()+":"+(attributeKey-1));
					//System.out.println(att.getType());
	        	}
	        	
	        	//invoking_method.invoke(inf_value,(Double)mbsc.getAttribute(statisticsComponent,f.getName()));
	        	
	        	//System.out.println("---\n");
	        }
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (IntrospectionException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public ProbeMeasurement collect() {
		System.out.println("Start collecting Infinispan Probe at: "+System.currentTimeMillis());
		// list of proble values
		ArrayList<ProbeValue> list = new ArrayList<ProbeValue>();
		int attributeKey = 0;
		try {
			ObjectName obj_mbeans;
			obj_mbeans = new ObjectName(jmxDomain_primary+":type=Cache,*");
			Set<ObjectName> mbeans = monitored_infinispan.mbsc.queryNames(obj_mbeans, null);
			if(mbeans == null || mbeans.size() == 0){
				obj_mbeans = new ObjectName(jmxDomain_secondary+":type=Cache,*");
				mbeans = monitored_infinispan.mbsc.queryNames(obj_mbeans, null);
			}
	        for(ObjectName on : mbeans) {
	        	if(on.getCanonicalName().contains("component=Cache"))
	        		continue;
	        	MBeanAttributeInfo[] atts = monitored_infinispan.mbsc.getMBeanInfo(on).getAttributes();
	        	for(MBeanAttributeInfo att : atts){
	        		//control added to be safe in case the name is null
	        		if(att.getName() == null){
	        			continue;
	        		}
	        		if(att.getName().equalsIgnoreCase("statisticsEnabled"))
	        			continue;
	        		try{
	        			Object value = monitored_infinispan.mbsc.getAttribute(on,att.getName());
	        			if(attributeKey == 33){
	        				int a = 0;
	        			}
	        			if(value instanceof Map){
	    					Map map = (Map) value;
	    					String value_str = MapToString(map);
	    			    	list.add(new DefaultProbeValue(attributeKey++, cleanValue(value_str)));
	    				}else if(value instanceof String){
	    					list.add(new DefaultProbeValue(attributeKey++, cleanValue((String)value)));
	    				}else{
	    					list.add(new DefaultProbeValue(attributeKey++, value));
	    				}
		        		//System.out.println("Value "+att.getName()+":"+(attributeKey-1)+":"+value);
	        		}catch(Exception e){
	        			e.printStackTrace();
	        			list.add(new DefaultProbeValue(attributeKey++, "xx"));
	        		}
	        	}
	        	if(resetStatis){
	        		//reset statistics specified MBean
		        	try {
						monitored_infinispan.mbsc.invoke(on, "resetStatistics", new Object[]{}, new String[]{});
					} catch (MBeanException e) {
						
					}
	        	}
	        }
	        
	        
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (IntrospectionException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		} catch (TypeException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		return new ProducerMeasurement(this, list, MonitorableResources.JMX.toString());
	}	
	

	private static String MapToString(Map map) {
		String mapStringed = new String();
		mapStringed = map.toString();
		mapStringed = mapStringed.replace("{","");
		mapStringed = mapStringed.replace("}","");
		mapStringed = mapStringed.replace(" ","");
		mapStringed = mapStringed.replace(",","|");
		if(mapStringed.equals(""))
			return "null";
		return "|"+mapStringed;
	}

	private static void loadParametersFromRegistry(){
    	String propsFile = "config/infinispan_probe.config";
    	Properties props = new Properties();
		try {
			props.load(new FileInputStream(propsFile));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		port_num = Integer.parseInt(props.getProperty("Infinispan_JMX_port_number")); 
		cache_name = props.getProperty("Cache_name");
		jmxDomain_primary = props.getProperty("JMXDomain_primary");
		jmxDomain_secondary = props.getProperty("JMXDomain_secondary");
		replicationType = props.getProperty("Replication_type");
		cacheManager = props.getProperty("Cache_Manager");
		JBossEnabled = Boolean.parseBoolean(props.getProperty("UseJBoss"));
		addr = props.getProperty("Infinispan_IP_Address");
		if(addr == null || addr.equals("")){
			try {
				InetAddress thisIp = InetAddress.getLocalHost();
				addr = thisIp.getHostAddress();
				System.out.println("Infinispan probe attached to "+addr);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		resetStatis = Boolean.parseBoolean(props.getProperty("ResetStats"));
		
    }
	private String cleanValue(String str){
		String ris = str.replaceAll(",", "_");
		ris = ris.replaceAll(" ", "__");
		return ris;
		
	}
}