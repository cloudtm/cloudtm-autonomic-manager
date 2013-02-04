package controllerTas.config.configs;/*
 * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
 * Copyright 2013 INESC-ID and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/02/13
 */
public class ScaleConfig {

   private int minNumNodes = 2;
   private int maxNumNodes = 10;
   private int minNumThreads = 2;
   private int maxNumThreads = 8;
   private int initNumNodes = 2;
   private int initNumThreads = 2;

   public int getInitNumNodes() {
      return initNumNodes;
   }

   public void setInitNumNodes(int initNumNodes) {
      this.initNumNodes = initNumNodes;
   }

   public int getInitNumThreads() {
      return initNumThreads;
   }

   public void setInitNumThreads(int initNumThreads) {
      this.initNumThreads = initNumThreads;
   }

   public int getMinNumNodes() {
      return minNumNodes;
   }

   public void setMinNumNodes(int minNumNodes) {
      this.minNumNodes = minNumNodes;
   }

   public int getMaxNumNodes() {
      return maxNumNodes;
   }

   public void setMaxNumNodes(int maxNumNodes) {
      this.maxNumNodes = maxNumNodes;
   }

   public int getMinNumThreads() {
      return minNumThreads;
   }

   public void setMinNumThreads(int minNumThreads) {
      this.minNumThreads = minNumThreads;
   }

   public int getMaxNumThreads() {
      return maxNumThreads;
   }

   public void setMaxNumThreads(int maxNumThreads) {
      this.maxNumThreads = maxNumThreads;
   }

   @Override
   public String toString() {
      return "ScaleConfig{" +
              "minNumNodes=" + minNumNodes +
              ", maxNumNodes=" + maxNumNodes +
              ", minNumThreads=" + minNumThreads +
              ", maxNumThreads=" + maxNumThreads +
              ", initNumNodes=" + initNumNodes +
              ", initNumThreads=" + initNumThreads +
              '}';
   }


}
