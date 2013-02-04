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

import java.rmi.RemoteException;

import eu.cloudtm.am.client.ActuatorClient;
import eu.cloudtm.wpm.connector.WPMConnector;
import eu.cloudtm.wpm.logService.remote.events.PublishViewChangeEvent;
import eu.cloudtm.wpm.logService.remote.events.SubscribeEvent;
import eu.cloudtm.wpm.logService.remote.listeners.WPMStatisticsRemoteListener;
import eu.cloudtm.wpm.logService.remote.listeners.WPMViewChangeRemoteListener;
import eu.cloudtm.wpm.logService.remote.observables.Handle;

/*
* @author Diego Rughetti
* @author Sebastiano Peluso
*/
public class NNViewChangeRemoteListenerImpl implements WPMViewChangeRemoteListener {

	private WPMConnector connector;
	
	private Handle prevStatisticsListener;
	
	private Nns nn;
	private ActuatorClient ac;
	
	public NNViewChangeRemoteListenerImpl(WPMConnector connector, Handle prevStatisticsListener, Nns nn, ActuatorClient ac){
		
		this.connector = connector;
		
		this.prevStatisticsListener = prevStatisticsListener;
		
		this.nn= nn;
		this.ac = ac;
	}
	
	@Override
	public void onViewChange(PublishViewChangeEvent event) throws RemoteException {
		
		
		if(this.prevStatisticsListener != null){
			this.connector.removeStatisticsRemoteListener(this.prevStatisticsListener);
			this.prevStatisticsListener = null;
		}
		
		
		String[] newNodes = event.getCurrentVMs();
		
		WPMStatisticsRemoteListener statistics = new NNStatisticsRemoteListenerImpl(connector, this.nn, this.ac);
		
		this.prevStatisticsListener = connector.registerStatisticsRemoteListener(new SubscribeEvent(newNodes), statistics);

	}

}
