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
 
package eu.cloudtm.wpm.consumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.ProbeValue;
import eu.reservoir.monitoring.core.Reporter;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import eu.reservoir.monitoring.distribution.ConsumerMeasurementWithMetaData;
import eu.reservoir.monitoring.distribution.ConsumerMeasurementWithMetadataAndProbeName;

/*
* @author Roberto Palmieri
*/
public class ResourceReporter implements Reporter {
	private InfoPlane infoModel;
	private String id_consumer;
	private long lastTimestamp;
	private long timeToRefresh;
	
	public ResourceReporter(InfoPlane infoPlane,String id_cons,long refresh_period) {
		infoModel = infoPlane;
		id_consumer = id_cons;
		timeToRefresh = refresh_period;
	}
	
	public void report(Measurement m) {
		//String probeName = (String)infoModel.lookupProbeInfo(m.getProbeID(), "name");
		//System.out.print("Received msg from probe"+probeName + " => ");
		long currentTimestamp = System.currentTimeMillis();
		File logFile = new File("log/stat_"+id_consumer+".log");
		try{
			FileWriter fstream = new FileWriter(logFile,true);
			BufferedWriter out = new BufferedWriter(fstream);
			//out.write("Name:"+probeName+"Value:"+m.toString()+"\n");
			if(m instanceof ConsumerMeasurementWithMetadataAndProbeName){
				ConsumerMeasurementWithMetadataAndProbeName cm = (ConsumerMeasurementWithMetadataAndProbeName) m;
				//out.write(m.toString()+"\n");
				out.write(formatMeasurement(cm));
			}else if(m instanceof ConsumerMeasurementWithMetaData){
				ConsumerMeasurementWithMetaData cm = (ConsumerMeasurementWithMetaData) m;
				//out.write(ng()+"\n");
				
				out.write(formatMeasurementWithName(cm,infoModel));
			}else{
				throw new RuntimeException("Unsupported measurement message");
			}
			System.out.println("File updated!!");
			out.close();
			if(currentTimestamp - lastTimestamp >= timeToRefresh){
				System.out.print("Generating zip file...");
				byte [] logFileByteArray  = new byte [(int)logFile.length()];
				FileInputStream fis = new FileInputStream(logFile);
				fis = new FileInputStream(logFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				//Create zip file to store
				File logFileZip = new File("log/active/"+(logFile.getName().substring(0,logFile.getName().lastIndexOf(".log")))+"_"+lastTimestamp+"_"+currentTimestamp+".log.zip");
				FileOutputStream dest = new FileOutputStream(logFileZip);
				CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
				ZipOutputStream outZip = new ZipOutputStream(new BufferedOutputStream(checksum));
				ZipEntry entry = new ZipEntry(logFileZip.getName().substring(0,logFileZip.getName().lastIndexOf(".zip")));
				outZip.putNextEntry(entry);
	            bis.read(logFileByteArray, 0, logFileByteArray.length);
	            outZip.write(logFileByteArray,0,logFileByteArray.length);
	            outZip.close();
	            bis.close();
	            fis.close();
	            System.out.println("done!");
	            System.out.println ("Zip file stored: "+logFile.getPath());
	            File checkFile = new File("log/active/"+logFileZip.getName().substring(0,logFileZip.getName().lastIndexOf(".zip"))+".check");
	            FileWriter fw = new FileWriter(checkFile);
	            BufferedWriter checkFile_writer = new BufferedWriter(fw);
	            checkFile_writer.write(new String(""+checksum.getChecksum().getValue()));
	            checkFile_writer.close();
	            fw.close();
	            System.out.println ("Check file stored: "+checkFile.getPath());
	            File readyFile = new File("log/active/"+logFileZip.getName().substring(0,logFileZip.getName().lastIndexOf(".zip"))+".ready");
	            if(!readyFile.createNewFile())
	            	throw new RuntimeException("Error while creating ready file");
	            System.out.println ("Ready file stored: "+readyFile.getPath());
				lastTimestamp = currentTimestamp;
				logFile.delete();
			}
			//System.out.print("Fine elaborazione");
		}catch(Exception ex){
			ex.printStackTrace();
			//delete file if is bigger than 100Mb
			try{
				if(logFile.length() > 100000000)
					logFile.delete();
			}catch(Exception e){
				e.printStackTrace();
			}
			System.exit(0);
		}
	}
	public static String formatMeasurement(ConsumerMeasurementWithMetadataAndProbeName m){
		return ""+m.getSequenceNo()+":"+m.getProbeID()+":"+m.getProbeName()+":"+m.getTimestamp()+":"+m.getType()+m.getValues()+"\n";
	}
	public static String formatMeasurementWithName(ConsumerMeasurementWithMetaData m, InfoPlane infoModel2){
		String probeName = (String)infoModel2.lookupProbeInfo(m.getProbeID(), "name");
		//System.out.println(m.getValues());
		String values = "[";
		for(ProbeValue pv : m.getValues()){
			String name_att = (String)infoModel2.lookupProbeAttributeInfo(m.getProbeID(), pv.getField(), "name");
			values += pv.getField()+": "+name_att+": "+pv.getType()+" "+pv.getValue()+", ";
		}
		values = values.substring(0,values.length()-1)+"]";
		
		return ""+m.getSequenceNo()+":"+m.getProbeID()+":"+probeName+":"+m.getTimestamp()+":"+m.getType()+values+"\n";
	}
}