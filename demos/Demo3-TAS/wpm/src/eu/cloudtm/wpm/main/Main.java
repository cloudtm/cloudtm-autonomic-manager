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
 
package eu.cloudtm.wpm.main;

import java.rmi.RemoteException;

import eu.cloudtm.wpm.consumer.ResourceConsumer;
import eu.cloudtm.wpm.logService.LogService;
import eu.cloudtm.wpm.producer.HwResourceMain;

/*
* @author Roberto Palmieri
*/
public class Main {

	public static void main(String[] args) throws RemoteException {
		if(args.length != 1){
			System.out.println("Bad Parameter, use only [logService][producer][consumer]...");
			System.exit(0);
		}
		String parameter = args[0];
		Component component_to_run = null;
		if(parameter.equalsIgnoreCase("logService"))
			component_to_run = Component.LOG_SERVICE;
		else if(parameter.equalsIgnoreCase("producer"))
			component_to_run = Component.PRODUCER;
		else if(parameter.equalsIgnoreCase("consumer"))
			component_to_run = Component.CONSUMER;
		else{
			System.out.println("Bad Parameter, use only [logService][producer][consumer]...");
			System.exit(0);
		}
		switch (component_to_run) {
			case LOG_SERVICE : LogService.main(null); break;
			case CONSUMER : ResourceConsumer.main(null); break;
			case PRODUCER : HwResourceMain.main(null);break;
			default:{System.out.println("Bad Parameter, use only [logService][producer][consumer]...");System.exit(0);}
		}
	}
}

enum Component{
	PRODUCER,
	CONSUMER,
	LOG_SERVICE;
}