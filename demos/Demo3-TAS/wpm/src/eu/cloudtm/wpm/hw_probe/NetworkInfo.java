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

import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.Free;

/**
 * @author Roberto Palmieri
 * Display amount of free and used memory in the system.
 */

public class NetworkInfo {
	
	private Sigar handle_sigar;
	private long[] last_Rx_bytes;
	private long[] last_Tx_bytes;
	private long lastTimestamp;
	private int num_interfaces;
	
    public NetworkInfo() {
    	handle_sigar = new Sigar();
    	try {
			num_interfaces = handle_sigar.getNetInterfaceList().length;
		} catch (SigarException e) {
			e.printStackTrace();
		} 
    }

    public ArrayList<NetworkValue> getNetworkValues(){
    	String[] net_names = null;
    	try {
			net_names = handle_sigar.getNetInterfaceList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ArrayList<NetworkValue> net_values = new ArrayList<NetworkValue>(net_names.length);
		if(last_Rx_bytes == null || last_Tx_bytes == null){
			last_Rx_bytes = new long[net_names.length];
			last_Tx_bytes = new long[net_names.length];
		}
		long currentTimestamp = System.currentTimeMillis();
    	for (int i=0; i<net_names.length; i++) {
    		NetInterfaceStat stat_interf = null;
			try {
				stat_interf = handle_sigar.getNetInterfaceStat(net_names[i]);
			} catch (SigarException e) {
				e.printStackTrace();
			}
    		NetworkValue net = new NetworkValue();
    		if(last_Rx_bytes[i] == 0 || last_Tx_bytes[i] == 0){
    			net.setRx_bytes(0);
        		net.setTx_bytes(0);
        		net.setRx_brandwidth(0);
        		net.setTx_brandwidth(0);
    		}else{
    			net.setRx_bytes(stat_interf.getRxBytes() - last_Rx_bytes[i]);
        		net.setTx_bytes(stat_interf.getTxBytes() - last_Tx_bytes[i]);
        		
        		net.setRx_brandwidth(net.getRx_bytes()/((currentTimestamp-lastTimestamp)/1000));
        		net.setTx_brandwidth(net.getTx_bytes()/((currentTimestamp-lastTimestamp)/1000));
    		}
    		last_Rx_bytes[i] = stat_interf.getRxBytes();
    		last_Tx_bytes[i] = stat_interf.getTxBytes();
    		net_values.add(net);
    		//System.out.println("------\t"+net_names[i]+"-"+stat_interf.getSpeed());
    		//System.out.println(stat_interf.getRxBytes());
    		//System.out.println(stat_interf.getTxBytes());
        }
    	lastTimestamp = System.currentTimeMillis();
		return net_values;
    }
    public ArrayList<NetworkValue> getDefaultValues(){
		ArrayList<NetworkValue> net_values = new ArrayList<NetworkValue>(getNumberInterfaces());
    	for (int i=0; i<getNumberInterfaces(); i++) {
    		NetworkValue net = new NetworkValue();
			net.setRx_bytes(-1);
    		net.setTx_bytes(-1);
    		net.setRx_brandwidth(-1);
    		net.setTx_brandwidth(-1);
    		net_values.add(net);
        }
		return net_values;
    }
    
    public int getNumberInterfaces(){
		return num_interfaces;
    }
    
    /*    
    private void output(CpuPerc cpu) {
    	throw new RuntimeException("Do Not Call this method");
    }
    public void output(String[] args) throws SigarException {
    	throw new RuntimeException("Do Not Call this method");
    }
    
    public String getUsageShort() {
        return "Display information about free and used memory";
    }

    private static Long format(long value) {
        return new Long(value / 1024);
    }

    public void output(String[] args) throws SigarException {
    	MemoryInfo mem   = this.sigar.getMem();
        Swap swap = this.sigar.getSwap();

        Object[] header = new Object[] { "total", "used", "free" };

        Object[] memRow = new Object[] {
            format(mem.getTotal()),
            format(mem.getUsed()),
            format(mem.getFree())
        };

        Object[] actualRow = new Object[] {
            format(mem.getActualUsed()),
            format(mem.getActualFree())
        };

        Object[] swapRow = new Object[] {
            format(swap.getTotal()),
            format(swap.getUsed()),
            format(swap.getFree())
        };

        printf("%18s %10s %10s", header);

        printf("Mem:    %10ld %10ld %10ld", memRow);

        //e.g. linux
        if ((mem.getUsed() != mem.getActualUsed()) ||
            (mem.getFree() != mem.getActualFree()))
        {
            printf("-/+ buffers/cache: " + "%10ld %10d", actualRow);
        }

        printf("Swap:   %10ld %10ld %10ld", swapRow);

        printf("RAM:    %10ls", new Object[] { mem.getRam() + "MB" });
    }
     */
    public static void main(String[] args) throws Exception {
        new Free().processCommand(args);
    }
}

