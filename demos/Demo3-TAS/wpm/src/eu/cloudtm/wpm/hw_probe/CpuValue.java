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
 
package eu.cloudtm.wpm.hw_probe;

/*
* @author Roberto Palmieri
*/
class CpuValue{
	private int number;
	private double idle;
	//private double nice;
	private double system;
	private double user;
	
	public double getIdle() {
		return idle;
	}
	
	public void setIdle(double idle) {
		this.idle = idle;
	}
	/*
	public double getNice() {
		return nice;
	}
	public void setNice(double nice) {
		this.nice = nice;
	}
	*/
	public double getSystem() {
		return system;
	}
	public void setSystem(double system) {
		this.system = system;
	}
	public double getUser() {
		return user;
	}
	public void setUser(double user) {
		this.user = user;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "CPU"+number+"[idle:"+idle*100+"%|system:"+system*100+"%|user:"+user*100+"%]";
	}
	
}