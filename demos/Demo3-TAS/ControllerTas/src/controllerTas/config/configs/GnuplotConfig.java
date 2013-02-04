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
public class GnuplotConfig {

   private String pathToData = "gnuplot/data/";
   private String pathToScript ="gnuplot/script/";
   private String pathToPlot= "gnuplot/plots/";
   private boolean eraseOldPlotsOnStartup = false;
   private String exec = "gnuplot";
   private String gnuplotScript = "plot.p";

   public String getExec() {
      return exec;
   }

   public void setExec(String exec) {
      this.exec = exec;
   }

   public String getGnuplotScript() {
      return gnuplotScript;
   }

   public void setGnuplotScript(String gnuplotScript) {
      this.gnuplotScript = gnuplotScript;
   }

   public String getPathToData() {
      return pathToData;
   }

   public void setPathToData(String pathToData) {
      this.pathToData = pathToData;
   }

   public boolean isEraseOldPlotsOnStartup() {
      return eraseOldPlotsOnStartup;
   }

   public void setEraseOldPlotsOnStartup(String eraseOldPlotsOnStartup) {
      this.eraseOldPlotsOnStartup = Boolean.valueOf(eraseOldPlotsOnStartup);
   }

   public String getPathToScript() {
      return pathToScript;
   }

   public void setPathToScript(String pathToScript) {
      this.pathToScript = pathToScript;
   }

   public String getPathToPlot() {
      return pathToPlot;
   }

   public void setPathToPlot(String pathToPlot) {
      this.pathToPlot = pathToPlot;
   }
}
