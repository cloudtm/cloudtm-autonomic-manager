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
 * @author      Maria Couceiro mcouceiro@gsd.inesc-id.pt
 * @version     1.0               
 * @since       2013-02-01          
 */

package switchmanager;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.StringTokenizer;

import metrics.WPMMetricsGatherer;
import utils.PropertyReader;
import eu.cloudtm.wpm.connector.WPMConnector;
import eu.cloudtm.wpm.logService.remote.events.SubscribeEvent;
import eu.cloudtm.wpm.logService.remote.listeners.WPMStatisticsRemoteListener;


public class SwitchManager {

	public enum Protocol {
		TwoPC, PB, TOB;
		public String toString() {
			switch(this){
			case TwoPC:
				return "2PC"; 
			case PB:
				return "PB";
			case TOB:
				return "TO";
			default:
				return null;	
			}
		}
	}

	private final static String nodes = PropertyReader.getString("namingHosts", "/jmx.properties");

	private static void runCycle() {
		while(true) {}
	}


	public static void main(String[] args) {		

		StringTokenizer strTokenizer = new StringTokenizer(nodes);
		String[] ips = new String[strTokenizer.countTokens()];
		int i = 0;
		while(strTokenizer.hasMoreTokens()){
			String token = strTokenizer.nextToken();
			ips[i++]=new String(token);
		}
		try {
			WPMConnector connector = new WPMConnector();
			WPMStatisticsRemoteListener metricsGatherer = new WPMMetricsGatherer(ips);
			connector.registerStatisticsRemoteListener(new SubscribeEvent(ips), metricsGatherer);
			((WPMMetricsGatherer)metricsGatherer).init();
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}

		Thread thread = new Thread(new Runnable(){
			public void run(){
				runCycle();
			}
		});
		thread.setName("Cycle");
		thread.setDaemon(true);
		thread.start();


	}
}
