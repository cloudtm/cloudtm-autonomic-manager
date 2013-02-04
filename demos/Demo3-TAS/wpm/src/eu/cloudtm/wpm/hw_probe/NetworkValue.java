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
class NetworkValue{
	private long tx_bytes;
	private long rx_bytes;
	private double tx_brandwidth;
	private double rx_brandwidth;
	
	public long getTx_bytes() {
		return tx_bytes;
	}
	public void setTx_bytes(long txBytes) {
		tx_bytes = txBytes;
	}
	public long getRx_bytes() {
		return rx_bytes;
	}
	public void setRx_bytes(long rxBytes) {
		rx_bytes = rxBytes;
	}
	public double getTx_brandwidth() {
		return tx_brandwidth;
	}
	public void setTx_brandwidth(double txBrandwidth) {
		tx_brandwidth = txBrandwidth;
	}
	public double getRx_brandwidth() {
		return rx_brandwidth;
	}
	public void setRx_brandwidth(double rxBrandwidth) {
		rx_brandwidth = rxBrandwidth;
	}
	
	
}