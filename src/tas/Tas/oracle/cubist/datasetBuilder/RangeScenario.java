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
public class RangeScenario {

   private double numReads;
   private double numWrites;
   private double numMinNodes;
   private double numMaxNodes;
   private double numMinThreads;
   private double numMaxThreads;
   private double numWarehouses;


   public RangeScenario(double numReads, double numWrites, double numMinNodes, double numMaxNodes, double numMinThreads, double numMaxThreads) {
      this.numReads = numReads;
      this.numWrites = numWrites;
      this.numMinNodes = numMinNodes;
      this.numMaxNodes = numMaxNodes;
      this.numMinThreads = numMinThreads;
      this.numMaxThreads = numMaxThreads;
   }

   public RangeScenario(double numReads, double numWrites, double numWarehouses, double numMinNodes, double numMaxNodes, double numMinThreads, double numMaxThreads) {
      this.numReads = numReads;
      this.numWrites = numWrites;
      this.numMinNodes = numMinNodes;
      this.numMaxNodes = numMaxNodes;
      this.numMinThreads = numMinThreads;
      this.numMaxThreads = numMaxThreads;
      this.numWarehouses = numWarehouses;
   }

   public double getNumWarehouses() {
      return numWarehouses;
   }

   public void setNumWarehouses(double numWarehouses) {
      this.numWarehouses = numWarehouses;
   }

   public static boolean in(Scenario s, RangeScenario rs) {
      return sameWl(s, rs) && matchingDeploy(s, rs);
   }

   public static boolean in(Scenario s, RangeScenario[] rs) {
      for (RangeScenario r : rs) {
         //System.out.println(RangeScenario.class+" "+r);
         if (in(s, r)) {
            //System.out.println("in");
            return true;
         }
      }
      return false;
   }

   private static boolean sameWl(Scenario s, RangeScenario rs) {
      return s.getNumReads() == rs.getNumReads() && s.getNumWrites() == rs.getNumWrites()
              && (s.getNumWarehouses() == 0 || s.getNumWarehouses() == rs.getNumWarehouses());
   }


   private static boolean matchingDeploy(Scenario s, RangeScenario rs) {
      return in(s.getNumThreads(), rs.getNumMinThreads(), rs.getNumMaxThreads()) &&
              in(s.getNumNodes(), rs.getNumMinNodes(), rs.getNumMaxNodes());
   }

   private static boolean in(double d, double min, double max) {
      return d >= min && d <= max;
   }


   /*
  Getters and setters
   */

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

   public double getNumMinNodes() {
      return numMinNodes;
   }

   public void setNumMinNodes(double numMinNodes) {
      this.numMinNodes = numMinNodes;
   }

   public double getNumMaxNodes() {
      return numMaxNodes;
   }

   public void setNumMaxNodes(double numMaxNodes) {
      this.numMaxNodes = numMaxNodes;
   }

   public double getNumMinThreads() {
      return numMinThreads;
   }

   public void setNumMinThreads(double numMinThreads) {
      this.numMinThreads = numMinThreads;
   }

   public double getNumMaxThreads() {
      return numMaxThreads;
   }

   public void setNumMaxThreads(double numMaxThreads) {
      this.numMaxThreads = numMaxThreads;
   }

   @Override
   public String toString() {
      return "RangeScenario{" +
              "numReads=" + numReads +
              ", numWrites=" + numWrites +
              ", numMinNodes=" + numMinNodes +
              ", numMaxNodes=" + numMaxNodes +
              ", numMinThreads=" + numMinThreads +
              ", numMaxThreads=" + numMaxThreads +
              ", numWarehouses=" + numWarehouses +
              '}';
   }
}
