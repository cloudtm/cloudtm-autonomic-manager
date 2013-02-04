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
 *         Date: 02/12/12
 */
public class ClosedSolverConfig {

   private boolean debug;

   private double convergenceThreshold;
   private double recursionIteration;
   private boolean validateUnstable = true;
   private SOLVING_METHOD solvingMethod;
   private IterativeLambdaGeneratorConfig iterativeLambdaGeneratorConfig;
   private BinaryLambdaGeneratorConfig binaryLambdaGeneratorConfig;
   private RecursiveLambdaGeneratorConfig recursiveLambdaGeneratorConfig;


   private double cutCpuU;

   public double getCutCpuU() {
      return cutCpuU;
   }

   public void setCutCpuU(double cutCpuU) {
      this.cutCpuU = cutCpuU;
   }

   public boolean isValidateUnstable() {
      return validateUnstable;
   }

   public void setValidateUnstable(String validateUnstable) {
      this.validateUnstable = Boolean.valueOf(validateUnstable);
   }

   public boolean isDebug() {
      return debug;
   }

   public RecursiveLambdaGeneratorConfig getRecursiveLambdaGeneratorConfig() {
      return recursiveLambdaGeneratorConfig;
   }

   public void setRecursiveLambdaGeneratorConfig(RecursiveLambdaGeneratorConfig recursiveLambdaGeneratorConfig) {
      this.recursiveLambdaGeneratorConfig = recursiveLambdaGeneratorConfig;
   }

   public BinaryLambdaGeneratorConfig getBinaryLambdaGeneratorConfig() {
      return binaryLambdaGeneratorConfig;
   }

   public void setBinaryLambdaGeneratorConfig(BinaryLambdaGeneratorConfig binaryLambdaGeneratorConfig) {
      this.binaryLambdaGeneratorConfig = binaryLambdaGeneratorConfig;
   }

   public IterativeLambdaGeneratorConfig getIterativeLambdaGeneratorConfig() {
      return iterativeLambdaGeneratorConfig;
   }

   public void setIterativeLambdaGeneratorConfig(IterativeLambdaGeneratorConfig iterativeLambdGeneratorConfig) {
      this.iterativeLambdaGeneratorConfig = iterativeLambdGeneratorConfig;
   }

   public Boolean getDebug() {
      return debug;
   }

   public SOLVING_METHOD getSolvingMethod() {
      return solvingMethod;
   }

   public void setSolvingMethod(String solvingMethod) {
      this.solvingMethod = SOLVING_METHOD.valueOf(solvingMethod);
   }

   public void setDebug(String debug) {
      this.debug = Boolean.valueOf(debug);
   }

   public double getConvergenceThreshold() {
      return convergenceThreshold;
   }

   public void setConvergenceThreshold(double convergenceThreshold) {
      this.convergenceThreshold = convergenceThreshold;
   }

   public double getRecursionIteration() {
      return recursionIteration;
   }

   public void setRecursionIteration(double recursionIteration) {
      this.recursionIteration = recursionIteration;
   }


   public enum SOLVING_METHOD {
      PURE_RECURSION, ITERATIVE, BINARY_SEARCH, MIN_ERROR
   }
}
