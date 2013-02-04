/*
 *
 *  * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
 *  * Copyright 2013 INESC-ID and/or its affiliates and other
 *  * contributors as indicated by the @author tags. All rights reserved.
 *  * See the copyright.txt in the distribution for a full listing of
 *  * individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 3.0 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package Tas2.core;

import Tas2.logicalModel.solver.probabilities.Probabilities;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */
public class Tas2ModelResult implements ModelResult {

   private Probabilities probabilities;
   private Metrics metrics;
   private boolean stable = true;

   public void markAsUnstable() {
      this.stable = false;
   }

   public boolean isStable() {
      return this.stable;
   }

   public Probabilities getProbabilities() {
      return probabilities;
   }

   public void setProbabilities(Probabilities probabilities) {
      this.probabilities = probabilities;
   }

   public Metrics getMetrics() {
      return metrics;
   }

   public void setMetrics(Metrics metric) {
      this.metrics = metric;
   }

   @Override
   public String toString() {
      return "Tas2ModelResult{" +
              "probabilities=" + probabilities +
              ", metrics=" + metrics +
              '}';
   }
}
