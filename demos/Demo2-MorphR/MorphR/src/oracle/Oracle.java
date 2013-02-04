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


package oracle;

import exceptions.ClusterNotStableException;
import metrics.WPMMetricsGatherer;
import switchmanager.SwitchManager.Protocol;
import utils.PropertyReader;



public class Oracle {

	private static String modelFilename;
	private static boolean modelUsesTrees, forceStop, abort;
	private boolean firstQuery = true;
	private WPMMetricsGatherer metrics; //TODO This shouldn't be here!!!!
	
	private native void initiateSee5withTrees(String filename);
	private native void initiateSee5withRules(String filename);
	private native String getPrediction(String att);
	
	
	private Protocol currentProtocol;
	
	//public Oracle(MetricsGatherer m){
	public Oracle(WPMMetricsGatherer m){
		
		modelFilename = PropertyReader.getString("modelFilename", "/oracle.properties");
		modelUsesTrees =  PropertyReader.getBoolean("useTrees", "/oracle.properties");
		forceStop =  PropertyReader.getBoolean("forceStop", "/oracle.properties");
		abort = PropertyReader.getBoolean("abort", "/oracle.properties");
		

		System.loadLibrary("switchmanagerJNI");
		
		if(modelUsesTrees)
			initiateSee5withTrees(modelFilename);
		else
			initiateSee5withRules(modelFilename);
		
		metrics = m;
	}
	
	
	public void queryOracle(String att){
		if(firstQuery){
			try {
				currentProtocol = metrics.determineProtocolRunning();
			} catch (ClusterNotStableException e) {
				e.printStackTrace();
			}
			firstQuery = false;
		}
		
		Protocol protocol = getOraclePrediction(att);
		
		if(protocol != currentProtocol){
			metrics.switchProtocol(protocol, forceStop, abort);
			currentProtocol = protocol;
		}

	}
	
	
	private Protocol getOraclePrediction(String att){
		String pred = getPrediction(att);
		
		if(Protocol.PB.toString().equalsIgnoreCase(pred))
			return Protocol.PB;
		else
			if(Protocol.TOB.toString().equalsIgnoreCase(pred))
				return Protocol.TOB;
			else
				return Protocol.TwoPC;
	}
	
	
}
