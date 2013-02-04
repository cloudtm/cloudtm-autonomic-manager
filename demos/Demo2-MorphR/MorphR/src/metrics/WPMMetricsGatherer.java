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

import switchmanager.SwitchManager.Protocol;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import oracle.Oracle;

import eu.cloudtm.wpm.logService.remote.events.PublishAttribute;
import eu.cloudtm.wpm.logService.remote.events.PublishMeasurement;
import eu.cloudtm.wpm.logService.remote.events.PublishStatisticsEvent;
import eu.cloudtm.wpm.logService.remote.listeners.WPMStatisticsRemoteListener;
import eu.cloudtm.wpm.parser.ResourceType;
import exceptions.ClusterNotStableException;

import utils.PropertyReader;


public class WPMMetricsGatherer implements WPMStatisticsRemoteListener {


	private final static String outputFileName = PropertyReader.getString("outputFileName", "/jmx.properties");
	private final static int runDuration = PropertyReader.getInt("runDuration", "/jmx.properties");
	private final static boolean toQuery = PropertyReader.getBoolean("toQuery", "/jmx.properties");

	private static ArrayList<ProberJMX> probersList = new ArrayList<ProberJMX>();

	private static OutputStreamWriter out = null;

	private Oracle o;
	private Long startTime;

	public WPMMetricsGatherer(String[] nodes) {	
		super();	
		try {
			out = new OutputStreamWriter(new FileOutputStream(outputFileName), "ASCII");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		ProberJMX p;
		for(String i : nodes){
			p = new ProberJMX(i);
			probersList.add(p);
		}
	}

	public void init() {
		o = new Oracle(this);
		for(ProberJMX p : probersList)
			p.connect();
		startTime = System.currentTimeMillis();
	}


	@Override
	public void onNewPerSubscriptionStatistics(PublishStatisticsEvent event)
			throws RemoteException {
		Protocol prot = null;
		try {
			prot = determineProtocolRunning();
		} catch (ClusterNotStableException e1) {
			e1.printStackTrace();
		}

		StringBuilder str = new StringBuilder(prot.toString());
		int probersSize = probersList.size();

		double metricPrimary = 0.0, metricOthers= 0.0;		
		for(ProberJMX p : probersList){
			if(p.isPrimary() && prot == Protocol.PB)
				metricPrimary = (Double) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "PercentageWriteTransactions");  
			else
				metricOthers+= (Double) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "PercentageWriteTransactions");
		}
		printPBCollapsed(prot, str, metricPrimary, metricOthers, probersSize,true);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && prot == Protocol.PB)
				metricPrimary = (Double) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AbortRate");
			else
				metricOthers+= (Double) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AbortRate");
		}
		printPBdiff(prot, str, metricPrimary, metricOthers, probersSize);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && (prot == Protocol.PB || prot == Protocol.TOB))
				metricPrimary = (Double) p.getAvgCPU();
			else
				metricOthers+= (Double) p.getAvgCPU();
		}
		printPBTOBdiff(prot, str, metricPrimary, metricOthers, probersSize, false);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && (prot == Protocol.PB || prot == Protocol.TOB))
				metricPrimary = (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.MEMORY, "MemoryInfo.used");
			else
				metricOthers+= (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.MEMORY, "MemoryInfo.used");
		}
		printPBTOBdiff(prot, str, metricPrimary, metricOthers, probersSize, false);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && prot == Protocol.PB)
				metricPrimary = (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgReadOnlyTxDuration");
			else
				metricOthers+= (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgReadOnlyTxDuration");
		}
		printPBCollapsed(prot, str, metricPrimary, metricOthers, probersSize,false);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && (prot == Protocol.PB || prot == Protocol.TOB))
				metricPrimary =(Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgWriteTxDuration") - (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgWriteTxLocalExecution");
			else
				metricOthers+= (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgWriteTxDuration") - (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgWriteTxLocalExecution");
		}
		printPBTOBdiff(prot, str, metricPrimary, metricOthers, probersSize, false);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && prot == Protocol.PB)
				metricPrimary = (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgWriteTxLocalExecution");
			else
				metricOthers+= (Long)getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgWriteTxLocalExecution");
		}
		printPBCollapsed(prot, str, metricPrimary, metricOthers, probersSize,true);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && (prot == Protocol.PB || prot == Protocol.TOB))
				metricPrimary = (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgWriteTxDuration");
			else
				metricOthers+= (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgWriteTxDuration");
		}
		printPBTOBdiff(prot, str, metricPrimary, metricOthers, probersSize, false);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && prot == Protocol.PB)
				metricPrimary = (Double) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgTxArrivalRate");
			else
				metricOthers+= (Double) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgTxArrivalRate");
		}
		printPBdiff(prot, str, metricPrimary, metricOthers, probersSize);


		if(prot == Protocol.TwoPC || prot == Protocol.PB){
			metricOthers = 0.0;
			for(ProberJMX p : probersList)
				if(p.isPrimary())
					metricPrimary = (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgLockWaitingTime");
		}
		print2PCandPB(prot, str, metricPrimary, probersSize);


		if(prot == Protocol.TwoPC || prot == Protocol.PB){
			metricOthers = 0.0;
			for(ProberJMX p : probersList)
				if(p.isPrimary())
					metricPrimary = (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgLockHoldTime");
		}
		print2PCandPB(prot, str, metricPrimary, probersSize);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && prot == Protocol.PB)
				metricPrimary = (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgGetsPerROTransaction");
			else
				metricOthers+= (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgGetsPerROTransaction");
		}
		printPBCollapsed(prot, str, metricPrimary, metricOthers, probersSize,false);

		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && prot == Protocol.PB)
				metricPrimary = (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgGetsPerWrTransaction");
			else
				metricOthers+= (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgGetsPerWrTransaction");
		}
		printPBCollapsed(prot, str, metricPrimary, metricOthers, probersSize, true);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && prot == Protocol.PB)
				metricPrimary = (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgNumPutsBySuccessfulLocalTx");
			else
				metricOthers+= (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgNumPutsBySuccessfulLocalTx");
		}
		printPBCollapsed(prot, str, metricPrimary, metricOthers, probersSize, true);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && (prot == Protocol.PB || prot == Protocol.TOB))
				metricPrimary = (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgPrepareRtt");
			else
				metricOthers+= (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgPrepareRtt");
		}
		printPBTOBdiff(prot, str, metricPrimary, metricOthers, probersSize, false);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary() && prot == Protocol.PB)
				metricPrimary = (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgPrepareCommandSize");
			else
				metricOthers+= (Long) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "AvgPrepareCommandSize");
		}
		printPBCollapsed(prot, str, metricPrimary, metricOthers, probersSize, true);


		metricOthers = 0.0;
		for(ProberJMX p : probersList){
			if(p.isPrimary())
				metricPrimary = (Double) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "Throughput");
			else
				metricOthers+= (Double) getAttributeValue(event,((ProberJMX)p).getIP(), ResourceType.JMX, "Throughput");
		}
		printPBTOBdiff(prot, str, metricPrimary, metricOthers, probersSize, true);

		for(ProberJMX p : probersList)
			p.resetStats();

		try {
			out.write(str+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(toQuery) {
			o.queryOracle("xx,"+str+",8,?,xx");
			
		}
		if(System.currentTimeMillis() - startTime >= runDuration) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}



	} 



	public void switchProtocol(Protocol prot, boolean forceStop, boolean abortTxs){
		for(ProberJMX p : probersList)
			if(p.isPrimary())
				p.switchTo(prot, forceStop, abortTxs);
	}

	public Protocol determineProtocolRunning() throws ClusterNotStableException{
		Protocol prot = probersList.get(0).getProtocolInUse();
		for(ProberJMX p : probersList){
			if(prot != p.getProtocolInUse())
				throw new ClusterNotStableException();
		}
		return prot;
	}


	private void printPBCollapsed(Protocol prot, StringBuilder str, double metricPrimary, double metricOthers, int probersSize, boolean primary){
		if(prot == Protocol.PB && primary) 
			str.append(","+metricPrimary);
		else {
			metricOthers /= probersSize;
			str.append(","+metricOthers);
		}

	}

	private void printPBdiff(Protocol prot, StringBuilder str, double metricPrimary, double metricOthers, int probersSize){
		if(prot == Protocol.PB) {
			metricOthers /= probersSize-1;
			str.append(","+metricPrimary+","+metricOthers);
		} else {
			metricOthers /= probersSize;
			str.append(",N/A,"+metricOthers);
		}
	}

	private void printPBTOBdiff(Protocol prot, StringBuilder str, double metricPrimary, double metricOthers, int probersSize, boolean twoPC){
		if(prot == Protocol.PB || prot == Protocol.TOB || (twoPC && prot == Protocol.TwoPC)){
			metricOthers /= probersSize-1;
			str.append(","+metricPrimary+","+metricOthers);
		} else {
			metricOthers /= probersSize;
			str.append(",N/A,"+metricOthers);
		}
	}

	private void print2PCandPB(Protocol prot, StringBuilder str, double metricPrimary, int probersSize){
		if(prot == Protocol.TwoPC || prot == Protocol.PB)
			str.append(","+metricPrimary);
		else 
			str.append(",N/A");
	}


	@Override
	public void onNewPerVMStatistics(PublishStatisticsEvent arg0)
			throws RemoteException {
		// TODO Auto-generated method stub

	}

	private Object getAttributeValue(PublishStatisticsEvent event, String ip, ResourceType resource, String att) {
		int numResources = event.getNumResources(resource, ip);
		PublishMeasurement pm = event.getPublishMeasurement(resource, numResources-1, ip);
		HashMap<String, PublishAttribute> values = pm.getValues();
		Set<Entry<String,PublishAttribute>> entries = values.entrySet();
		for(Entry<String,PublishAttribute> entry: entries){
			if(entry.getKey().compareToIgnoreCase(att) == 0)
				return entry.getValue().getValue();
		}
		return null;
	}



}
