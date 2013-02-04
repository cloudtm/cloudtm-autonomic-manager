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
 
package eu.cloudtm.wpm.logService;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/*
* @author Roberto Palmieri
*/
public class LogServiceAck implements Runnable{
	private int consumer_port;
	private long timeout;
	
	public LogServiceAck(){
		loadParametersFromRegistry();
		Thread ack_thread = new Thread(this,"Log Service Ack");
		ack_thread.start();
	}

	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				//System.out.println("Running Log Service Ack Thread!!");
				File active_folder = new File("log/ls_worked");
				if(active_folder.isDirectory()){
					for(File activeFile : active_folder.listFiles()){
						if(!activeFile.getName().endsWith(".ack"))
							continue;
						try {
							//take the ip on the filename sent by consumer
							//activeFile contains the IP to send the ack file
							//stat_127.0.0.1_0_1338842406981.log
							String IP_consumer = new String("");
							if(activeFile.getName().startsWith("stat_")){
								String filename_no_stat = activeFile.getName().substring(5);
								IP_consumer = filename_no_stat.substring(0, filename_no_stat.indexOf("_"));
							}
							//String IP_consumer = activeFile.
							SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
							SSLSocket sock = (SSLSocket) factory.createSocket(IP_consumer, consumer_port);
							OutputStream os = sock.getOutputStream();
							InputStream is = sock.getInputStream();
							DataOutputStream dos = new DataOutputStream (os);
							DataInputStream dis = new DataInputStream(is);
							
							sendFile(activeFile,dos);
							//System.out.println("Ack sent for file "+activeFile.getName());
							String msg = receiveAck(dis);
							//System.out.println("Ack received "+msg);
							if(msg.equals("ACK"))
								activeFile.delete();
							else if(msg.equals("NACK"))
								System.out.println("NACK received");
							dos.close();
							dis.close();
				            os.close();
				            sock.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadParametersFromRegistry(){
    	String propsFile = "config/log_service.config";
    	Properties props = new Properties();
		try {
			props.load(new FileInputStream(propsFile));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		consumer_port = Integer.parseInt(props.getProperty("Consumer_ack_port_number"));
		timeout = Long.parseLong(props.getProperty("AckThreadTimeout"));
    }
	
	private void sendFile(File file,DataOutputStream dos){
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			byte [] fileInByte = new byte [(int)file.length()];
			//System.out.println("FileName sending..."+file.getName()+" bytes "+file.getName().getBytes().length);
			dos.writeInt(file.getName().getBytes().length);
			dos.flush();
			dos.write(file.getName().getBytes());
			dos.flush();
			//System.out.println("FileName written");
			dos.writeInt((int)file.length());
			dos.flush();
			bis.read(fileInByte, 0, fileInByte.length);
			dos.write(fileInByte);
			dos.flush();
            bis.close();
            fis.close();
            //System.out.println("File "+file.getName()+" sent!!");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String receiveAck(DataInputStream dis){
		try{
			byte [] msgInByte = new byte [1024];
		    int count = 0;
		    int sizeOfMsg = dis.readInt();
		    count = dis.read(msgInByte, 0, sizeOfMsg);
		    String msg = new String(msgInByte,0,count);
			return msg;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
