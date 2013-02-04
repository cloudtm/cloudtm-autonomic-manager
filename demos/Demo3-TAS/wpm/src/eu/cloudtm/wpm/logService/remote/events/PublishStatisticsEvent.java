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
 
package eu.cloudtm.wpm.logService.remote.events;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import eu.cloudtm.wpm.parser.ResourceType;

/*
* @author Sebastiano Peluso
*/
public class PublishStatisticsEvent extends PublishEvent {
	
    private HashMap<String, Integer> ips;
	
    private HashMap<String, Integer> numCpu;
    private HashMap<String, Integer> numMemory;
    private HashMap<String, Integer> numJmx;
    private HashMap<String, Integer> numNetwork;
    private HashMap<String, Integer> numDisk;
    
    private HashMap<String, PublishMeasurement> cpuValues;   
    private HashMap<String, PublishMeasurement> memoryValues;
    private HashMap<String, PublishMeasurement> jmxValues;
    private HashMap<String, PublishMeasurement> networkValues;
    private HashMap<String, PublishMeasurement> diskValues;
    
	public PublishStatisticsEvent(){
		super();
		ips = new HashMap<String, Integer>();
		cpuValues = new HashMap<String, PublishMeasurement>();
		memoryValues = new HashMap<String, PublishMeasurement>();
		jmxValues = new HashMap<String, PublishMeasurement>();
		networkValues = new HashMap<String, PublishMeasurement>();
		diskValues = new HashMap<String, PublishMeasurement>();
		
		numCpu= new HashMap<String, Integer>();
	    numMemory= new HashMap<String, Integer>();
	    numJmx= new HashMap<String, Integer>();
	    numNetwork= new HashMap<String, Integer>();
	    numDisk= new HashMap<String, Integer>();
	}
	
	public PublishStatisticsEvent(String[] ips){
		super();
		this.ips = new HashMap<String, Integer>();
		
		if(ips != null){
			
			for(int i = 0; i< ips.length; i++){
				this.ips.put(ips[i], i);
			}
		}
		
		cpuValues = new HashMap<String, PublishMeasurement>();
		memoryValues = new HashMap<String, PublishMeasurement>();
		jmxValues = new HashMap<String, PublishMeasurement>();
		networkValues = new HashMap<String, PublishMeasurement>();
		diskValues = new HashMap<String, PublishMeasurement>();
		
		numCpu= new HashMap<String, Integer>();
	    numMemory= new HashMap<String, Integer>();
	    numJmx= new HashMap<String, Integer>();
	    numNetwork= new HashMap<String, Integer>();
	    numDisk= new HashMap<String, Integer>();
		
	}
	
	
	
	
	public Set<String> getIps() {
		return ips.keySet();
	}

	public void addMeasure(String ip, String group_ID, String provider_ID, long timestamp, ResourceType resourceType, List<PublishAttribute> attributes){
		
		
		Integer VMIndex = this.ips.get(ip);
		
		if(VMIndex != null  && (resourceType.equals(ResourceType.DISK) || resourceType.equals(ResourceType.JMX) || resourceType.equals(ResourceType.MEMORY) || resourceType.equals(ResourceType.NETWORK) || resourceType.equals(ResourceType.CPU))){
			
			
				
				if(attributes != null){
					
					Set<Integer> resourcesIndexes = new HashSet<Integer>();
					
					Iterator<PublishAttribute> itr = attributes.iterator();
					PublishAttribute current;
					PublishMeasurement pm = null;
					String key;
					
					while(itr.hasNext()){
						current = itr.next();
						
						key = String.valueOf(VMIndex);
						
						key+=""+current.getResourceIndex();
						
						resourcesIndexes.add(current.getResourceIndex());
						
						if(resourceType.equals(ResourceType.CPU)){
							pm = this.cpuValues.get(key);
						}
						else if(resourceType.equals(ResourceType.DISK)){
							pm = this.diskValues.get(key);
						}
						else if(resourceType.equals(ResourceType.JMX)){
							pm = this.jmxValues.get(key);
						}
						else if(resourceType.equals(ResourceType.MEMORY)){
							pm = this.memoryValues.get(key);
						}
						else if(resourceType.equals(ResourceType.NETWORK)){
							pm = this.networkValues.get(key);
						}
						
						if(pm == null){
							pm = new PublishMeasurement(ip, group_ID, provider_ID, timestamp);
							
							if(resourceType.equals(ResourceType.CPU)){
								this.cpuValues.put(key, pm);
							}
							else if(resourceType.equals(ResourceType.DISK)){
								this.diskValues.put(key, pm);
							}
							else if(resourceType.equals(ResourceType.JMX)){
								this.jmxValues.put(key, pm);
							}
							else if(resourceType.equals(ResourceType.MEMORY)){
								this.memoryValues.put(key, pm);
							}
							else if(resourceType.equals(ResourceType.NETWORK)){
								this.networkValues.put(key, pm);
							}
						}
						
						
						pm.addMeasure(current);
						
					}
					
					key = ""+VMIndex;
					
					if(resourceType.equals(ResourceType.CPU)){
						this.numCpu.put(key, resourcesIndexes.size());
					}
					else if(resourceType.equals(ResourceType.DISK)){
						this.numDisk.put(key, resourcesIndexes.size());
					}
					else if(resourceType.equals(ResourceType.JMX)){
						this.numJmx.put(key, resourcesIndexes.size());
					}
					else if(resourceType.equals(ResourceType.MEMORY)){
						this.numMemory.put(key, resourcesIndexes.size());
					}
					else if(resourceType.equals(ResourceType.NETWORK)){
						this.numNetwork.put(key, resourcesIndexes.size());
					}
					
				}
				
			
			
		}
		
	}
	
