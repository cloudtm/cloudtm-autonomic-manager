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


import java.util.Properties;

/*
* @author Diego Rughetti
*/
public final class Configuration {
	
	private Properties props;
	
	private static Configuration instance;
	
	private Configuration(Properties props){
		
		this.props = props;
		
	}
	
	public static synchronized Configuration getOrCreateConfiguration(Properties props){
		
		if(Configuration.instance == null){
			
			Configuration.instance = new Configuration(props);
			
		}
		
		return Configuration.instance;
		
		
	} 
	
	public static Configuration getConfiguration(){
		
		return instance;
		
	}
	
	
	public String getValue(String propertyName){
		
		return this.props.getProperty(propertyName);
		
	}
	
	

}
