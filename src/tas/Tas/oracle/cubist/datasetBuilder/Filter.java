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
 *         Date: 05/01/13
 */
public class Filter {

   private String feature;
   private double lowerBound;
   private double upperBound;

   public Filter(String feature, double lowerBound, double upperBound) {
      this.feature = feature;
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
   }

   public String getFeature() {
      return feature;
   }

   public void setFeature(String feature) {
      this.feature = feature;
   }

   public double getLowerBound() {
      return lowerBound;
   }

   public void setLowerBound(double lowerBound) {
      this.lowerBound = lowerBound;
   }

   public double getUpperBound() {
      return upperBound;
   }

   public void setUpperBound(double upperBound) {
      this.upperBound = upperBound;
   }

   public boolean accept(double d) {
      return d >= lowerBound && d <= upperBound;
   }
}
