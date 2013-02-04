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

import org.infinispan.config.Configuration;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

/*
* @author Roberto Palmieri
*/
public class ClearCache {

	public static void main(String[] args) {
		GlobalConfiguration gc = GlobalConfiguration.getClusteredDefault();
		gc.setClusterName("LogServiceConnection");
		Configuration c = new Configuration();
		c.setCacheMode(Configuration.CacheMode.REPL_SYNC);
		c.setExpirationLifespan(-1);
		c.setExpirationMaxIdle(-1);
		EmbeddedCacheManager cm = new DefaultCacheManager(gc, c);
		System.out.println("Dataitem in cache: "+cm.getCache().size());
		cm.getCache().clear();
		System.out.println("Cache empty");
		System.exit(0);
	}

}
