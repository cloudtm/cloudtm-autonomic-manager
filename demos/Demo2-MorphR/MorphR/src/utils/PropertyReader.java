/*
 * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
 * Copyright 2013 INESC-ID and/or its affiliates and other
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

/**
 * @author      Maria Couceiro <mcouceiro@gsd.inesc-id.pt>
 * @version     1.0               
 * @since       2013-02-01          
 */

package utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class PropertyReader {

	private PropertyReader(){}

	private static Properties boot(String propsFile){
			try {
				return readProperties(propsFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
	}

	private static Properties readProperties(String propsFile) throws IOException{
		Properties props = new Properties();
		URL props_url = PropertyReader.class.getResource(propsFile);
		if(props_url == null)
			throw new IOException("Could not find properties file: "+propsFile);
		InputStream is = props_url.openStream();
		props.load(is);
		is.close();
		return props;
	}
	
	public static String getString(String prop, String newPropsFile){
		Properties props = boot(newPropsFile);
		return props.getProperty(prop);
	}
	
	public static int getInt(String prop, String newPropsFile){
		Properties props = boot(newPropsFile);
		return Integer.parseInt(props.getProperty(prop));
	}

	public static long getLong(String prop, String newPropsFile){
		Properties props = boot(newPropsFile);
		return Long.parseLong(props.getProperty(prop));
	}
	
	public static double getDouble(String prop, String newPropsFile){
		Properties props = boot(newPropsFile);
		return Double.parseDouble(props.getProperty(prop));
	}

	public static boolean getBoolean(String prop, String newPropsFile){
		Properties props = boot(newPropsFile);
		return Boolean.parseBoolean(props.getProperty(prop));
	}
}