package controllerTas.actions.gnuplot;       /*
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/02/13
 */
public class PlottableData {

   private String header; //Can be null
   private Set<PlottableDataLine> lines = new HashSet<PlottableDataLine>();
   private final static Log log = LogFactory.getLog(PlottableData.class);

   public PlottableData(String header, HashSet<PlottableDataLine> lines) {
      this.header = header;
      this.lines.addAll(lines);
   }

   public PlottableData(String header) {
      this.header = header;
   }

   public void setHeader(String header) {
      this.header = header;
   }

   public void setLines(SortedSet<PlottableDataLine> lines) {
      this.lines = lines;
   }

   public void addDataLine(PlottableDataLine line) {
      this.lines.add(line);
      log.trace(lines);
   }

   public String getHeader() {
      return header;
   }

   public Set<PlottableDataLine> getLines() {
      return lines;
   }
}
