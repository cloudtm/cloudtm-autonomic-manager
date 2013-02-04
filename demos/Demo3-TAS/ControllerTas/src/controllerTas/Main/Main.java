package controllerTas.Main;/*
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
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/02/13
 */

import controllerTas.config.TasControllerConfigurationFactory;
import controllerTas.config.configs.TasControllerConfiguration;
import controllerTas.controller.TasControllerThread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;


public class Main {
   private static final Log log = LogFactory.getLog(Main.class);

   public static void main(String[] args) throws IOException {
      Thread.currentThread().setName("ControllerTas");
      PropertyConfigurator.configure("conf/log4j.properties");
      TasControllerConfiguration config = TasControllerConfigurationFactory.buildConfiguration("conf/controller.xml", "controllerTas.config.configs.");
      log.info("Startng TasController");
      TasControllerThread tasControllerThread = new TasControllerThread(config);
      try {
         tasControllerThread.start();
         log.info("TasController created. Now waiting for the magic.");
         tasControllerThread.join();
      } catch (InterruptedException e) {
         e.printStackTrace();
         System.exit(-1);
      }
      log.trace("I'm out :(");
   }

}
