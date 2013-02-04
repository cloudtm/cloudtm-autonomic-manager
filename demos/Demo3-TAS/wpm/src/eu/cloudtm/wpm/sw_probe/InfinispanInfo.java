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

import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Display amount of free and used memory in the system.
 * @author Roberto Palmieri
 */
public class InfinispanInfo {
	
	protected MBeanServerConnection mbsc = null;
	private String address;
	private int port;
	private String cacheName;
	private String jmxDomain_primary;
	private String jmxDomain_secondary;
	private String replicationType;
	private String cacheManager;
	private boolean JBossEnabled;
	private boolean resetStats;
    
	public InfinispanInfo(String addr, int port_num,String cache_name,String jmxDomain,String repl,String manager, boolean jBossEn, boolean resetStatis, String jmxDomain_sec) {
    	address = addr;
    	port = port_num;
    	cacheName = cache_name;
    	jmxDomain_primary = jmxDomain;
    	jmxDomain_secondary = jmxDomain_sec;
    	replicationType = repl;
    	cacheManager = manager; 
    	JBossEnabled = jBossEn;
    	resetStats = resetStatis;
    	if(JBossEnabled)
			connectToJBossJMXServer();
		else
			connectToJMXServer();
    	
    }
	/*
    public InfinispanValue getInfinispanValues(){
    	if(mbsc == null){
    		if(JBossEnabled)
    			connectToJBossJMXServer();
    		else
    			connectToJMXServer();
    	}
    		
    	InfinispanValue inf_value = new InfinispanValue();
    	try{
    		//ObjectName transactionComponent = new ObjectName(jmxDomainName+":type=Cache,name="+ObjectName.quote(cacheName+"("+replicationType+")")+",manager="+ObjectName.quote(cacheManager)+",component=Transactions");
    		ObjectName statisticsComponent = new ObjectName(jmxDomainName+":type=Cache,name="+ObjectName.quote(cacheName+"("+replicationType+")")+",manager="+ObjectName.quote(cacheManager)+",component=ExtendedStatistics");
    		
			Field[] jmx_attributes = InfinispanValue.class.getDeclaredFields();
			for(Field f : jmx_attributes){
				if(!f.getName().startsWith("get")){
					String nameAttributes =  f.getName().substring(0,1).toUpperCase() + f.getName().substring(1);
					if(f.getType() == double.class){
						Method invoking_method = InfinispanValue.class.getMethod("set"+nameAttributes,double.class);
						invoking_method.invoke(inf_value,(Double)mbsc.getAttribute(statisticsComponent,f.getName()));
					}else if(f.getType() == long.class){
						Method invoking_method = InfinispanValue.class.getMethod("set"+nameAttributes,long.class);
						invoking_method.invoke(inf_value,(Long)mbsc.getAttribute(statisticsComponent,f.getName()));
					}else if(f.getType() == int.class){
						Method invoking_method = InfinispanValue.class.getMethod("set"+nameAttributes,int.class);
						invoking_method.invoke(inf_value,(Integer)mbsc.getAttribute(statisticsComponent,f.getName()));
					}else if(f.getType() == String.class){
						Method invoking_method = InfinispanValue.class.getMethod("set"+nameAttributes,String.class);
						invoking_method.invoke(inf_value,(String)mbsc.getAttribute(statisticsComponent,f.getName()));
					}else if(f.getType() == Map.class){
						Method invoking_method = InfinispanValue.class.getMethod("set"+nameAttributes,Map.class);
						invoking_method.invoke(inf_value,(Map<Long,Long>)mbsc.getAttribute(statisticsComponent,f.getName()));
					}else
						throw new RuntimeException("Format not supported!");
				}else{
					String nameAttributes =  f.getName().substring(0,1).toUpperCase() + f.getName().substring(1);
					Method invoking_method = InfinispanValue.class.getMethod("set"+nameAttributes,double.class);
					invoking_method.invoke(inf_value,mbsc.invoke(statisticsComponent, f.getName(), new Object[]{10}, new String[]{"int"}));
				}
			}
    		
    	}catch (IOException e) {
    		e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (AttributeNotFoundException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (MBeanException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
    	
		return inf_value;
    }
    
    public InfinispanTopK getInfinispanTopK(){
    	if(mbsc == null)
    		connectToJMXServer();
    	InfinispanTopK inf_value = new InfinispanTopK();
    	try{
    		ObjectName streamComponent = new ObjectName(jmxDomainName+":type=Cache,name="+ObjectName.quote(cacheName+"("+replicationType+")")+",manager="+ObjectName.quote(cacheManager)+",component=StreamLibStatistics");
    		
			Field[] jmx_attributes = InfinispanTopK.class.getDeclaredFields();
			for(Field f : jmx_attributes){
				String nameAttributes =  f.getName().substring(0,1).toUpperCase() + f.getName().substring(1);
				if(f.getType() == Map.class){
					Method invoking_method = InfinispanTopK.class.getMethod("set"+nameAttributes,Map.class);
					invoking_method.invoke(inf_value,mbsc.invoke(streamComponent, "getN"+f.getName(), new Object[]{10}, new String[]{"int"}));
					//invoking_method.invoke(inf_value,(Map<Long,Long>)mbsc.getAttribute(streamComponent,f.getName()));
				}else
					throw new RuntimeException("Format not supported!");
			}
    	}catch (IOException e) {
    		e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (MBeanException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
    	
		return inf_value;
    }
    */
	
