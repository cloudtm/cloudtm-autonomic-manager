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
 
package simutools.infinispan.server;

import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.manager.DefaultCacheManager;

import javax.transaction.TransactionManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/*
* @author Sebastiano Peluso
*/

public class Server {


    public static void main(String[] args){


        // read arguments
        if (args.length!=2) {
            System.out.println("Usage: java Server <host> <infinispan_config_file>");
            System.exit(-1);
        }
        int port = 2222;
        String address = args[0];
        String config = args[1];


        DefaultCacheManager cacheManager=null;
        Cache<Object, Object> cache = null;
        TransactionManager tm = null;

        Executor executor = null;


        ServerSocket socket = null;

        try {
            cacheManager = new DefaultCacheManager(config);
            // use a named cache, based on the 'default'
            //cacheManager.defineConfiguration("x", new Configuration());
            //cache = cacheManager.getCache("x");

            cache = cacheManager.getCache();
            tm=cache.getAdvancedCache().getTransactionManager();



            executor = Executors.newFixedThreadPool(64);


            socket = new ServerSocket(port, 0, InetAddress.getByName(address));



        } catch (IOException e) {
            System.err.println("Could not start server: " + e);
            System.exit(-1);
        }
        System.out.println("Infinispan Server accepting connections on port " + port);


        // request handler loop
        while (true) {
            Socket connection = null;
            try {
                // wait for request
                connection = socket.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }

            TransactionalService service = new TransactionalService(connection, tm, cache, cacheManager);

            executor.execute(service);
        }




    }
}