	public PublishMeasurement getPublishMeasurement(ResourceType resourceType, int resourceIndex, String ip){
		
		Integer VMIndex = this.ips.get(ip);
		
		if(VMIndex != null){
			
			String key = String.valueOf(VMIndex);
			
			key+=""+resourceIndex;
			
			if(resourceType.equals(ResourceType.CPU)){
				return this.cpuValues.get(key);
			}
			else if(resourceType.equals(ResourceType.DISK)){
				return this.diskValues.get(key);
			}
			else if(resourceType.equals(ResourceType.JMX)){
				return this.jmxValues.get(key);
			}
			else if(resourceType.equals(ResourceType.MEMORY)){
				return this.memoryValues.get(key);
			}
			else if(resourceType.equals(ResourceType.NETWORK)){
				return this.networkValues.get(key);
			}
			else{
				return null;
			}
			
			
		}
		else{
			return null;
		}
		
		
	}
	
	public int getNumResources(ResourceType resourceType, String ip){
		
		Integer VMIndex = this.ips.get(ip);
		
		if(VMIndex != null){
			
			String key = String.valueOf(VMIndex);
			Integer value = null;
			if(resourceType.equals(ResourceType.CPU)){
				value = this.numCpu.get(key);
				if(value==null){
					return 0;
				}
				return value;
			}
			else if(resourceType.equals(ResourceType.DISK)){
				value = this.numDisk.get(key);
				
				if(value == null){
					return 0;
				}
				
				return value;
			}
			else if(resourceType.equals(ResourceType.JMX)){
				
				value = this.numJmx.get(key);
				if(value == null){
					return 0;
				}
				return value;
			}
			else if(resourceType.equals(ResourceType.MEMORY)){
				value = this.numMemory.get(key);
				if(value == null){
					return 0;
				}
				 return value;
			}
			else if(resourceType.equals(ResourceType.NETWORK)){
				value = this.numNetwork.get(key);
				if(value == null){
					return 0;
				}
				
				return value;
			}
			else{
				return 0;
			}
			
			
		}
		else{
			return 0;
		}
		
		
		
		
	}
	

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		
		int size = in.readInt();
		
		if(size > 0){
			
			String key;
			Integer value;
			
			for(int i = 0; i < size; i++){
				key = in.readUTF();
				value = in.readInt();
				this.ips.put(key, value);
				
			}
			
		}
		
		
		size = in.readInt();
		
		if(size > 0){
			
			String key;
			Integer value;
			
			for(int i = 0; i < size; i++){
				key = in.readUTF();
				value = in.readInt();
				this.numCpu.put(key, value);
				
			}
			
		}
		
		size = in.readInt();
		
		if(size > 0){
			
			String key;
			Integer value;
			
			for(int i = 0; i < size; i++){
				key = in.readUTF();
				value = in.readInt();
				this.numMemory.put(key, value);
				
			}
			
		}
		
		size = in.readInt();
		
		if(size > 0){
			
			String key;
			Integer value;
			
			for(int i = 0; i < size; i++){
				key = in.readUTF();
				value = in.readInt();
				this.numJmx.put(key, value);
				
			}
			
		}

		
		size = in.readInt();
		
		if(size > 0){
			
			String key;
			Integer value;
			
			for(int i = 0; i < size; i++){
				key = in.readUTF();
				value = in.readInt();
				this.numNetwork.put(key, value);
				
			}
			
		}
		
		size = in.readInt();
		
		if(size > 0){
			
			String key;
			Integer value;
			
			for(int i = 0; i < size; i++){
				key = in.readUTF();
				value = in.readInt();
				this.numDisk.put(key, value);
				
			}
			
		}
		
		size = in.readInt();
		
		if(size > 0){
			
			String key;
			PublishMeasurement value;
			
			for(int i = 0; i < size; i++){
				key = in.readUTF();
				value = (PublishMeasurement) in.readObject();
				this.cpuValues.put(key, value);
				
			}
			
		}
		
		size = in.readInt();
		
		if(size > 0){
			
			String key;
			PublishMeasurement value;
			
			for(int i = 0; i < size; i++){
				key = in.readUTF();
				value = (PublishMeasurement) in.readObject();
				this.memoryValues.put(key, value);
				
			}
			
		}
		
		size = in.readInt();
		
