package controllerTas.config.configs;/*
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
 *         Date: 02/02/13
 */
/*
 In this class we have all the information needed to understand when the transitory phase relevant to the population
 of the cache for the demo is over.
 We rely on a simple heuristic, to avoid over-engineering: in the TpccPopulation element in the benchmark.xml file
 in Radargun there's a "batch" attribute, defining the number of item contained in each population-phase transaction.
 This number is huge (~200) whereas tpcc transactions should no go over 25 puts/transactions
 Thus, as long as the write per transactions are greater than 50 or a minimum amount ot time has not elapsed,
 the system will not process collected data.

 */
public class DemoTransitoryConfig {

   private long transtitoryTime = 400;   //in sec
   private long maxPutsForRealXacts = 50;

   public long getTranstitoryTime() {
      return transtitoryTime;
   }

   public void setTranstitoryTime(long transtitoryTime) {
      this.transtitoryTime = transtitoryTime;
   }

   public long getMaxPutsForRealXacts() {
      return maxPutsForRealXacts;
   }

   public void setMaxPutsForRealXacts(long maxPutsForRealXacts) {
      this.maxPutsForRealXacts = maxPutsForRealXacts;
   }
}
