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
public class TasControllerConfiguration {

   private PlatformConfig platformConfig;
   private ScaleConfig scaleConfig;
   private GnuplotConfig gnuplotConfig;
   private DemoTransitoryConfig demoTransitoryConfig;

   public DemoTransitoryConfig getDemoTransitoryConfig() {
      return demoTransitoryConfig;
   }

   public void setDemoTransitoryConfig(DemoTransitoryConfig demoTransitoryConfig) {
      this.demoTransitoryConfig = demoTransitoryConfig;
   }

   public GnuplotConfig getGnuplotConfig() {
      return gnuplotConfig;
   }

   public void setGnuplotConfig(GnuplotConfig gnuplotConfig) {
      this.gnuplotConfig = gnuplotConfig;
   }

   public PlatformConfig getPlatformConfig() {
      return platformConfig;
   }

   public void setPlatformConfig(PlatformConfig platformConfig) {
      this.platformConfig = platformConfig;
   }

   public ScaleConfig getScaleConfig() {
      return scaleConfig;
   }

   public void setScaleConfig(ScaleConfig scaleConfig) {
      this.scaleConfig = scaleConfig;
   }

   @Override
   public String toString() {
      return "TasControllerConfiguration{" +
              "platformConfig=" + platformConfig +
              ", scaleConfig=" + scaleConfig +
              '}';
   }
}
