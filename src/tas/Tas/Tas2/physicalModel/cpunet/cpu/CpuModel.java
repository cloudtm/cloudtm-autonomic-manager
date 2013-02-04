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

package Tas2.physicalModel.cpunet.cpu;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 07/12/12
 */
public abstract class CpuModel {

   protected final double meanAbortedExecutionTime(double totalExecutionTime, int totalOps, double granuleAbortProb) {
      double meanExec = 0D;
      for (int i = 1; i <= totalOps; i++) {
         meanExec += binomialProbIthTrial(granuleAbortProb, i) * executionTillIthOperation(totalExecutionTime, totalOps, i);
      }
      return meanExec;
   }

   protected final double executionTillIthOperation(double totalExecutionTime, int totalOps, int ithOp) {
      double quantum = totalExecutionTime / totalOps;
      return ithOp * quantum;
   }

   protected final double binomialProbIthTrial(double p, int currentTrial) {
      return Math.pow(1.0D - p, currentTrial - 1) * p;
   }
}
