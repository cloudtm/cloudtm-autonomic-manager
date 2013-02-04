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

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.cmd.Free;

/**
 * @author Roberto Palmieri
 * Display amount of free and used memory in the system.
 */
public class MemoryInfo {
	
	private Sigar handle_sigar;
	
    public MemoryInfo() {
    	handle_sigar = new Sigar();
    }

    public MemoryValue getMemoryValues(){
    	Mem mem = new Mem();
    	try {
			mem = handle_sigar.getMem();
		} catch (Exception e) {
			e.printStackTrace();
		}
		MemoryValue mem_value = new MemoryValue();
		//mem_value.setFree(mem.getFreePercent());
		//mem_value.setUsed(mem.getUsedPercent());
		mem_value.setFree(mem.getFree());
		mem_value.setUsed(mem.getUsed());
		return mem_value;
    }
    public MemoryValue getDefaultValues(){
		MemoryValue mem_value = new MemoryValue();
		mem_value.setFree(-1);
		mem_value.setUsed(-1);
		return mem_value;
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

