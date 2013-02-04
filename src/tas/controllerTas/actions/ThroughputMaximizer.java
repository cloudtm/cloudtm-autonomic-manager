package controllerTas.actions;/*
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

import Tas2.core.ModelResult;
import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.exception.Tas2Exception;
import Tas2.core.Tas2;
import controllerTas.common.DSTMScenarioFactory;
import controllerTas.common.Scale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ThroughputMaximizer {

   private int maxNodes;
   private int minNodes;
   private int minThreads;
   private int maxThreads;

   private final DSTMScenarioFactory factory = new DSTMScenarioFactory();

   private final static Log log = LogFactory.getLog(ThroughputMaximizer.class);

   public ThroughputMaximizer(int minNodes, int maxNodes, int minThreads, int maxThreads) {
      this.maxNodes = maxNodes;
      this.minNodes = minNodes;
      this.minThreads = minThreads;
      this.maxThreads = maxThreads;
   }

   public Scale computeMaxThroughputScale(DSTMScenarioTas2 scenario) throws Tas2Exception {
      ModelResult result;
      Scale optScale = null, tempScale;
      double maxTh = 0, tempTh;
      //TODO scenario should be cloned
      for (int n = minNodes; n <= maxNodes; n++) {
         for (int t = minThreads; t <= maxThreads; t++) {
            scenario.getWorkParams().setNumNodes(n);
            scenario.getWorkParams().setThreadsPerNode(t);
            tempScale = new Scale(n, t);
            log.trace("Going to query for "+tempScale);
            try {
               result = new Tas2().solve(scenario);
               tempTh = result.getMetrics().getThroughput();
               log.info(tempScale+" throughput = " +tempTh*1e9 + ", rtt = "+result.getMetrics().getPrepareRtt()+", abortProb = "+(1.0D - result.getProbabilities().getPrepareProbability() * result.getProbabilities().getCoherProbability()));
               if (tempTh > maxTh) {
                  optScale = tempScale;
                  maxTh = tempTh;
               }
            } catch (Tas2Exception e) {
               log.debug("The model did not converge for " + tempScale);
               log.info("TAS does not converge: throughput = 0, abortProb = 1.0");
            }
         }
      }
      if (optScale == null) {
         throw new Tas2Exception("The model never converges");
      }
      return optScale;
   }

}
