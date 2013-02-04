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
 
package eu.cloudtm.wpm.parser;
import java.util.ArrayList;
import java.util.Iterator;


/*
* @author Roberto Palmieri
*/
public class Measurement {
	private long probeID;
	private String component_ID;
	private String ip;
	private String group_ID;
	private String provider_ID;
	private long    seq;
	private long timestamp;
	private ResourceType resourceType;
	private ArrayList<MeasurementAttribute> measurementAttributes;
	private String source;

	public Measurement(){
		this.component_ID=null;
		this.ip=null;
		this.resourceType=null;
		this.measurementAttributes=new ArrayList<MeasurementAttribute>();
	}
	
	public long getProbeID() {
		return probeID;
	}

	public void setProbeID(long probeID) {
		this.probeID = probeID;
	}

	public String getComponent_ID() {
		return component_ID;
	}

	public void setComponent_ID(String component_ID) {
		this.component_ID = component_ID;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getGroup_ID() {
		return group_ID;
	}

	public void setGroup_ID(String group_ID) {
		this.group_ID = group_ID;
	}

	public String getProvider_ID() {
		return provider_ID;
	}

	public void setProvider_ID(String provider_ID) {
		this.provider_ID = provider_ID;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public ArrayList<MeasurementAttribute> getMeasurementAttributes() {
		return measurementAttributes;
	}

	public void setMeasurementAttributes(
			ArrayList<MeasurementAttribute> measurementAttributes) {
		this.measurementAttributes = measurementAttributes;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	protected void deleteAttributes(){
		this.measurementAttributes=null;
		this.measurementAttributes=new ArrayList<MeasurementAttribute>();
	}
	
	public String toString(){
		String lista="";
		Iterator<MeasurementAttribute> it=measurementAttributes.listIterator();
		while(it.hasNext()){
			MeasurementAttribute m=it.next();
			lista+=m.toString()+" ";
		}

		return "identification: "+this.component_ID+" ip: "+this.ip+" group_ID: "+this.group_ID+" providerID: "+this.provider_ID+" probeID: "+this.probeID+" seq: "+this.seq+" timestamp: "
		+this.timestamp+" type: "+this.resourceType+" attributes: "+lista;
	}
	
	public String getAttributesForInfinispan(int res_index){
		String attributes = "";
		Iterator<MeasurementAttribute> it=measurementAttributes.listIterator();
		while(it.hasNext()){
			MeasurementAttribute m = it.next();
			if(m.getResource_index() == res_index){
				attributes += m.getShort_name()+"$$"+m.getValue()+";";
			}
				
		}
		return attributes;
	}
	public int getNumberOfResIndex(){
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		Iterator<MeasurementAttribute> it=measurementAttributes.listIterator();
		while(it.hasNext()){
			MeasurementAttribute m=it.next();
			if(!indexes.contains(m.getResource_index()))
				indexes.add(m.getResource_index());
		}
		return indexes.size();
	}
	
}
