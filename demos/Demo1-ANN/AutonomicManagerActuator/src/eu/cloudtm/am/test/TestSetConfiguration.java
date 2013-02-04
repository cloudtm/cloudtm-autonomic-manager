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

package eu.cloudtm.am.test;

import eu.cloudtm.am.client.ActuatorClient;

/*
* @author Sebastiano Peluso
*/
public class TestSetConfiguration {


    public static void main(String[] args){
     if(args== null || args.length != 2){
            System.out.println("Usage: java TestChangeCOnfiguration <Actuator Server IP> <Atuator Server Port>");
        }

        ActuatorClient client = new ActuatorClient(args[0], Integer.parseInt(args[1]));


            client.setConfiguration(1, 2);


    }
}
