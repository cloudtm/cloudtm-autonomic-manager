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
 
package eu.cloudtm.wpm.logService.remote.listeners;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import eu.cloudtm.wpm.logService.remote.events.PublishAttribute;
import eu.cloudtm.wpm.logService.remote.events.PublishMeasurement;
import eu.cloudtm.wpm.logService.remote.events.PublishStatisticsEvent;
import eu.cloudtm.wpm.parser.ResourceType;

/*
* @author Sebastiano Peluso
*/
public class TestWPMStatisticsRemoteListenerImpl implements
		WPMStatisticsRemoteListener {

	@Override
	public void onNewPerVMStatistics(PublishStatisticsEvent event)
			throws RemoteException {
		
		System.out.println("Called onNewPerVMStatistics");

	}

	@Override
	public void onNewPerSubscriptionStatistics(PublishStatisticsEvent event)
			throws RemoteException {
	
		Set<String> ips = event.getIps();
		
		for(String ip: ips){
			
			System.out.println("Printing Statistics for machine "+ip);
			
			int numResources = event.getNumResources(ResourceType.JMX, ip);
			
			if(numResources > 0){
				for (int i= 0; i< numResources; i++){
					PublishMeasurement pm = event.getPublishMeasurement(ResourceType.JMX, i, ip);
					HashMap<String, PublishAttribute> values = pm.getValues();
					if(values != null && !values.isEmpty()){
						
						Set<Entry<String,PublishAttribute>> entries = values.entrySet();
						
						for(Entry<String,PublishAttribute> entry: entries){
							
							System.out.println(""+entry.getKey()+" - "+entry.getValue().getValue());
							
						}
					}
				}
				
			}
			
		}
		

	}

}
