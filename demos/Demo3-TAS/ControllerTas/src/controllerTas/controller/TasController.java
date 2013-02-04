package controllerTas.controller;
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
import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.exception.Tas2Exception;
import controllerTas.actions.ThroughputMaximizer;
import controllerTas.actions.WhatIfAnalyzer;
import controllerTas.actions.gnuplot.GnuplotException;
import controllerTas.actions.gnuplot.ThroughputPlotter;
import controllerTas.config.configs.TasControllerConfiguration;
import controllerTas.common.DSTMScenarioFactory;
import controllerTas.common.KPI;
import controllerTas.common.PublishAttributeException;
import controllerTas.common.Scale;
import controllerTas.wpm.TasWPMViewChangeRemoteListenerImpl;
import eu.cloudtm.wpm.connector.WPMConnector;
import eu.cloudtm.wpm.logService.remote.events.PublishAttribute;
import eu.cloudtm.wpm.logService.remote.listeners.WPMViewChangeRemoteListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


public class TasController {

   private WPMConnector connector;
   private static final Log log = LogFactory.getLog(TasController.class);
   private AtomicBoolean maskInterrupt = new AtomicBoolean(false);
   private ThroughputMaximizer throughputMaximizer;
   private WhatIfAnalyzer analyzer;
   private DSTMScenarioFactory factory = new DSTMScenarioFactory();  //DummyScenarioFactory
   private ControllerState state;

   private TasControllerConfiguration config;

   public TasController(TasControllerConfiguration conf) throws RemoteException, UnknownHostException, NotBoundException {
      this.config = conf;
      init(this.config);
      log.info("TasController created: Going to create the WPMConnector for it");
      connector = new WPMConnector();
      log.info("WPMConnector created. Attaching the viewChangeRemoteListener");
      WPMViewChangeRemoteListener viewChange = new TasWPMViewChangeRemoteListenerImpl(connector, this);
      connector.registerViewChangeRemoteListener(viewChange);
      log.info("TasController is set up");

   }

   private void init(TasControllerConfiguration config) {
      log.trace("Creating TasController with configuration:\n" + config);
      int minN = config.getScaleConfig().getMinNumNodes();
      int maxN = config.getScaleConfig().getMaxNumNodes();
      int minT = config.getScaleConfig().getMinNumThreads();
      int maxT = config.getScaleConfig().getMaxNumThreads();
      this.throughputMaximizer = new ThroughputMaximizer(minN, maxN, minT, maxT);
      this.analyzer = new WhatIfAnalyzer(minN, maxN, minT, maxT);

      int initN = config.getScaleConfig().getInitNumNodes();
      int initT = config.getScaleConfig().getInitNumThreads();
      Scale initScale = new Scale(initN, initT);

      this.state = new ControllerState(initScale,config.getDemoTransitoryConfig());

   }

   public void consumeStats(Set<HashMap<String, PublishAttribute>> jmx, Set<HashMap<String, PublishAttribute>> mem) throws PublishAttributeException, Tas2Exception {

      if (state.testAndSetMaskInterrupt(false, true)) {
         try {
            double timeWindow = state.getLastTimeWindow() / 1e3;
            state.resetTimeWindow();
            log.info("Analyzing stats relevant to the last " + timeWindow + " sec");
            DSTMScenarioTas2 scenario = factory.buildScenario(jmx, mem, timeWindow, state.getCurrentScale().getNumThreads());
            if(!state.isStable((int)scenario.getWorkParams().getWriteOpsPerTx())) {
               return;
            }
            log.trace("BuiltScenario\n" + scenario.toString());
            Set<KPI> kpis = analyzer.computeKPI(scenario);
            log.trace("KPIs " + kpis.toString());
            ThroughputPlotter gnu = new ThroughputPlotter(config.getGnuplotConfig());
            gnu.plot(kpis);
            //this.throughputMaximizer.computeMaxThroughputScale(scenario);
         } catch (Tas2Exception t) {
            log.warn(t);
            log.trace(Arrays.toString(t.getStackTrace()));
            log.trace("Skipping");
         } catch (GnuplotException g) {
            log.warn(g.getMessage());
         }
         catch (Exception e){
            log.fatal(e.getMessage());
            log.fatal(Arrays.toString(e.getStackTrace()));
         }
         finally {
            log.trace("Resetting maskInterrupt");
            state.atomicSetMaskInterrupt(false);
         }
      } else {
         state.resetTimeWindow();
         log.trace("Masked interrupt");
      }
   }

   public boolean canProcessNewData(){
      return !this.maskInterrupt.get();
   }

   public void resetStateTimeWindow(){
      this.state.resetTimeWindow();
   }

}
