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
 *         Date: 01/10/12
 */
public class Util {


   private static double[] facCache;

   public static void init(int max) {
      //System.out.println("Util: initing the factorial cache with max value = " + max);
      facCache = new double[max + 1];
      for (int i = 0; i < max + 1; i++) {
         facCache[i] = realFac(i);
      }
   }

   private static double realFac(int n) {
      if (n < 0)
         throw new RuntimeException("Factorial invoked on a negative number");
      if (n == 0 || n == 1)
         return 1;
      return n * fac(n - 1);
   }


   static double fac(int n) {
      try {
         return facCache[n];
      } catch (IndexOutOfBoundsException i) {
         return realFac(n);
      } catch (NullPointerException nu) {
         System.err.println("Remember to invoke Util.init() before invoking this code. This *MUST* be fixed");
         throw nu;
      }

   }

   static double pow(double a, double b) {
      return Math.pow(a, b);
   }
}
