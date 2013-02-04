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
 
package eu.cloudtm.wpm.logService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;


import eu.cloudtm.wpm.logService.remote.publisher.PublishStatsEventInternal;
import eu.cloudtm.wpm.logService.remote.publisher.PublisherStatsThread;
import eu.cloudtm.wpm.logService.remote.publisher.PublisherViewThread;
import eu.cloudtm.wpm.logService.remote.events.PublishViewChangeEvent;
import eu.cloudtm.wpm.logService.remote.observables.StatsSubscriptionEntry;
import eu.cloudtm.wpm.logService.remote.observables.ViewSubscriptionEntry;
import eu.cloudtm.wpm.logService.remote.observables.WPMObservable;
import eu.cloudtm.wpm.logService.remote.observables.WPMObservableImpl;
import eu.cloudtm.wpm.parser.Measurement;
import eu.cloudtm.wpm.parser.MeasurementAttribute;
import eu.cloudtm.wpm.parser.ResourceType;
import eu.cloudtm.wpm.parser.WPMParser;

/*
* @author Roberto Palmieri
* @author Sebastiano Peluso
*/
public class LogServiceAnalyzer implements Runnable{
	
	private static final int RMI_REGISTRY_PORT = 1099;
	
	static final int filesize = 2048;
	private Cache<String,String> cache;
	private String cacheName;
	private String infinispanConfigFile;
	private long timeout;
	private boolean enableInfinispan;
	
	private boolean enableListeners;
	
	private WPMObservableImpl observable;
	
	private HashMap<String, Long> jmxNodes;
	
	private long numJmxNodes = 0;
	
	private long numCheckJmxNodes = 0;
	
