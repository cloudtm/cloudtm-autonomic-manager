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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.*;

/*
* @Author Diego Rughetti
*/
public class Demo {
	
	private Nns nn;
	private int minReplication;
	private int maxNodeNumber;
	
	
	public Demo(Nns nn, int a, int b){
		this.nn = nn;
		this.minReplication = a;
		this.maxNodeNumber = b;
	}
	
	public void ExecuteDemo(String fileInput, String fileOutput){
		BufferedReader br;
		FileInputStream fis;
		InputStreamReader isr;
		FileOutputStream fos;
		PrintStream ps;
		String stringa;
		OptimalPrevision op;
		int numeroClient;
		try{
			File f = new File(fileInput);
			fis = new FileInputStream(f);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			File output = new File(fileOutput);
			fos = new FileOutputStream(output);
			ps = new PrintStream(fos);
			stringa = br.readLine();
			while(stringa !=null ){
				numeroClient = Integer.parseInt(stringa);
				op = this.nn.getOptimalPrevision(minReplication, maxNodeNumber, numeroClient);
				ps.println(numeroClient + ", " + op.getServerThroughput() + ", " + op.getReplicationThroughput()
							+ ", " + op.getServerResponseTime() + ", " + op.getReplicationResponseTime());
			}
			ps.close();
			fos.close();
			br.close();
			isr.close();
			fis.close();
		}catch(Exception e){
			
		}
	}
}
