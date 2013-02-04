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

package Tas2.config.configs;


/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 30/11/12
 */
public class Tas2Configuration extends Configuration {

   private CubistConfig cubistConfig;
   private QueueFactoryConfig queueFactoryConfig;
   private ProbabilityFactoryConfig probabilityFactoryConfig;
   private OpenSolverConfig openSolverConfig;
   private IterativeCubistSolverConfig iterativeCubistSolverConfig;
   private ClosedSolverConfig closedSolverConfig;
   private SolverFactoryConfig solverFactoryConfig;
   private CorrectionConfiguration correctionConfiguration;
   private PhysicalConfiguration physicalConfiguration;

   public PhysicalConfiguration getPhysicalConfiguration() {
      return physicalConfiguration;
   }

   public void setPhysicalConfiguration(PhysicalConfiguration physicalConfiguration) {
      this.physicalConfiguration = physicalConfiguration;
   }

   public CorrectionConfiguration getCorrectionConfiguration() {
      return correctionConfiguration;
   }

   public void setCorrectionConfiguration(CorrectionConfiguration correctionConfiguration) {
      this.correctionConfiguration = correctionConfiguration;
   }

   public SolverFactoryConfig getSolverFactoryConfig() {
      return solverFactoryConfig;
   }

   public void setSolverFactoryConfig(SolverFactoryConfig solverFactoryConfig) {
      this.solverFactoryConfig = solverFactoryConfig;
   }

   public QueueFactoryConfig getQueueFactoryConfig() {
      return queueFactoryConfig;
   }

   public void setQueueFactoryConfig(QueueFactoryConfig queueFactoryConfig) {
      this.queueFactoryConfig = queueFactoryConfig;
   }

   public ProbabilityFactoryConfig getProbabilityFactoryConfig() {
      return probabilityFactoryConfig;
   }

   public void setProbabilityFactoryConfig(ProbabilityFactoryConfig probabilityFactoryConfig) {
      this.probabilityFactoryConfig = probabilityFactoryConfig;
   }

   public OpenSolverConfig getOpenSolverConfig() {
      return openSolverConfig;
   }

   public void setOpenSolverConfig(OpenSolverConfig openSolverConfig) {
      this.openSolverConfig = openSolverConfig;
   }

   public IterativeCubistSolverConfig getIterativeCubistSolverConfig() {
      return iterativeCubistSolverConfig;
   }

   public void setIterativeCubistSolverConfig(IterativeCubistSolverConfig iterativeCubistSolverConfig) {
      this.iterativeCubistSolverConfig = iterativeCubistSolverConfig;
   }

   public ClosedSolverConfig getClosedSolverConfig() {
      return closedSolverConfig;
   }

   public void setClosedSolverConfig(ClosedSolverConfig closedSolverConfig) {
      this.closedSolverConfig = closedSolverConfig;
   }

   public CubistConfig getCubistConfig() {
      return cubistConfig;
   }

   public void setCubistConfig(CubistConfig cubistConfig) {
      this.cubistConfig = cubistConfig;
   }
}