	public LogServiceAnalyzer() throws RemoteException{
		loadParametersFromRegistry();
		
		
		if(enableInfinispan){
			GlobalConfiguration gc = GlobalConfiguration.getClusteredDefault();

			gc.setClusterName("LogServiceConnection");
			Configuration c = new Configuration();
			c.setCacheMode(Configuration.CacheMode.REPL_SYNC);
			c.setExpirationLifespan(-1);
			c.setExpirationMaxIdle(-1);
			EmbeddedCacheManager cm = new DefaultCacheManager(gc, c);
			this.cache = cm.getCache();

		}
		//System.out.println("Running Log Service Analyzer Thread!!");
		
		//TransactionManager tm = cache.getAdvancedCache().getTransactionManager();+
		
		
		Registry registry=LocateRegistry.createRegistry(RMI_REGISTRY_PORT);
		
		this.observable = new WPMObservableImpl();
		
		WPMObservable stub_observable= this.observable;
		
		registry.rebind("WPMObservable", stub_observable);
		
		
		
		this.jmxNodes = new HashMap<String, Long>();
		
		
		
		Thread analyzer = new Thread(this,"Log Service Analyzer");
		analyzer.start();
	}

	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				//System.out.println("Dataitem in cache: "+cache.size());
				//System.out.println("Running Log Service Analyzer Thread!!");
				File active_folder = new File("log/ls_processed");
				if(active_folder.isDirectory()){
					for(File activeFile : active_folder.listFiles()){
						if(!activeFile.getName().endsWith(".zip"))
							continue;
						try {
					        FileInputStream is = new FileInputStream(activeFile);
						    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
						    ZipEntry entry = null;
						    FileOutputStream fos = null;
						    String nameFileToStore = "";
						    if((entry = zis.getNextEntry()) != null){
						    	byte [] logFileByteArray  = new byte [filesize];
						    	int count = 0;
						    	nameFileToStore = "log/ls_worked/"+entry.getName();
						    	fos = new FileOutputStream(nameFileToStore);
						    	BufferedOutputStream dest = new BufferedOutputStream(fos, filesize);
								//System.out.println("Extracting: " +entry.getName());
								while ((count = zis.read(logFileByteArray, 0, filesize)) != -1) {
									dest.write(logFileByteArray, 0, count);
								}
								dest.flush();
						    }
						    fos.close();
						    zis.close();
						    //System.out.println("File decompressed stored");
						    //Create ack file
						    File ackFile = new File("log/ls_worked/"+activeFile.getName().substring(0,activeFile.getName().lastIndexOf(".zip"))+".ack");
				            if(!ackFile.createNewFile())
				            	System.out.println("Error while creating ack file");
				          
				            
				            
				            
				            //System.out.println ("Ack file stored: "+ackFile.getPath());
				            if(enableInfinispan || enableListeners){
				            	//timestamp nomeMetrica:valore;nomeMetrica:valore;nomeMetrica:valore
				            	String strLine = "";
				            	FileInputStream fstream = new FileInputStream(nameFileToStore);
				            	DataInputStream in = new DataInputStream(fstream);
				            	BufferedReader br = new BufferedReader(new InputStreamReader(in));
				            	
				            	
				            	boolean membersChanged = false;
				            	
				            	while ((strLine = br.readLine()) != null){
				            		//System.out.println("Data received by LogService before Parsing:\n"+strLine);
				            		Measurement mis = WPMParser.parseLine(strLine);
				            		if(mis == null)
				            			continue;


				            		if(enableListeners){
				            			
				            			
				            			membersChanged = checkJmxMembers(mis);
				            			
				            			
				            			if(membersChanged){
					            			
					            			
					            			int size = this.jmxNodes.size();
					            			Set<String> set = this.jmxNodes.keySet();
					            			
					            			String[] addresses = new String[size];
					            			
					            			int i = 0;
					            			for(String key: set){
					            				addresses[i] = key;
					            				i++;
					            				
					            			}
					            			
					            			
					            			PublishViewChangeEvent event = new PublishViewChangeEvent(addresses);
					            			
					            			
					            			Iterator<ViewSubscriptionEntry> itr = this.observable.getViewIterator();
					            			
					            			while(itr.hasNext()){
					            				
					            				ViewSubscriptionEntry current = itr.next();
					            				
					            				PublisherViewThread pt = new PublisherViewThread(current, event, this.observable);
					            				
					            				pt.start();
					            				
					            			}
					            			
					            			
					            		}
				            			
				            			
				            			
				            			
				            			
				            			
				            			
				            			
				            			
				            			
				            			String ip = mis.getIp();
				            			int resourceType = mis.getResourceType().ordinal();
				            			
				            			Iterator<StatsSubscriptionEntry> itr = this.observable.getStatsIterator();
				            			
				            			while(itr.hasNext()){
				            				
				            				
				            				itr.next().addMeasurement(mis);
				            				
				            			}
				            			
				            		}
				            		

				            		if(enableInfinispan){
				            			int num_of_res_ind = mis.getNumberOfResIndex();
				            			String mis_spec = "";
				            			for(int i=0;i<num_of_res_ind;i++){
				            				//String identification_key = mis.getComponent_ID()+":"+mis.getResourceType()+":"+i;
				            				String identification_key = mis.getIp()+":";
				            				if(mis.getResourceType() == ResourceType.JMX){
				            					identification_key += "Infinispan Cache ( CloudTM )"+":"+i;
				            				}else{
				            					identification_key += mis.getResourceType()+":"+i;
				            				}

				            				String payload = mis.getTimestamp()+";"+mis.getAttributesForInfinispan(i);
				            				cache.put(identification_key,payload);
				            				String date_format = "";
				            				try{
				            					SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
				            					date_format = sdf.format(new Date(mis.getTimestamp()));
				            				}catch(Exception e){}
				            				System.out.println("Put done! Id: "+identification_key+" timestap: "+date_format);
				            				System.out.println("Put done! Value: "+payload);
				            				if(mis.getResourceType() == ResourceType.DISK){
				            					String [] att_vect = mis.getAttributesForInfinispan(i).split(";");
				            					for(int k=0;k<att_vect.length;k++){
				            						if(att_vect[k] != null && att_vect[k].startsWith("fileSystemUsage.mounting_point")){
				            							mis_spec += i+"="+att_vect[k].substring(att_vect[k].indexOf("fileSystemUsage.mounting_point$$")+(new String("fileSystemUsage.mounting_point$$").length()))+";";
				            						}
				            					}
				            				}
				            				if(mis.getResourceType() == ResourceType.NETWORK){
				            					mis_spec += i+"=en"+i+";";
				            				}
				            			}

				            			String platforms = cache.get("WPMPlatforms");
				            			if(platforms == null){
				            				cache.put("WPMPlatforms", mis.getIp());
				            			}else{
				            				if(!platforms.contains(mis.getIp())){
				            					platforms += ";"+mis.getIp();
				            					cache.put("WPMPlatforms", platforms);
				            				}
				            			}
				            			String plats = cache.get("WPMPlatforms");
				            			System.out.println("Platforms: "+plats);
				            			switch(mis.getResourceType()){
				            			case CPU 		: cache.put("WPMPlatform_NUM_CPUS:"+mis.getIp(), ""+num_of_res_ind);break;
				            			case DISK 		: cache.put("WPMPlatfom_MOUNTING_PTS:"+mis.getIp(), ""+mis_spec);break;
				            			case NETWORK 	: cache.put("WPMPlatfom_NETWORKS:"+mis.getIp(), ""+mis_spec);break;
				            			case JMX		: cache.put("WPMPlatfom_CACHES:"+mis.getIp(), "CloudTM");break;
				            			default : if(mis.getResourceType() != ResourceType.MEMORY){ throw new RuntimeException("Error!");};
				            			}
				            			System.out.println("WPMPlatform_NUM_CPUS:"+mis.getIp()+": "+cache.get("WPMPlatform_NUM_CPUS:"+mis.getIp()));
				            			System.out.println("WPMPlatfom_MOUNTING_PTS:"+mis.getIp()+": "+cache.get("WPMPlatfom_MOUNTING_PTS:"+mis.getIp()));
				            			System.out.println("WPMPlatfom_NETWORKS:"+mis.getIp()+": "+cache.get("WPMPlatfom_NETWORKS:"+mis.getIp()));
				            			System.out.println("WPMPlatfom_CACHES:"+mis.getIp()+": "+cache.get("WPMPlatfom_CACHES:"+mis.getIp()));


				            		}

				            	}
				            	
				            	
				            	if(enableListeners){
				            		
				            		
				            		
				            		
				            		
				            		
				            		
				            		
				            		//Try to publish here
				            		Iterator<StatsSubscriptionEntry> itr = this.observable.getStatsIterator();
				            		
				            		int i = 0;
				            		int size = this.observable.numStatsSubscriptions();
				            		
			            			PublishStatsEventInternal[] toPublish= new PublishStatsEventInternal[size]; 
			            			
			            			while(itr.hasNext()){
			            				
			            				
			            				toPublish[i] = itr.next().computePublishStatsEventInternal();
			            				
			            				i++;
			            				
			            			}
			            			
			            			
			            			for(i=0; i < size; i++){
			            				
			            				if(toPublish[i] != null){
			            					
			            					PublisherStatsThread pt = new PublisherStatsThread(toPublish[i], this.observable);
			            					
			            					pt.start();
			            				}
			            				
			            			}
				            		
				            		
				            		
				            	}
				            	
				            	
				            	
				            	
							    //System.out.println("Dataitem in cache: "+cache.size());
							    if(!activeFile.delete())
							    	System.out.println("ZIP file not delted!! "+activeFile);
							    //System.out.println("Deleted file: "+activeFile.getName());
				            }
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean checkJmxMembers(Measurement mis) {
	
		boolean result = false;
		
		if(mis != null && mis.getResourceType()!=null && mis.getResourceType().equals(ResourceType.JMX)){
			
			Long timestamp = this.jmxNodes.get(mis.getIp());
			
			if(timestamp == null){//New Member
				
				
				result = true;
				
			}
			
			this.jmxNodes.put(mis.getIp(), System.currentTimeMillis());
			
			
			ArrayList<MeasurementAttribute> list = mis.getMeasurementAttributes();
			
			for(MeasurementAttribute ma: list){
				
				if(ma.getShort_name().equalsIgnoreCase("numNodes")){
					this.numJmxNodes = Long.parseLong(ma.getValue());
					
				}
				
			}
			
			this.numCheckJmxNodes++;
			
			
			if(numCheckJmxNodes > 2*this.numJmxNodes){
				
				
				numCheckJmxNodes = 0;
				
				Set<Entry<String, Long>> set = this.jmxNodes.entrySet();
				List<KnownMember> all = new LinkedList<KnownMember>();
				
				long now = System.currentTimeMillis();
				
				for(Entry<String,Long> entry: set){
					
					all.add(new KnownMember(entry.getKey(), entry.getValue() - now)); //entry.getValue() - now is correct. We want that member with small delta (in this case a big negative number) go out.
					
				}
				
				Collections.sort(all);
				
				int toRemove = (all.size()) - (int)this.numJmxNodes;
				
				if(toRemove > 0){
					
					result = true;
					
					for(int i=0; i< toRemove; i++){
						
						all.remove(0);
						
					}
					
					
					this.jmxNodes.clear();
					
					for(KnownMember m: all){
						
						this.jmxNodes.put(m.address, now);
						
					}
					
				}
				
			}
		}
		
		
		return result;
	}

	private void loadParametersFromRegistry(){
    	String propsFile = "config/log_service.config";
    	Properties props = new Properties();
		try {
			props.load(new FileInputStream(propsFile));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		cacheName = props.getProperty("Cache_name");
		//infinispanConfigFile = props.getProperty("InfinispanConfigFile");
		timeout = Long.parseLong(props.getProperty("AnalyzerThreadTimeout"));
		enableInfinispan = Boolean.parseBoolean(props.getProperty("enableInfinispan"));
		enableListeners = Boolean.parseBoolean(props.getProperty("enableListeners"));
    }
	
	
		
		
	private class KnownMember implements Comparable<KnownMember>{
		String address;
		
		long delta;
		
		
		public KnownMember(String addres, long delta){
			
			
			this.address = address;
			this.delta = delta;
		}

		@Override
		public int compareTo(KnownMember arg0) {
			
			if(arg0 == null){
				
				return -1;
			}
			
			if(delta < arg0.delta){
				return -1;
			}
			else if(delta > arg0.delta){
				return 1;
			}
			else{
				return 0;
			}
		}
	}	
		
	
	
	
}