		if(size > 0){
			
			String key;
			PublishMeasurement value;
			
			for(int i = 0; i < size; i++){
				key = in.readUTF();
				value = (PublishMeasurement) in.readObject();
				this.jmxValues.put(key, value);
				
			}
			
		}
		
		size = in.readInt();
		
		if(size > 0){
			
			String key;
			PublishMeasurement value;
			
			for(int i = 0; i < size; i++){
				key = in.readUTF();
				value = (PublishMeasurement) in.readObject();
				this.networkValues.put(key, value);
				
			}
			
		}
		
		size = in.readInt();
		
		if(size > 0){
			
			String key;
			PublishMeasurement value;
			
			for(int i = 0; i < size; i++){
				key = in.readUTF();
				value = (PublishMeasurement) in.readObject();
				this.diskValues.put(key, value);
				
			}
			
		}
		
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	    
	    int size = this.ips.size();
	    
	    out.writeInt(size);
	    Set<Entry<String,Integer>> set = this.ips.entrySet();
	    Iterator<Entry<String,Integer>> itr = set.iterator();
	    Entry<String,Integer> current;
	    while(itr.hasNext()){
	    	current = itr.next();
	    	
	    	out.writeUTF(current.getKey());
	    	out.writeInt(current.getValue());
	    }
	    
	    
	    
	    
	    size = this.numCpu.size();
	    
	    out.writeInt(size);
	    
	    set = this.numCpu.entrySet();
	    itr = set.iterator();
	    
	    while(itr.hasNext()){
	    	current = itr.next();
	    	
	    	out.writeUTF(current.getKey());
	    	out.writeInt(current.getValue());
	    }
	    
	    size = this.numMemory.size();
	    
	    out.writeInt(size);
	    
	    set = this.numMemory.entrySet();
	    itr = set.iterator();
	    
	    while(itr.hasNext()){
	    	current = itr.next();
	    	
	    	out.writeUTF(current.getKey());
	    	out.writeInt(current.getValue());
	    }
	    
	    
	    size = this.numJmx.size();
	    
	    out.writeInt(size);
	    
	    set = this.numJmx.entrySet();
	    itr = set.iterator();
	    
	    while(itr.hasNext()){
	    	current = itr.next();
	    	
	    	out.writeUTF(current.getKey());
	    	out.writeInt(current.getValue());
	    }
	    
	    
	    size = this.numNetwork.size();
	    
	    out.writeInt(size);
	    
	    set = this.numNetwork.entrySet();
	    itr = set.iterator();
	    
	    while(itr.hasNext()){
	    	current = itr.next();
	    	
	    	out.writeUTF(current.getKey());
	    	out.writeInt(current.getValue());
	    }
	    
	    size = this.numDisk.size();
	    
	    out.writeInt(size);
	    
	    set = this.numDisk.entrySet();
	    itr = set.iterator();
	    
	    while(itr.hasNext()){
	    	current = itr.next();
	    	
	    	out.writeUTF(current.getKey());
	    	out.writeInt(current.getValue());
	    }
	    
	    
	    
	    
	    size = this.cpuValues.size();
	    
	    out.writeInt(size);
	    Set<Entry<String, PublishMeasurement>> setValues = this.cpuValues.entrySet();
	    Iterator<Entry<String, PublishMeasurement>> itrValues = setValues.iterator();
	    Entry<String, PublishMeasurement> currentValue;
	    while(itrValues.hasNext()){
	    	currentValue = itrValues.next();
	    	
	    	out.writeUTF(currentValue.getKey());
	    	out.writeObject(currentValue.getValue());
	    }
	    
	    
	    size = this.memoryValues.size();
	    
	    out.writeInt(size);
	    setValues = this.memoryValues.entrySet();
	    itrValues = setValues.iterator();
	    
	    while(itrValues.hasNext()){
	    	currentValue = itrValues.next();
	    	
	    	out.writeUTF(currentValue.getKey());
	    	out.writeObject(currentValue.getValue());
	    }
	    
	    size = this.jmxValues.size();
	    
	    out.writeInt(size);
	    setValues = this.jmxValues.entrySet();
	    itrValues = setValues.iterator();
	    
	    while(itrValues.hasNext()){
	    	currentValue = itrValues.next();
	    	
	    	out.writeUTF(currentValue.getKey());
	    	out.writeObject(currentValue.getValue());
	    }
	    
	    
	    size = this.networkValues.size();
	    
	    out.writeInt(size);
	    setValues = this.networkValues.entrySet();
	    itrValues = setValues.iterator();
	    
	    while(itrValues.hasNext()){
	    	currentValue = itrValues.next();
	    	
	    	out.writeUTF(currentValue.getKey());
	    	out.writeObject(currentValue.getValue());
	    }
	    
	    size = this.diskValues.size();
	    
	    out.writeInt(size);
	    setValues = this.diskValues.entrySet();
	    itrValues = setValues.iterator();
	    
	    while(itrValues.hasNext()){
	    	currentValue = itrValues.next();
	    	
	    	out.writeUTF(currentValue.getKey());
	    	out.writeObject(currentValue.getValue());
	    }
	    
	}

}
