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

import java.io.*;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import eu.cloudtm.am.client.*;


import eu.cloudtm.wpm.*;
import eu.cloudtm.wpm.connector.*;
import eu.cloudtm.wpm.logService.remote.events.SubscribeEvent;
import eu.cloudtm.wpm.logService.remote.listeners.WPMStatisticsRemoteListener;
import eu.cloudtm.wpm.logService.remote.listeners.WPMViewChangeRemoteListener;
import eu.cloudtm.wpm.logService.remote.observables.Handle;

/*
* @author Diego Rughetti
* @author Sebastiano Peluso
*/
public class StartDemo {
	
	private static int MINREPLICATION = 2;
	private static int MAXNODENUMBER = 4;
	
	private static double CLIENTNORM = 33;
	private static double SERVERNORM = 5;
	private static double REPLICATIONNORM = 5;
	private static double THROUGHPUTNORM = 2000;
	private static double RESPONSENORM = 17000;
	
	public static void main(String [] arg) throws RemoteException, UnknownHostException, NotBoundException{
		
		
		Properties props = new Properties();
		InputStream is = StartDemo.class.getClassLoader().getResourceAsStream("ANN.properties");
		if(is==null){
			is = StartDemo.class.getClass().getResourceAsStream("/ANN.properties");
		}
		try{
			props.load(is);
			if(is != null)
				is.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		Configuration.getOrCreateConfiguration(props);
		
		
		// sebastiano 
		
		WPMConnector connector = new WPMConnector();
		
		
		double clientNorm;
		double serverNorm;
		double replicationNorm;
		double throughputNorm;
		double responseNorm;
		Nns nn;
		ActuatorClient ac;


		clientNorm = Double.valueOf(Configuration.getConfiguration().getValue("clientNormalization")).doubleValue();
		serverNorm = Double.valueOf(Configuration.getConfiguration().getValue("serverNormalization")).doubleValue();
		replicationNorm  = Double.valueOf(Configuration.getConfiguration().getValue("replicationNormalization")).doubleValue();
		throughputNorm  = Double.valueOf(Configuration.getConfiguration().getValue("throughputNormalization")).doubleValue();
		responseNorm  = Double.valueOf(Configuration.getConfiguration().getValue("responseNormalization")).doubleValue();

		nn = new Nns(clientNorm, serverNorm, replicationNorm, throughputNorm, responseNorm);

		nn.parseInputFile(Configuration.getConfiguration().getValue("trainingFilesPath")); // path dei file dei log per il training


		nn.trainNetworks();
		nn.loadNetworks();

		ac = new ActuatorClient(Configuration.getConfiguration().getValue("actuatorServer"), Integer.parseInt(Configuration.getConfiguration().getValue("actuatorServerPort")));

		
		
		WPMStatisticsRemoteListener statistics = new NNStatisticsRemoteListenerImpl(connector, nn, ac);
		
		String nodes = Configuration.getConfiguration().getValue("monitoredNodes");
		
		StringTokenizer strTok = new StringTokenizer(nodes, " ");
		int numNodes = strTok.countTokens();
		String[] subNodes = new String[numNodes]; 
		int i = 0;
		while(strTok.hasMoreTokens()){
			subNodes[i] = strTok.nextToken();
			
			i++;
		}
		
		Handle handle = connector.registerStatisticsRemoteListener(new SubscribeEvent(subNodes), statistics);
		
		WPMViewChangeRemoteListener viewListener = new NNViewChangeRemoteListenerImpl(connector, handle, nn, ac);
		
		connector.registerViewChangeRemoteListener(viewListener);
		
		
	
		
		while(true){
			
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


		}
	}
}
