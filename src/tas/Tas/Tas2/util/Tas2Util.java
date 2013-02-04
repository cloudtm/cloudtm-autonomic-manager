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

package Tas2.util;

import Tas2.exception.Tas2Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */
public class Tas2Util {

   private static final Log log = LogFactory.getLog(Tas2Util.class);

   public static double relErr(double a, double b) {
      if (a == 0) {
         if (b == 0) {
            return 0;
         }
         return 1;
      }
      return Math.abs(a - b) / a;
   }

   private static final boolean stop = false;

   public static void checkProb(double a, String parameter) throws Tas2Exception {
      try {
         checkBoundaries(a, 0, 1, false, false, parameter);
      } catch (Tas2Exception t) {
         throwException(parameter + " is not a probability. Its value is " + a);
      }
   }

   public static void checkPositive(double value, boolean strict, String param) throws Tas2Exception {
      checkGreaterThan(value, 0, strict, param);
   }

   public static void debug(String s) {
      log.trace(s);
   }

   public static void debug(boolean debug, String s) {
      if (debug)
         log.debug(s);
   }


   public static void checkBoundaries(double value, double min, double max, boolean strictMin, boolean stricMax, String param) throws Tas2Exception {
      checkSmallerThan(value, max, stricMax, param);
      checkGreaterThan(value, min, strictMin, param);
   }

   public static void checkSmallerThan(double value, double threshold, boolean strict, String param) throws Tas2Exception {
      if (value > threshold)
         throwException(param + " strictly greater than " + threshold + ". Actual value = " + value);
      if (strict && value == threshold)
         throwException(param + " equal to " + threshold);
   }

   public static void checkGreaterThan(double value, double threshold, boolean strict, String param) throws Tas2Exception {
      if (value < threshold)
         throwException(param + " strictly smaller than " + threshold);
      if (strict && value == threshold)
         throwException(param + " equal to " + threshold + ". Actual value = " + value);
   }


   private static void throwException(String s) throws Tas2Exception {
      if (stop) {
         log.fatal("Stopping because of " + s);
         throw new RuntimeException(s);
      }
      log.warn(s);
      throw new Tas2Exception(s);
   }


   /**
    *
    */

   public static double meanAbortedExecutionTime(double totalExecutionTime, int totalOps, double granuleAbortProb) {
      double meanExec = 0D;
      for (int i = 1; i <= totalOps; i++) {
         meanExec += binomialProbIthTrial(granuleAbortProb, i) * executionTillIthOperation(totalExecutionTime, totalOps, i);
      }
      return meanExec;
   }


   public static double executionTillIthOperation(double totalExecutionTime, int totalOps, int ithOp) {
      double quantum = totalExecutionTime / totalOps;
      return ithOp * quantum;
   }

   public static double binomialProbIthTrial(double p, int currentTrial) {
      return Math.pow(1.0D - p, currentTrial - 1) * p;
   }
}

