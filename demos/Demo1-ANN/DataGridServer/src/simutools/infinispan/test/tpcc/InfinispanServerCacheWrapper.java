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
 
package simutools.infinispan.test.tpcc;

import org.infinispan.Cache;
import org.radargun.CacheWrapper;
import org.radargun.utils.TypedProperties;

/**
 * @author Sebastiano Peluso
 */
public class InfinispanServerCacheWrapper implements CacheWrapper {

    private Cache infinispanCache;

    public InfinispanServerCacheWrapper(Cache cache){
         this.infinispanCache = cache;
    }


    public void setUp(String s, boolean b, int i, TypedProperties typedProperties, String s1) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void tearDown() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void put(String s, Object o, Object o1) throws Exception {

        this.infinispanCache.put(o,o1);
    }

    public Object get(String s, Object o) throws Exception {
        return this.infinispanCache.get(o);
    }

    public void empty() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getNumMembers() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getInfo() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getReplicatedData(String s, String s1) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void startTransaction() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void endTransaction(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int size() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CacheWrapper transactify() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean populateTpcc(int i, int i1, int i2, long l, long l1, long l2) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
