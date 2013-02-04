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
 
package eu.cloudtm.wpm.connector;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import eu.cloudtm.wpm.logService.remote.events.SubscribeEvent;
import eu.cloudtm.wpm.logService.remote.listeners.WPMStatisticsRemoteListener;
import eu.cloudtm.wpm.logService.remote.listeners.WPMViewChangeRemoteListener;
import eu.cloudtm.wpm.logService.remote.observables.Handle;
import eu.cloudtm.wpm.logService.remote.observables.WPMObservable;

/*
* @author Sebastiano Peluso
*/

public class WPMConnector {
	
	private static final int PORT = 1099;
	
	private WPMObservable observable;
	
	public WPMConnector() throws RemoteException, UnknownHostException, NotBoundException{
		
		Registry registry;
		
		
		registry = LocateRegistry.getRegistry(InetAddress.getLocalHost().getHostName(),PORT);
		
		this.observable=(WPMObservable) registry.lookup("WPMObservable");
		
		
	}
	
	
	public Handle registerStatisticsRemoteListener(SubscribeEvent event, WPMStatisticsRemoteListener listener) throws RemoteException{
		
		WPMStatisticsRemoteListener stub_listener = (WPMStatisticsRemoteListener) UnicastRemoteObject.exportObject(listener, 0);
		
		return this.observable.registerWPMStatisticsRemoteListener(event, stub_listener);
		
	}
	
	public Handle registerViewChangeRemoteListener(WPMViewChangeRemoteListener listener) throws RemoteException{
		
		WPMViewChangeRemoteListener stub_listener = (WPMViewChangeRemoteListener) UnicastRemoteObject.exportObject(listener, 0);
		
		return this.observable.registerWPMViewChangeRemoteListener(stub_listener);
		
	}
	
	public void removeStatisticsRemoteListener(Handle handle) throws RemoteException{
		
		this.observable.removeWPMStatisticsRemoteListener(handle);
		
	}
	
	public void removeViewChangeRemoteListener(Handle handle) throws RemoteException{
		
		this.observable.removeWPMViewChangeRemoteListener(handle);
	}

}
