package controllerTas.actions;  /*
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
import Tas2.core.Tas2;
import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.exception.Tas2Exception;
import controllerTas.common.KPI;
import controllerTas.common.Scale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/02/13
 */
public class WhatIfAnalyzer {

   private int maxNodes;
   private int minNodes;
   private int minThreads;
   private int maxThreads;

   private final static Log log = LogFactory.getLog(WhatIfAnalyzer.class);

   public WhatIfAnalyzer(int minNodes, int maxNodes, int minThreads, int maxThreads) {
      this.maxNodes = maxNodes;
      this.minNodes = minNodes;
      this.minThreads = minThreads;
      this.maxThreads = maxThreads;
   }

   public Set<KPI> computeKPI(DSTMScenarioTas2 scenario) {
      Scale tempScale;
      ModelResult result;
      double throughput, abortP, rtt;
      Set<KPI> kpis = new HashSet<KPI>();
      for (int n = minNodes; n <= maxNodes; n++) {
         for (int t = minThreads; t <= maxThreads; t++) {
            scenario.getWorkParams().setNumNodes(n);
            scenario.getWorkParams().setThreadsPerNode(t);
            tempScale = new Scale(n, t);
            log.trace("Going to query for " + tempScale);
            try {
               result = new Tas2().solve(scenario);
               throughput = result.getMetrics().getThroughput();
               rtt = result.getMetrics().getPrepareRtt();
               abortP = (1.0D - result.getProbabilities().getPrepareProbability() * result.getProbabilities().getCoherProbability());
               log.info(tempScale + " throughput = " + throughput * 1e9 + " txs/sec, rtt = " + rtt + " msec, abortProb = " + abortP);
               kpis.add(new KPI(tempScale, throughput, abortP, rtt));
            } catch (Tas2Exception e) {
               log.debug("The model did not converge for " + tempScale);
               log.info(tempScale+ " throughput = 0.0 txs/sec, rtt = N/A, abortProb = 1.0");
            }
         }
      }
      return kpis;
   }

   @Override
   public String toString() {
      return "WhatIfAnalyzer{" +
              "maxNodes=" + maxNodes +
              ", minNodes=" + minNodes +
              ", minThreads=" + minThreads +
              ", maxThreads=" + maxThreads +
              '}';
   }
}