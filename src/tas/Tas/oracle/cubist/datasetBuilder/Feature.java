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
public class Feature {

   private Object name;
   private mode mod;

   public Feature(Object name, Feature.mode mod) {
      this.name = name;
      this.mod = mod;
   }

   public Object getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setName(features f) {
      this.name = f;
   }

   public Feature.mode getMod() {
      return mod;
   }

   public void setMod(Feature.mode mod) {
      this.mod = mod;
   }

   public enum mode {
      SUM, AVG, CUSTOM
   }

   public boolean isAvg() {
      return this.mod == mode.AVG;
   }

   public boolean isSum() {
      return this.mod == mode.SUM;
   }

   public boolean isCustom() {
      return this.mod == mode.CUSTOM;
   }


   public enum features {
      NODES, NODESQUARED, THROUGHPUT, MEX_SIZE, CPU_USAGE, NET_THROUGHPUT
   }
}
