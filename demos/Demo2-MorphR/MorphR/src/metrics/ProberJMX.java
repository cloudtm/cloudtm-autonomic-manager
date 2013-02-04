/*
 * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
 * Copyright 2013 INESC-ID and/or its affiliates and other
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

/**
 * @author      Maria Couceiro <mcouceiro@gsd.inesc-id.pt>
 * @version     1.0               
 * @since       2013-02-01          
 */



package metrics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import switchmanager.SwitchManager.Protocol;
import utils.PropertyReader;


public class ProberJMX {

	private String ip;
	private final int namingPort = PropertyReader.getInt("namingPort", "/jmx.properties");
	private JMXConnector connector;
	private MBeanServerConnection connection;
	private ObjectName ReconfigProtdomain, ExtendedStatsdomain, OSdomain;
	private boolean isPrimary;
	private double lastTime, lastCPUTime;

	public ProberJMX(String ip) {
		this.ip = new String(ip);
	}
	
	public void connect() {
		final String strURL = "service:jmx:rmi:///jndi/rmi://"+ip+":"+namingPort+"/jmxrmi";
		JMXServiceURL url;	 
		try {
			url = new JMXServiceURL(strURL);
			connector = JMXConnectorFactory.connect(url);
			connection = connector.getMBeanServerConnection();
			ReconfigProtdomain = new ObjectName(getReconfigurableReplicationManagerComponentBaseString());
			ExtendedStatsdomain = new ObjectName(getCacheComponentBaseString());
			OSdomain = new ObjectName("java.lang:type=OperatingSystem");
			isPrimary = isThisNodeThePrimary();
			lastCPUTime = (Long) connection.getAttribute(OSdomain,"ProcessCpuTime");
		} catch (MalformedURLException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (AttributeNotFoundException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (MBeanException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		} 
	}


	public void disconnect() {
		try {
			connector.close();
		} catch (IOException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		}
	}


	public boolean isPrimary() {
		return isPrimary;
	}




	public Object getAvgCPU() {
		try {
			double currentTime = System.nanoTime();
			double currentCPU = (Long) connection.getAttribute(OSdomain,"ProcessCpuTime");

			Double result = (currentCPU - lastCPUTime) / (currentTime - lastTime);

			lastCPUTime = currentCPU;
			lastTime = currentTime;

			return result;
		} catch (AttributeNotFoundException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (MBeanException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (ReflectionException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		}
		return null;
	}

	
	public Protocol getProtocolInUse() {
		try {
			String p = (String) connection.getAttribute(ReconfigProtdomain, "CurrentProtocolId");
			if(p.compareToIgnoreCase(Protocol.PB.toString()) == 0)
				return Protocol.PB;
			if(p.compareToIgnoreCase(Protocol.TwoPC.toString()) == 0)
				return Protocol.TwoPC;
			return Protocol.TOB;
		} catch (AttributeNotFoundException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (MBeanException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	public void resetStats() {
		try {
			connection.invoke(ExtendedStatsdomain, "resetStatistics", new Object[0], new String[0]);
		} catch (InstanceNotFoundException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (MBeanException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (ReflectionException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		}
	}


	public void switchTo(Protocol protocol, boolean forceStop, boolean abort) {
		try {
			connection.invoke(ReconfigProtdomain, "switchTo", new Object[] {protocol.toString(), forceStop}, new String[] {"java.lang.String", "boolean"});
		}  catch (InstanceNotFoundException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (MBeanException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (ReflectionException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		}

	}

	public String getIP() {
		return ip;
	}

	private String getCacheComponentBaseString() {
		String domain = "org.infinispan";
		try {
			for (ObjectName name : connection.queryNames(null, null)) {
				if (name.getDomain().equals(domain)) {
					if ("Cache".equals(name.getKeyProperty("type"))) {
						String cacheName = name.getKeyProperty("name");
						String cacheManagerName = name.getKeyProperty("manager");
						return new StringBuilder(domain)
						.append(":type=Cache,name=")
						.append(cacheName.startsWith("\"") ? cacheName :
							ObjectName.quote(cacheName))
							.append(",manager=").append(cacheManagerName.startsWith("\"") ? cacheManagerName :
								ObjectName.quote(cacheManagerName))
								.append(",component=ExtendedStatistics").toString();
					}
				}
			}
		} catch (NullPointerException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		}
		return null;
	}
	
	private String getReconfigurableReplicationManagerComponentBaseString() {
		String domain = "org.infinispan";
		try {
			for (ObjectName name : connection.queryNames(null, null)) {
				if (name.getDomain().equals(domain)) {
					if ("Cache".equals(name.getKeyProperty("type"))) {
						String cacheName = name.getKeyProperty("name");
						String cacheManagerName = name.getKeyProperty("manager");
						return new StringBuilder(domain)
						.append(":type=Cache,name=")
						.append(cacheName.startsWith("\"") ? cacheName :
							ObjectName.quote(cacheName))
							.append(",manager=").append(cacheManagerName.startsWith("\"") ? cacheManagerName :
								ObjectName.quote(cacheManagerName))
								.append(",component=ReconfigurableReplicationManager").toString();
					}
				}
			}
		} catch (NullPointerException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean isThisNodeThePrimary(){
		try {
			String clusterView = (String) connection.getAttribute(new ObjectName("org.infinispan:type=CacheManager,name=\"DefaultCacheManager\",component=CacheManager"), "clusterMembers");

			if(clusterView.substring(1, clusterView.indexOf("-")).compareToIgnoreCase(ip) == 0)
				return true;

		} catch (AttributeNotFoundException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (MBeanException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (ReflectionException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println((new Date()).toString());
			e.printStackTrace();
		}
		return false;
	}


}
