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
 *         Date: 09/11/12
 */
public class YuLoadIndependentOpenMMK extends LoadIndependentOpenMMK {


   protected double __responseTime(int clazz) {
      double service = this.getClassServiceTime(clazz);
      double y = yuY();

      return service * y;
   }

   private double yuA() {
      double ro = this.ro;
      double k = this.numServers;
      return Math.pow((k * ro), k) / ((1.0D - ro) * Util.fac((int) k));
   }

   private double yuB() {
      double sum = 0D;
      double k = this.numServers;
      double ro = this.ro;

      for (int i = 0; i < k; i++) {
         sum += Math.pow((k * ro), i) / Util.fac(i);
      }
      return sum;
   }

   public double getResponseTimeByServiceTime(double service) {
      double y = yuY();
      return service * y;
   }

   public double yuY() {
      double k = this.numServers;
      double a = yuA();
      double b = yuB();
      double ro = this.ro;
      double den = (k * (a + b) * (1.0D - ro));
      return 1 + (a / den);
   }


   public void forceUtilization(double r) {
      if (r < 1)
         this.ro = r;
   }

   public YuLoadIndependentOpenMMK(double numServers, Clazz... serviceTimes) {
      super(numServers, serviceTimes);    //To change body of overridden methods use File | Settings | File Templates.
   }


   public double debugA() {
      return yuA();
   }

   public double debugB() {
      return yuB();
   }

   @Override
   public double avgQueueingTime() {
      double k = this.numServers;
      double a = yuA();
      double b = yuB();
      double ro = this.ro;
      double den = (k * (a + b) * (1.0D - ro));
      return a / den;
   }
}
