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


import open.exceptions.UnstableQueueException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/10/12
 */
public class LoadIndependentOpenMMK extends AbstractOpenMMK {


   private double p0;

   private final static boolean debug = false;
   private final static Log log = LogFactory.getLog(LoadIndependentOpenMMK.class);


   public LoadIndependentOpenMMK(double numServers, Clazz... serviceTimes) {
      super(numServers, serviceTimes);
   }

   @Override
   protected double __responseTime(int clazz) {
      return this.getClassServiceTime(clazz) + this.avgQueueingTime();
   }

   public double getResponseTimeByServiceTime(double service) {
      return service + this.avgQueueingTime();
   }

   @Override
   protected void flowInflowOut() throws UnstableQueueException {
      double servers = this.numServers;
      this.ro = ro(servers);
      this.p0 = this.computeP0(ro, servers);
   }


   public double getRo() {
      return this.ro;
   }


   @Override
   public double avgQueueingTime() {
      double pQ = this.avgQueueingProb(ro, p0, numServers);
      double lambda = this.effectiveLambda();
      return pQ / lambda * (ro / (1.0D - ro));
   }


   private double computeP0(double ro, double m) throws UnstableQueueException {
      double sum = (Util.pow(m * ro, m) / Util.fac((int) m)) * (1.0D / (1.0D - ro));

      for (double k = 0; k < m; k++) {
         sum += Util.pow(m * ro, k) / Util.fac((int) k);
      }

      return 1.0D / sum;
   }


   protected double ro(double numServers) throws UnstableQueueException {
      double ro = 0D;
      double classRo = 0;
      OpenClazz openClazz;
      for (Clazz c : serviceTimes) {
         openClazz = (OpenClazz) c;
         if (openClazz.getServiceTime() != 0) {
            classRo = this.ro(numServers, openClazz);
            log.trace("ID " + this.ID + " Class  = " + c.getClazz() + " L = " + openClazz.getLambda() + " S = " + openClazz.getServiceTime() + " Ro = " + classRo);
            if (classRo >= 1) {
               log.trace(this.ID + " unstable\n" + openClazz.toString(numServers));
               throw new UnstableQueueException(this.ID + " unstable\n" + openClazz.toString(numServers));
            }
            ro += classRo;
         }

      }
      if (ro >= 1) {
         log.trace(this.ID + " unstable as a whole. Ro is " + ro);
         throw new UnstableQueueException(this.ID + " unstable as a whole. Ro is " + ro);
      }
      return ro;
   }


   private double ro(double numServers, OpenClazz openClazz) {
      return openClazz.getLambda() * openClazz.getServiceTime() / numServers;
   }

   @Override
   public double utilization(int clazz) {
      OpenClazz o = (OpenClazz) this.serviceTimes[clazz];
      return this.ro(this.numServers, o);
   }

   private void debug(Object o) {
      if (debug)
         System.out.println(o);
   }

   @Override
   public double avgQueueingProb() {
      return this.avgQueueingProb(ro, p0, numServers);
   }


   private double avgQueueingProb(double ro, double p0, double m) {
      return (Util.pow(m * ro, m) / Util.fac((int) m)) * (p0 / (1.0D - ro));

   }


}
