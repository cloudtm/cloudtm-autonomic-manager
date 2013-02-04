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

package oracle.cubist.datasetBuilder;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 29/10/12
 */
public class Scenario {

   private double numReads;
   private double numWrites;
   private double numNodes;
   private double numThreads;
   private double numWarehouses;

   public Scenario(double numReads, double numWrites, double numNodes, double numThreads, double numWarehouses) {
      this.numReads = numReads;
      this.numWrites = numWrites;
      this.numNodes = numNodes;
      this.numThreads = numThreads;
      this.numWarehouses = numWarehouses;
   }

   public Scenario(double numReads, double numWrites, double numNodes, double numThreads) {
      this.numReads = numReads;
      this.numWrites = numWrites;
      this.numNodes = numNodes;
      this.numThreads = numThreads;
   }

   public double getNumWarehouses() {
      return numWarehouses;
   }

   public void setNumWarehouses(double numWarehouses) {
      this.numWarehouses = numWarehouses;
   }

   public double getNumReads() {
      return numReads;
   }

   public void setNumReads(double numReads) {
      this.numReads = numReads;
   }

   public double getNumWrites() {
      return numWrites;
   }

   public void setNumWrites(double numWrites) {
      this.numWrites = numWrites;
   }

   public double getNumNodes() {
      return numNodes;
   }

   public void setNumNodes(double numNodes) {
      this.numNodes = numNodes;
   }

   public double getNumThreads() {
      return numThreads;
   }

   public void setNumThreads(double numThreads) {
      this.numThreads = numThreads;
   }

   @Override
   public String toString() {
      return "Scenario{" +
              "numReads=" + numReads +
              ", numWrites=" + numWrites +
              ", numNodes=" + numNodes +
              ", numThreads=" + numThreads +
              ", numWarehouses=" + numWarehouses +
              '}';
   }
}
