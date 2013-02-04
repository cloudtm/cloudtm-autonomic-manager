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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/*
* @author Roberto Palmieri
*/
public class LogService {
	static int port_num;
	static final int filesize = 2048;

	public static void main(String[] args) throws RemoteException {
		//start analyzer thread 
		new LogServiceAnalyzer();
		
		//start ack thread
		//new LogServiceAck();
		
		//receive zip + check file
		try{
			loadParametersFromRegistry();
			//ServerSocket servsock = new ServerSocket(port_num);
			//-------
			ServerSocket servsock = getServer();
			//-------
			while (true) {
				//System.out.println("Log Service Waiting...");
				Socket sock = servsock.accept();
				InputStream is = sock.getInputStream();
				DataInputStream dis = new DataInputStream(is);
				//receive Zip file
				File zipFile = receiveFile(dis);
				//receive Check file
				File checkFile = receiveFile(dis);
				checkZipFile(checkFile,zipFile);
				dis.close();
				is.close();
				sock.close();
				//System.out.println("Now I can process...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static void loadParametersFromRegistry(){
    	String propsFile = "config/log_service.config";
    	Properties props = new Properties();
		try {
			props.load(new FileInputStream(propsFile));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		port_num = Integer.parseInt(props.getProperty("Port_number"));
    }
	public static ServerSocket getServer() throws Exception {
		SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocket serversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(port_num);
		return serversocket;
	}
	
	private static void checkZipFile(File checkFile, File zipFile){
		//System.out.println("checking files...");
		try{
			FileInputStream f_check_stream = new FileInputStream(checkFile);
		    DataInputStream check_in = new DataInputStream(f_check_stream);
		    BufferedReader br = new BufferedReader(new InputStreamReader(check_in));
		    FileInputStream f_zip_stream = new FileInputStream(zipFile);
		    CheckedInputStream check = new CheckedInputStream(f_zip_stream, new Adler32());
		    BufferedInputStream in_zip = new BufferedInputStream(check);
		    while (in_zip.read() != -1);
		    String strLine = "";
		    if((strLine = br.readLine()) != null){
		    	try{
		    		long fileCheck_cks = Long.parseLong(strLine);
		    		long fileZip_cks = check.getChecksum().getValue();
		    		//System.out.println(fileCheck_cks+"-"+fileZip_cks);
		    		if(fileCheck_cks == fileZip_cks){
		    			copyfile(zipFile,new File("log/ls_processed/"+zipFile.getName()));
			    		checkFile.delete();
			    		zipFile.delete();
		    		}
		    	}catch(Exception e){
		    		e.printStackTrace();
		    		throw new RuntimeException("Bad check file");
		    	}
		    }
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private static File receiveFile(DataInputStream dis){
		try{
			byte [] fileInByte = new byte [filesize];
		    int count = 0;
		    int sizeOfName = dis.readInt();
		    count = dis.read(fileInByte, 0, sizeOfName);
		    String filename = new String(fileInByte,0,count);
		    File nameFileToStore = new File("log/ls_unprocessed/"+filename);
		    FileOutputStream fos = new FileOutputStream(nameFileToStore);
			BufferedOutputStream dest = new BufferedOutputStream(fos, filesize);
		    int sizeOfFile = dis.readInt();
		    int totalSize = 0;
		    while((count = dis.read(fileInByte, 0, filesize)) != -1) {
				dest.write(fileInByte, 0, count);
				totalSize+=count;
				if(totalSize == sizeOfFile)
					break;
			}
		    dest.flush();
		    dest.close();
		    fos.close();
			//System.out.println("File "+filename+" written!!");
			return nameFileToStore;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private static void copyfile(File f1, File f2){
		try{
			InputStream in = new FileInputStream(f1);
			//For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			//System.out.println("File copied.");
		}catch(FileNotFoundException ex){
			System.out.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
	}
}