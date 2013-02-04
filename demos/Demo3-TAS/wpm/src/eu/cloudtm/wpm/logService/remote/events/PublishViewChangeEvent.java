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

/*
* @author Sebastiano Peluso
*/
public class PublishViewChangeEvent extends PublishEvent{
	
	
	private String[] currentVMs;
	
	public PublishViewChangeEvent(){
		
	}
	
	

	public String[] getCurrentVMs() {
		return currentVMs;
	}



	public PublishViewChangeEvent(String[] currentVMs){
		this.currentVMs = currentVMs;
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		int size = in.readInt();
		
		if(size > 0){
			this.currentVMs = new String[size];
			
			for(int i=0; i<this.currentVMs.length; i++){
				this.currentVMs[i] = in.readUTF();
			}
		}
		
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		if(this.currentVMs != null){
			
			out.writeInt(this.currentVMs.length);
			
			for(int i=0; i<this.currentVMs.length; i++){
				out.writeUTF(this.currentVMs[i]);
			}
		}
		else{
			out.writeInt(0);
		}
		
	}

}
