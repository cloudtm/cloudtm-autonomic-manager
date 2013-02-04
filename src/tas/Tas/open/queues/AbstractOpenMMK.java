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

package open.queues;

import java.util.Arrays;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/10/12
 */
public abstract class AbstractOpenMMK extends AbstractMMK {


   protected Clazz[] serviceTimes;

   private boolean WEIGHT_ENABLED = true;

   protected AbstractOpenMMK(double numServers, Clazz... serviceTimes) {
      super(numServers);
      this.serviceTimes = serviceTimes;
   }


   public double getClassServiceTime(int clazz) {
      return serviceTimes[clazz].getServiceTime();
   }


   protected final double avgServiceTime(OpenClazz[] serviceTimes) {
      double sum = 0D;
      double totalLambda = this.effectiveLambda();
      OpenClazz openClazz;
      for (Clazz c : serviceTimes) {
         openClazz = (OpenClazz) c;
         sum += c.getServiceTime() * openClazz.getLambda() / totalLambda;
      }
      return sum;
   }


   public abstract double utilization(int clazz);

   public final double utilization() {
      double ro = 0D;
      for (Clazz c : this.serviceTimes)
         ro += this.utilization(c.getClazz());
      return ro;
   }


   public OpenClazz[] getOpenClazzes() {
      return Arrays.copyOf(serviceTimes, serviceTimes.length, OpenClazz[].class);
   }


   protected double effectiveLambda() {
      double sum = 0D;
      OpenClazz openClazz;
      for (Clazz c : this.serviceTimes) {
         openClazz = (OpenClazz) c;
         sum += getLambdaIfAlsoMu(openClazz);
      }
      return sum;
   }

   private double referenceServiceTime() {
      double max = 0D;
      double temp;
      for (Clazz c : this.serviceTimes) {
         temp = c.getServiceTime();
         if (temp > max)
            max = temp;
      }
      return max;
   }

   public double _effectiveLambda() {
      return this.effectiveLambda();
   }

   protected double getLambdaIfAlsoMu(OpenClazz o) {
      if (o.getServiceTime() != 0)
         return o.getLambda() * weight(o);
      return 0D;
   }


   protected double weight(OpenClazz o) {
      return WEIGHT_ENABLED ? o.getServiceTime() / referenceServiceTime() : 1;
   }
}
