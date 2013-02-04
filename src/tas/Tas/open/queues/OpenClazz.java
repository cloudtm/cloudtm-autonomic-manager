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
 *         Date: 02/10/12
 */
public class OpenClazz extends Clazz {

   private double lambda;

   public double getLambda() {
      return lambda;
   }

   public OpenClazz(int clazz) {
      super(clazz);
   }

   public OpenClazz(int clazz, double serviceTime, double lambda) {
      super(clazz, serviceTime);
      this.lambda = lambda;
   }


   public void setLambda(double lambda) {
      this.lambda = lambda;
   }

   @Override
   public String toString() {
      return "OpenClazz{" +
              "class=" + clazz +
              " lambda=" + lambda +
              " serviceTime=" + serviceTime +
              " l/m=" + (lambda * serviceTime) +
              '}';
   }

   public String toString(double servers) {
      return "OpenClazz{" +
              "class=" + clazz +
              " lambda=" + lambda +
              " serviceTime=" + serviceTime +
              " l/m=" + (lambda * serviceTime) / servers +
              '}';
   }


}
