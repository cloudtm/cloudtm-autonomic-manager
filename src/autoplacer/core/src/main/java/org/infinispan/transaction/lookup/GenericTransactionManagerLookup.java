/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
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
package org.infinispan.transaction.lookup;

import org.infinispan.config.Configuration;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.transaction.tm.DummyTransactionManager;
import org.infinispan.util.Util;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;

/**
 * A transaction manager lookup class that attempts to locate a TransactionManager. A variety of different classes and
 * JNDI locations are tried, for servers such as: <ul> <li> JBoss <li> JRun4 <li> Resin <li> Orion <li> JOnAS <li> BEA
 * Weblogic <li> Websphere 4.0, 5.0, 5.1, 6.0 <li> Sun, Glassfish </ul> If a transaction manager is not found, returns a
 * {@link org.infinispan.transaction.tm.DummyTransactionManager}.
 *
 * @author Markus Plesser
 * @since 4.0
 */
public class GenericTransactionManagerLookup implements TransactionManagerLookup {

   private static final Log log = LogFactory.getLog(GenericTransactionManagerLookup.class);

   /**
    * JNDI lookups performed?
    */
   private boolean lookupDone = false;

   /**
    * No JNDI available?
    */
   private boolean lookupFailed = false;

   /**
    * No JBoss TM embedded jars found?
    */
   private boolean noJBossTM = false;

   /**
    * The JTA TransactionManager found.
    */
   private TransactionManager tm = null;

   /**
    * JNDI locations for TransactionManagers we know of
    */
   private static String[][] knownJNDIManagers =
         {
               {"java:jboss/TransactionManager", "JBoss AS 7"},
               {"java:/TransactionManager", "JBoss AS 4 ~ 6, JRun4"},
               {"java:comp/TransactionManager", "Resin 3.x"},
               {"java:appserver/TransactionManager", "Sun Glassfish"},
               {"java:pm/TransactionManager", "Borland, Sun"},
               {"javax.transaction.TransactionManager", "BEA WebLogic"},
               {"java:comp/UserTransaction", "Resin, Orion, JOnAS (JOTM)"},
         };

   /**
    * WebSphere 5.1 and 6.0 TransactionManagerFactory
    */
   private static final String WS_FACTORY_CLASS_5_1 = "com.ibm.ws.Transaction.TransactionManagerFactory";

   /**
    * WebSphere 5.0 TransactionManagerFactory
    */
   private static final String WS_FACTORY_CLASS_5_0 = "com.ibm.ejs.jts.jta.TransactionManagerFactory";

   /**
    * WebSphere 4.0 TransactionManagerFactory
    */
   private static final String WS_FACTORY_CLASS_4 = "com.ibm.ejs.jts.jta.JTSXA";
   
   private Configuration configuration;
   
   @Inject
   public void setConfiguration(Configuration configuration) {
      this.configuration = configuration;
   }

   /**
    * Get the systemwide used TransactionManager
    *
    * @return TransactionManager
    */
   @Override
   public TransactionManager getTransactionManager() {
      if (!lookupDone)
         doLookups(configuration.getClassLoader());
      if (tm != null)
         return tm;
      if (lookupFailed) {
         if (!noJBossTM) {
            // First try an embedded JBossTM instance
            tryEmbeddedJBossTM();
         }

         if (noJBossTM) {
            //fall back to a dummy from Infinispan
            useDummyTM();
         }
      }
      return tm;
   }
   
   private void useDummyTM() {
      tm = DummyTransactionManager.getInstance();
      log.fallingBackToDummyTm();
   }
   
   private void tryEmbeddedJBossTM() {
      try {
         JBossStandaloneJTAManagerLookup jBossStandaloneJTAManagerLookup = new JBossStandaloneJTAManagerLookup();
         jBossStandaloneJTAManagerLookup.init(configuration);
         tm = jBossStandaloneJTAManagerLookup.getTransactionManager();
      } catch (Exception e) {
         noJBossTM = true;
      }
   }

   /**
    * Try to figure out which TransactionManager to use
    */
   private void doLookups(ClassLoader cl) {
      if (lookupFailed)
         return;
      InitialContext ctx = null;
      try {
         ctx = new InitialContext();
      }
      catch (NamingException e) {
         log.failedToCreateInitialCtx(e);
         lookupFailed = true;
         Util.close(ctx);
         return;
      }

      try {
         //probe jndi lookups first
         for (String[] knownJNDIManager : knownJNDIManagers) {
            Object jndiObject;
            try {
               log.debugf("Trying to lookup TransactionManager for %s", knownJNDIManager[1]);
               jndiObject = ctx.lookup(knownJNDIManager[0]);
            }
            catch (NamingException e) {
               log.debugf("Failed to perform a lookup for [%s (%s)]",
                         knownJNDIManager[0], knownJNDIManager[1]);
               continue;
            }
            if (jndiObject instanceof TransactionManager) {
               tm = (TransactionManager) jndiObject;
               log.debugf("Found TransactionManager for %s", knownJNDIManager[1]);
               return;
            }
         }
         lookupDone = true;
      } finally {
         Util.close(ctx);
      }

      //try to find websphere lookups since we came here
      // The TM may be deployed embedded alongside the app, so this needs to be looked up on the same CL as the Cache
      Class<?> clazz;
      try {
         log.debugf("Trying WebSphere 5.1: %s", WS_FACTORY_CLASS_5_1);
         clazz = Util.loadClassStrict(WS_FACTORY_CLASS_5_1, cl);
         log.debugf("Found WebSphere 5.1: %s", WS_FACTORY_CLASS_5_1);
      }
      catch (ClassNotFoundException ex) {
         try {
            log.debugf("Trying WebSphere 5.0: %s", WS_FACTORY_CLASS_5_0);
            clazz = Util.loadClassStrict(WS_FACTORY_CLASS_5_0, cl);
            log.debugf("Found WebSphere 5.0: %s", WS_FACTORY_CLASS_5_0);
         }
         catch (ClassNotFoundException ex2) {
            try {
               log.debugf("Trying WebSphere 4: %s", WS_FACTORY_CLASS_4);
               clazz = Util.loadClassStrict(WS_FACTORY_CLASS_4, cl);
               log.debugf("Found WebSphere 4: %s", WS_FACTORY_CLASS_4);
            }
            catch (ClassNotFoundException ex3) {
               log.debug("Couldn't find any WebSphere TransactionManager factory class, neither for WebSphere version 5.1 nor 5.0 nor 4");
               lookupFailed = true;
               return;
            }
         }
      }
      try {
         Class<?>[] signature = null;
         Object[] args = null;
         Method method = clazz.getMethod("getTransactionManager", signature);
         tm = (TransactionManager) method.invoke(null, args);
      }
      catch (Exception ex) {
         log.unableToInvokeWebsphereStaticGetTmMethod(ex, clazz.getName());
      }
   }

}
