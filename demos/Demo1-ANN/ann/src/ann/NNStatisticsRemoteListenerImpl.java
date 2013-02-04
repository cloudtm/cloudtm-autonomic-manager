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

package ann;

import java.io.File;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;

import eu.cloudtm.am.client.ActuatorClient;
import eu.cloudtm.wpm.logService.remote.events.PublishAttribute;
import eu.cloudtm.wpm.logService.remote.events.PublishMeasurement;
import eu.cloudtm.wpm.logService.remote.events.PublishStatisticsEvent;
import eu.cloudtm.wpm.logService.remote.listeners.WPMStatisticsRemoteListener;
import eu.cloudtm.wpm.connector.*;
import eu.cloudtm.wpm.parser.ResourceType;

/*
* @author Diego Rughetti
* @author Sebastiano Peluso
*/

public class NNStatisticsRemoteListenerImpl implements WPMStatisticsRemoteListener {
	
	private WPMConnector connector;
	private Nns nn;
	ActuatorClient ac;
	
	public NNStatisticsRemoteListenerImpl(WPMConnector connector, Nns nn, ActuatorClient ac){
		this.connector = connector;
		
		this.nn = nn;
		this.ac = ac;

	}


	@Override
	public void onNewPerVMStatistics(PublishStatisticsEvent event) throws RemoteException {

		System.out.println("Called onNewPerVMStatistics");

	}

	@Override
	public void onNewPerSubscriptionStatistics(PublishStatisticsEvent event) throws RemoteException {


		
		
		long activeTransactions = 0;
		Set<String> ips = event.getIps();
		
		int numResources = 0;
		PublishMeasurement pm;
		HashMap<String, PublishAttribute> statistics;
		long currentValue;
		for(String ip: ips){
			
			numResources = event.getNumResources(ResourceType.JMX, ip);
			
			for(int i = 0; i < numResources; i++){
				
				pm = event.getPublishMeasurement(ResourceType.JMX, i, ip);
				if(pm != null){
					statistics = pm.getValues();
					if(statistics != null){
						
						currentValue = (Long) statistics.get("LocalActiveTransactions").getValue();
						
						activeTransactions += currentValue;
					}
					
				}			
				
			}
			
		}
		
		
		
		OptimalPrevision op, previousOp = null;
		
		
		int minReplication = Integer.valueOf(Configuration.getConfiguration().getValue("minimalReplication")).intValue();
		int maxNodeNumber = Integer.valueOf(Configuration.getConfiguration().getValue("maximumNodeNumber")).intValue();
		op = nn.getOptimalPrevision(minReplication, maxNodeNumber, (int) activeTransactions);
		
		if(previousOp == null || !previousOp.equals(op)){
			previousOp = op;
			if(Configuration.getConfiguration().getValue("optimizationTarget").equals("Throughput")){
				System.out.println("Server = " + op.getServerThroughput() + " - " + "Replication = " + op.getReplicationThroughput());
				ac.setConfiguration((int) op.getServerThroughput(),(int) op.getReplicationThroughput());
		
			}else if(Configuration.getConfiguration().getValue("optimizationTarget").equals("ResponseTime")){
				System.out.println("Server = " + op.getServerResponseTime() + " - " + "Replication = " + op.getReplicationResponseTime());
				ac.setConfiguration((int) op.getServerResponseTime(), (int) op.getReplicationResponseTime());
			}else{
				System.out.println("Invalid value for OptimizationTarget parameter in configuration file");
			}
		}
		


	}

}