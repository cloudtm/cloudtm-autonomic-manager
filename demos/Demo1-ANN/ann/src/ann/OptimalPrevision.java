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

/*
* @author Diego Rughetti
*/
public class OptimalPrevision {
	private double serverThroughput;
	private double serverResponse;
	private double replThroughput;
	private double replResponse;
	public OptimalPrevision(){
		this.serverThroughput=-1;
		this.serverResponse=-1;
		this.replThroughput = -1;
		this.replResponse = -1;
	}
	public OptimalPrevision(double a, double b, double c, double d){
		this.serverThroughput= a;
		this.serverResponse= b;
		this.replThroughput = c;
		this.replResponse = d;
	}
	public void setServerThroughput(double a){
		this.serverThroughput = a;
	}
	public void setServerResponseTime(double a){
		this.serverResponse = a;
	}
	public void setReplicationResponseTime(double a){
		this.replResponse = a;
	}
	public void setReplicationThroughput(double a){
		this.replThroughput = a;
	}
	public double getServerThroughput(){
		return this.serverThroughput;
	}
	public double getServerResponseTime(){
		return this.serverResponse;
	}
	public double getReplicationResponseTime(){
		return this.replResponse;
	}
	public double getReplicationThroughput(){
		return this.replThroughput;
	}
	public boolean equals(Object o){
		if(o.getClass().equals(this)){
			OptimalPrevision op = (OptimalPrevision) o;
			return (this.replResponse == op.replResponse && this.replThroughput == op.replThroughput && 
					this.serverResponse == op.serverResponse && this.serverThroughput == this.serverThroughput);
		}else{
			return false;
		}
	}
}
