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

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 17/11/12
 */
@Deprecated
public class MeanOpenMMK extends LoadIndependentOpenMMK {

   private OpenClazz[] completeClazzes;

   public MeanOpenMMK(double numServers, OpenClazz... serviceTimes) {
      super(numServers);
      completeClazzes = serviceTimes;
      this.serviceTimes = averageServiceTimes(serviceTimes);
      System.out.println(this);
   }

   @Deprecated
   public double getClassServiceTime(int clazz) {
      return this.completeClazzes[clazz].getServiceTime();
   }

   protected double __responseTime(int clazz) {
      double lambda = ((OpenClazz) this.serviceTimes[0]).getLambda();
      double mu = 1.0D / this.serviceTimes[0].getServiceTime();
      return 1.0D / (mu - lambda);
   }

   private Clazz[] averageServiceTimes(OpenClazz... serviceTimes) {
      double totalLambda = effectiveLambda(serviceTimes);

      Clazz[] ret = new Clazz[1];
      //double avgServiceTime  = avgServiceTime(serviceTimes);
      double avgServiceTime = __avgServiceTime(serviceTimes);
      ret[0] = new OpenClazz(0, avgServiceTime, totalLambda);
      return ret;
   }


   protected double effectiveLambda(OpenClazz... serviceTimes) {
      double sum = 0D;
      OpenClazz openClazz;
      for (Clazz c : serviceTimes) {
         openClazz = (OpenClazz) c;
         sum += getLambdaIfAlsoMu(openClazz);
      }
      return sum;
   }

   protected double getLambdaIfAlsoMu(OpenClazz o) {
      if (o.getServiceTime() != 0)
         return o.getLambda();
      return 0D;
   }


   private double __avgServiceTime(OpenClazz... serviceTimes) {
      double lambda = effectiveLambda(serviceTimes);
      double ret = 0D;
      for (OpenClazz o : serviceTimes)
         ret += o.getLambda() * o.getServiceTime();
      return lambda != 0 ? ret / lambda : 0;
   }


   public String toString() {
      double lambda = ((OpenClazz) (serviceTimes[0])).getLambda();
      double service = serviceTimes[0].getServiceTime();
      return "TotalLambda " + lambda + " MeanServiceTime " + service;
   }

}
