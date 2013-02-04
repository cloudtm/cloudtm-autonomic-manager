package controllerTas.actions.gnuplot;    /*
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

import controllerTas.common.Scale;

import java.util.Arrays;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/02/13
 */
public class PlottableDataLine {

   private Scale scale;
   private double[] values;

   public PlottableDataLine(Scale scale, double... values) {
      this.scale = scale;
      this.values = values;
   }

   public Scale getScale() {
      return scale;
   }

   public void setScale(Scale scale) {
      this.scale = scale;
   }

   public double[] getValues() {
      return values;
   }

   public void setValues(double[] values) {
      this.values = values;
   }

   @Override
   public String toString() {
      return "PlottableDataLine{" +
              "scale=" + scale +
              ", values=" + Arrays.toString(values) +
              '}';
   }


   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      PlottableDataLine that = (PlottableDataLine) o;

      if (!scale.equals(that.scale)) return false;

      return true;
   }

   @Override
   public int hashCode() {
      int result = scale.hashCode();
      result = 31 * result + (values != null ? Arrays.hashCode(values) : 0);
      return result;
   }
}
