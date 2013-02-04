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
 
package eu.cloudtm.wpm.sw_probe;

import java.util.Map;

/*
* @author Roberto Palmieri
*/
class InfinispanTopK {

	private Map RemoteTopPuts;
	private Map TopLockFailedKeys;
	private Map TopLockedKeys;
	private Map TopContendedKeys;
	private Map LocalTopPuts;
	private Map LocalTopGets;
	private Map RemoteTopGets;

	public InfinispanTopK() {

	}

	public Map getRemoteTopPuts() {
		return RemoteTopPuts;
	}

	public void setRemoteTopPuts(Map remoteTopPuts) {
		RemoteTopPuts = remoteTopPuts;
	}

	public Map getTopLockFailedKeys() {
		return TopLockFailedKeys;
	}

	public void setTopLockFailedKeys(Map topLockFailedKeys) {
		TopLockFailedKeys = topLockFailedKeys;
	}

	public Map getTopLockedKeys() {
		return TopLockedKeys;
	}

	public void setTopLockedKeys(Map topLockedKeys) {
		TopLockedKeys = topLockedKeys;
	}

	public Map getTopContendedKeys() {
		return TopContendedKeys;
	}

	public void setTopContendedKeys(Map topContendedKeys) {
		TopContendedKeys = topContendedKeys;
	}

	public Map getLocalTopPuts() {
		return LocalTopPuts;
	}

	public void setLocalTopPuts(Map localTopPuts) {
		LocalTopPuts = localTopPuts;
	}

	public Map getLocalTopGets() {
		return LocalTopGets;
	}

	public void setLocalTopGets(Map localTopGets) {
		LocalTopGets = localTopGets;
	}

	public Map getRemoteTopGets() {
		return RemoteTopGets;
	}

	public void setRemoteTopGets(Map remoteTopGets) {
		RemoteTopGets = remoteTopGets;
	}

}