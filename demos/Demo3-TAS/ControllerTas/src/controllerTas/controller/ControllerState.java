package controllerTas.controller;     /*
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

import controllerTas.common.Scale;
import controllerTas.config.configs.DemoTransitoryConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/02/13
 */
public class ControllerState {

   private Scale currentScale;
   private AtomicBoolean maskInterrupt = new AtomicBoolean(false);
   private long lastReset;
   private long lastInit;
   private DemoTransitoryConfig demoConfig;

   private final static Log log = LogFactory.getLog(ControllerState.class);

   public ControllerState(Scale currentScale) {
      this.currentScale = currentScale;
      long now = System.currentTimeMillis();
      lastReset = now;
      lastInit = now;
   }

   public ControllerState(Scale currentScale, DemoTransitoryConfig demoConfig) {
      this.currentScale = currentScale;
      long now = System.currentTimeMillis();
      lastReset = now;
      lastInit = now;
      this.demoConfig = demoConfig;
   }

   public boolean testAndSetMaskInterrupt(boolean compare, boolean set) {
      return maskInterrupt.compareAndSet(compare, set);
   }

   public void atomicSetMaskInterrupt(boolean set) {
      this.maskInterrupt.set(set);
   }

   public void resetTimeWindow() {
      lastReset = System.currentTimeMillis();
   }

   public Scale getCurrentScale() {
      return currentScale;
   }

   public void setCurrentScale(Scale currentScale) {
      this.currentScale = currentScale;
   }

   public long getLastTimeWindow() {
      long now = System.currentTimeMillis();
      long ret = now - lastReset;
      return ret;
   }

   public boolean isStable(int numPuts) {
      if (demoConfig == null) {
         return true;
      }
      if (numPuts > demoConfig.getMaxPutsForRealXacts()) {
         log.info("Number of puts per transaction is too high (" + numPuts + "), thus I think tpcc is still populating");
         return false;
      }
      long elapsed = System.currentTimeMillis() - lastInit;
      long toElapse = demoConfig.getTranstitoryTime() * 1000 - elapsed;
      if (toElapse > 0) {
         log.info("It's too soon to consider the system stable. Wait for " + toElapse / 1000 + " seconds");
         return false;
      }
      return true;

   }
}