    private void connectToJMXServer(){
    	JMXServiceURL url = null;
    	JMXConnector jmxc = null;
    	boolean isConnected = false;
    	while(!isConnected){
	    	try {
				url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+address+":"+port+"/jmxrmi");
				jmxc = JMXConnectorFactory.connect(url, null);
				mbsc = jmxc.getMBeanServerConnection();
				ObjectName obj_mbeans;
				obj_mbeans = new ObjectName(jmxDomain_primary+":type=Cache,*");
				Set<ObjectName> mbeans = mbsc.queryNames(obj_mbeans, null);
				if(mbeans == null || mbeans.size() == 0){
					obj_mbeans = new ObjectName(jmxDomain_secondary+":type=Cache,*");
					mbeans = mbsc.queryNames(obj_mbeans, null);
					if(mbeans == null || mbeans.size() == 0){
						isConnected = false;
					}else
						isConnected = true;
				}else
					isConnected = true;
				//isConnected = true;
				if(isConnected)
					System.out.println("Infinispan conntected...");
				else{
					System.out.println("Waiting for Infinispan...");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				isConnected = false;
				System.out.println("Waiting for Infinispan...");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
    }
    
    private void connectToJBossJMXServer(){
    	JMXServiceURL url = null;
    	JMXConnector jmxc = null;
    	boolean isConnected = false;
    	while(!isConnected){
    		try {
        		String urlString = System.getProperty("jmx.service.url","service:jmx:remoting-jmx://" + address + ":" + port);
                url = new JMXServiceURL(urlString);
                jmxc = JMXConnectorFactory.connect(url, null);
                mbsc = jmxc.getMBeanServerConnection();
                ObjectName obj_mbeans;
				obj_mbeans = new ObjectName(jmxDomain_primary+":type=Cache,*");
				Set<ObjectName> mbeans = mbsc.queryNames(obj_mbeans, null);
				if(mbeans == null || mbeans.size() == 0){
					obj_mbeans = new ObjectName(jmxDomain_secondary+":type=Cache,*");
					mbeans = mbsc.queryNames(obj_mbeans, null);
					if(mbeans == null || mbeans.size() == 0){
						isConnected = false;
					}else
						isConnected = true;
				}else
					isConnected = true;
				//isConnected = true;
				if(isConnected)
					System.out.println("JBoss conntected...");
				else{
					System.out.println("Waiting for JBoss...");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
    		} catch (Exception e) {
    			e.printStackTrace();
    			isConnected = false;
    			System.out.println("Waiting for JBoss...");
    			try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
    		}
    	}
    }
}

