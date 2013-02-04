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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/*
* @author Sebastiano Peluso
*/
public class PublishMeasurement implements Externalizable{
	
	String ip; 
	String group_ID; 
	String provider_ID;
	long timestamp;
	
	private HashMap<String, PublishAttribute> values;
	
	
	public PublishMeasurement(){
		
		values = new HashMap<String,PublishAttribute>();
	}
	
	public PublishMeasurement(String ip, String group_ID, String provider_ID, long timestamp){
		
		this.ip = ip;
		this.group_ID = group_ID;
		this.provider_ID = provider_ID;
		this.timestamp = timestamp;
		
		values = new HashMap<String,PublishAttribute>();
	}
	
	public void addMeasure(PublishAttribute measure){
		
		values.put(measure.getName(), measure);
		
	}
	
	public HashMap<String, PublishAttribute> getValues(){
		
		return this.values;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		
		this.ip = in.readUTF();
		this.provider_ID = in.readUTF();
		this.group_ID = in.readUTF();
		this.timestamp = in.readLong();
		
		int size = in.readInt();
		
		if(size > 0){
			
			String key;
			PublishAttribute value;
			for(int i = 0; i < size; i++){
				
				key = in.readUTF();
				value = (PublishAttribute) in.readObject();
				
				this.values.put(key, value);
			}
			
		}
		
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		
		out.writeUTF(this.ip);
		out.writeUTF(this.group_ID);
		out.writeUTF(this.provider_ID);
		out.writeLong(this.timestamp);
		
		out.writeInt(values.size());
		
		Set<Entry<String,PublishAttribute>> set = this.values.entrySet();
		Iterator<Entry<String,PublishAttribute>> itr = set.iterator();
		
		Entry<String,PublishAttribute> current;
		
		
		while(itr.hasNext()){
			current = itr.next();
			
			out.writeUTF(current.getKey());
			out.writeObject(current.getValue());
			
		}
		
	}
	

}
