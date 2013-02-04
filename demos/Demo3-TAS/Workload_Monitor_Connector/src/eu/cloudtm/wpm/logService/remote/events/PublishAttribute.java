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

import eu.cloudtm.wpm.parser.ResourceType;

/*
* @author Sebastiano Peluso
*/
public class PublishAttribute<T> implements Externalizable{
	
	private ResourceType resourceType;
	private int resourceIndex;
	
	private String name;
	private T value;
	
	public PublishAttribute() {
		
	}

	public PublishAttribute(ResourceType resourceType, int resourceIndex, String name, T value){
		
		this.resourceType = resourceType;
		this.resourceIndex = resourceIndex;
		this.name=name;
		this.value=value;
	}
	
	public T getValue(){
		
		return this.value;
	}
	
	public String getName(){
		return this.name;
	}
	
	

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public int getResourceIndex() {
		return resourceIndex;
	}

	public void setResourceIndex(int resourceIndex) {
		this.resourceIndex = resourceIndex;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		
		this.resourceType = ResourceType.valueOf(ResourceType.class, in.readUTF());
		this.resourceIndex = in.readInt();
		this.name = in.readUTF();
		this.value = (T) in.readObject();
		
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		
		out.writeUTF(this.resourceType.name());
		out.writeInt(this.resourceIndex);
		out.writeUTF(this.name);
		out.writeObject(this.value);
		
	}

}
