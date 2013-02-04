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
 
package eu.cloudtm.wpm.logService.remote.observables;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import eu.cloudtm.wpm.logService.remote.events.SubscribeEvent;
import eu.cloudtm.wpm.logService.remote.listeners.WPMStatisticsRemoteListener;
import eu.cloudtm.wpm.logService.remote.listeners.WPMViewChangeRemoteListener;

/*
* @author Sebastiano Peluso
*/
public class WPMObservableImpl extends UnicastRemoteObject implements WPMObservable{
	
	private AtomicLong generator = new AtomicLong(0L);
	
	
	private ConcurrentLinkedQueue<StatsSubscriptionEntry> statsSubscriptions;
	
	private ConcurrentLinkedQueue<ViewSubscriptionEntry> viewSubscriptions;
	
	
	public WPMObservableImpl() throws RemoteException{
		
		this.statsSubscriptions = new ConcurrentLinkedQueue<StatsSubscriptionEntry>();
		this.viewSubscriptions = new ConcurrentLinkedQueue<ViewSubscriptionEntry>();
		
	}
	
	public Iterator<StatsSubscriptionEntry> getStatsIterator(){
		
		return this.statsSubscriptions.iterator();
	}
	
	public int numStatsSubscriptions(){
		return this.statsSubscriptions.size();
	}
	
	public Iterator<ViewSubscriptionEntry> getViewIterator(){
		
		return this.viewSubscriptions.iterator();
	}
	
	public int numViewSubscriptions(){
		return this.viewSubscriptions.size();
	}

	@Override
	public Handle registerWPMStatisticsRemoteListener(SubscribeEvent event, WPMStatisticsRemoteListener listener) throws RemoteException {
	
		System.out.println("Registered Statistics Listener");
		Handle handle = new Handle(this.generator.incrementAndGet());
		this.statsSubscriptions.add(new StatsSubscriptionEntry(handle, event.getVMs(),listener));
		
		return handle;
		
	}

	@Override
	public Handle registerWPMViewChangeRemoteListener(
			WPMViewChangeRemoteListener listener) throws RemoteException {
		
		System.out.println("Registered View Change Listener");
		Handle handle = new Handle(this.generator.incrementAndGet());
		this.viewSubscriptions.add(new ViewSubscriptionEntry(handle, listener));
		
		return handle;
	}

	public void garbageCollect(WPMViewChangeRemoteListener wpmViewChangeRemoteListener) {
		
		
		for(ViewSubscriptionEntry entry: this.viewSubscriptions){
			
			if(entry.getListener() == wpmViewChangeRemoteListener){
				this.viewSubscriptions.remove(entry);
				break;
			}
		}
		
	}

	public void garbageCollect(WPMStatisticsRemoteListener listener) {
		for(StatsSubscriptionEntry entry: this.statsSubscriptions){
			
			if(entry.getListener() == listener){
				this.statsSubscriptions.remove(entry);
				break;
			}
		}
		
	}

	@Override
	public void removeWPMStatisticsRemoteListener(Handle handle)
			throws RemoteException {
		
		for(StatsSubscriptionEntry entry: this.statsSubscriptions){
			
			if(entry.getHandle() != null && entry.getHandle().equals(handle)){
				
				this.statsSubscriptions.remove(entry);
				
			}
			
		}
		
	}

	@Override
	public void removeWPMViewChangeRemoteListener(Handle handle)
			throws RemoteException {
		
		for(ViewSubscriptionEntry entry: this.viewSubscriptions){
			
			if(entry.getHandle() != null && entry.getHandle().equals(handle)){
				
				this.viewSubscriptions.remove(entry);
				
			}
			
		}
		
	}

	
	
	
}
