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
 
package eu.cloudtm.wpm.logService.remote.publisher;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import eu.cloudtm.wpm.logService.remote.events.PublishAttribute;
import eu.cloudtm.wpm.logService.remote.events.PublishMeasurement;
import eu.cloudtm.wpm.logService.remote.events.PublishStatisticsEvent;
import eu.cloudtm.wpm.logService.remote.observables.WPMObservableImpl;
import eu.cloudtm.wpm.parser.ResourceType;

/*
* @author Sebastiano Peluso
*/
public class PublisherStatsThread extends Thread{

	private PublishStatsEventInternal event; 
	
	private WPMObservableImpl observableImpl;

	public PublisherStatsThread(PublishStatsEventInternal event, WPMObservableImpl observableImpl){

		this.event = event;
		this.observableImpl = observableImpl;
	}

	public void run(){

		Collection<PublishStatisticsEvent> perVM = event.getPerVMEvents();

		if(perVM != null){

			for(PublishStatisticsEvent pe: perVM){

				try {
					event.getListener().onNewPerVMStatistics(pe);
				} catch (RemoteException e) {
					
					this.observableImpl.garbageCollect(event.getListener());
				}

			}
		}

		PublishStatisticsEvent perSubscription = event.getPerSubscriptionEvent();

		if(perSubscription != null){

			try {
				event.getListener().onNewPerSubscriptionStatistics(perSubscription);
			} catch (RemoteException e) {
				this.observableImpl.garbageCollect(event.getListener());
			}


		}

	}

}
