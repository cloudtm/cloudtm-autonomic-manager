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

import java.util.ArrayList;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
/*
* @author Roberto Palmieri
*/
public class DiskInfo {
	private Sigar handle_sigar;
	private int num_fs;
	
    public DiskInfo() {
    	handle_sigar = new Sigar();
    	try {
			num_fs = handle_sigar.getFileSystemList().length;
		} catch (SigarException e) {
			e.printStackTrace();
		}
    }

    public ArrayList<DiskValue> getDiskValues(){
    	FileSystem[] fs_list = null;
    	try {
    		fs_list = handle_sigar.getFileSystemList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ArrayList<DiskValue> fs_values = new ArrayList<DiskValue>(fs_list.length);
		for(FileSystem fs : fs_list){
			FileSystemUsage fs_usage = null;
			try {
				fs_usage = handle_sigar.getFileSystemUsage(fs.getDirName());
			} catch (SigarException e) {
				e.printStackTrace();
			}
			DiskValue disk_value = new DiskValue();
			disk_value.setFree((new Double(1))-fs_usage.getUsePercent());
			disk_value.setUsed(fs_usage.getUsePercent());
			disk_value.setMounting_point(fs.getDirName());
			fs_values.add(disk_value);
		}
		return fs_values;
    }
    public ArrayList<DiskValue> getDefaultValues(){
		ArrayList<DiskValue> fs_values = new ArrayList<DiskValue>(getNumberFileSystems());
		for(int i=0;i<getNumberFileSystems();i++){
			DiskValue disk_value = new DiskValue();
			disk_value.setFree(-1);
			disk_value.setUsed(-1);
			disk_value.setMounting_point("");
			fs_values.add(disk_value);
		}
		return fs_values;
    }
    
    public int getNumberFileSystems(){
		return num_fs;
    }
}
