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

/*
* @author Sebastiano Peluso
*/
public class SubscribeEvent implements Externalizable{
	
	private String[] VMs;
	
	public SubscribeEvent(){
		
	}
	
	public SubscribeEvent(String[] VMs){
		this.VMs = VMs;
	}
	
	

	public String[] getVMs() {
		return VMs;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		int size = in.readInt();
		
		if(size > 0){
			this.VMs = new String[size];
			
			for(int i=0; i<this.VMs.length; i++){
				this.VMs[i] = in.readUTF();
			}
		}
		
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		if(this.VMs != null){
			
			out.writeInt(this.VMs.length);
			
			for(int i=0; i<this.VMs.length; i++){
				out.writeUTF(this.VMs[i]);
			}
		}
		else{
			out.writeInt(0);
		}
		
	}
	
	
	

}
