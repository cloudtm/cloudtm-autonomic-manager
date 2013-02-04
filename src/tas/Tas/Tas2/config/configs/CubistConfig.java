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

import java.util.ArrayList;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 30/10/12
 */
public class CubistConfig {

   private static final double NOT = -1;

   private double minThreads = NOT;
   private double maxThreads = NOT;
   private double stepThreads = NOT;
   private double minNodes = NOT;
   private double maxNodes = NOT;
   private double stepNodes = NOT;
   private String nodeSet = null;
   private String threadSet = null;
   private String warehouseSet = null;
   private double minReads = NOT;
   private double maxReads = NOT;
   private double minWrites = NOT;
   private double maxWrites = NOT;
   private double minWarehouses = NOT;
   private double maxWarehouses = NOT;
   private double stepWarehouses = NOT;
   private String pathToCubist;
   private String pathToOracle;
   private String pathToQueryData;
   private String pathToData;
   private String targetFeature;
   private int instances = 0;
   private int committee = 0;
   private double convergenceThreshold;


   private ClosedSolverConfig.SOLVING_METHOD solvingMethod;
   private RecursiveRttGeneratorConfig recursiveRttGeneratorConfig;
   private BinaryRttGeneratorConfig binaryRttGeneratorConfig;
   private IterativeRttGeneratorConfig iterativeRttGeneratorConfig;
   private MinimalErrorCubistSolverConfig minimalErrorCubistSolverConfig;

   public MinimalErrorCubistSolverConfig getMinimalErrorCubistSolverConfig() {
      return minimalErrorCubistSolverConfig;
   }

   public void setMinimalErrorCubistSolverConfig(MinimalErrorCubistSolverConfig minimalErrorCubistSolverConfig) {
      this.minimalErrorCubistSolverConfig = minimalErrorCubistSolverConfig;
   }


   public double getConvergenceThreshold() {
      return convergenceThreshold;
   }

   public void setConvergenceThreshold(double convergenceThreshold) {
      this.convergenceThreshold = convergenceThreshold;
   }

   public ClosedSolverConfig.SOLVING_METHOD getSolvingMethod() {
      return solvingMethod;
   }

   public void setSolvingMethod(String solvingMethod) {
      this.solvingMethod = ClosedSolverConfig.SOLVING_METHOD.valueOf(solvingMethod);
   }

   public RecursiveRttGeneratorConfig getRecursiveRttGeneratorConfig() {
      return recursiveRttGeneratorConfig;
   }

   public void setRecursiveRttGeneratorConfig(RecursiveRttGeneratorConfig recursiveRttGeneratorConfig) {
      this.recursiveRttGeneratorConfig = recursiveRttGeneratorConfig;
   }

   public BinaryRttGeneratorConfig getBinaryRttGeneratorConfig() {
      return binaryRttGeneratorConfig;
   }

   public void setBinaryRttGeneratorConfig(BinaryRttGeneratorConfig binaryRttGeneratorConfig) {
      this.binaryRttGeneratorConfig = binaryRttGeneratorConfig;
   }

   public IterativeRttGeneratorConfig getIterativeRttGeneratorConfig() {
      return iterativeRttGeneratorConfig;
   }

   public void setIterativeRttGeneratorConfig(IterativeRttGeneratorConfig iterativeRttGeneratorConfig) {
      this.iterativeRttGeneratorConfig = iterativeRttGeneratorConfig;
   }

   @Override
   public String toString() {
      return "CubistConfig{" +
              "minThreads=" + minThreads +
              ", maxThreads=" + maxThreads +
              ", minNodes=" + minNodes +
              ", maxNodes=" + maxNodes +
              ", minReads=" + minReads +
              ", maxReads=" + maxReads +
              ", minWrites=" + minWrites +
              ", maxWrites=" + maxWrites +
              ", minWarehouses=" + minWarehouses +
              ", maxWarehouses=" + maxWarehouses +
              ", pathToCubist='" + pathToCubist + '\'' +
              ", pathToOracle='" + pathToOracle + '\'' +
              ", pathToQueryData='" + pathToQueryData + '\'' +
              ", pathToData='" + pathToData + '\'' +
              ", targetFeature='" + targetFeature + '\'' +
              ", instances=" + instances +
              ", committee=" + committee +
              ", type=" + type +
              '}';
   }

   private TYPE type = TYPE.BYTE_NET_THROUGHPUT;

   public double getMinWarehouses() {
      return minWarehouses;
   }

   public void setMinWarehouses(double minWarehouses) {
      this.minWarehouses = minWarehouses;
   }

   public double getMaxWarehouses() {
      return maxWarehouses;
   }

   public void setMaxWarehouses(double maxWarehouses) {
      this.maxWarehouses = maxWarehouses;
   }

   public TYPE getType() {
      return type;
   }

   public void setType(TYPE type) {
      this.type = type;
   }

   public void setType(String s) {
      if (s.equals("BYTE_NET_THROUGHPUT"))
         this.type = TYPE.BYTE_NET_THROUGHPUT;
      else
         this.type = TYPE.LOAD_DEPENDENT;
   }

   public boolean isLoadDependent() {
      return this.type.equals(TYPE.LOAD_DEPENDENT);
   }

   public int getInstances() {
      return instances;
   }

   public void setInstances(int instances) {
      this.instances = instances;
   }

   public int getCommittee() {
      return committee;
   }

   public void setCommittee(int committee) {
      this.committee = committee;
   }

   public String getPathToOracle() {
      return pathToOracle;
   }

   public void setPathToOracle(String pathToOracle) {
      this.pathToOracle = pathToOracle;
   }

   public String getTargetFeature() {
      return targetFeature;
   }

   public void setTargetFeature(String targetFeature) {
      this.targetFeature = targetFeature;
   }

   public String getPathToQueryData() {
      return pathToQueryData;
   }

   public void setPathToQueryData(String pathToQueryData) {
      this.pathToQueryData = pathToQueryData;
   }

   public String getPathToCubist() {
      return pathToCubist;
   }

   public void setPathToCubist(String pathToCubist) {
      this.pathToCubist = pathToCubist;
   }


   public double getMinThreads() {
      return minThreads;
   }

   public void setMinThreads(double minThreads) {
      this.minThreads = minThreads;
   }

   public double getMaxThreads() {
      return maxThreads;
   }

   public void setMaxThreads(double maxThreads) {
      this.maxThreads = maxThreads;
   }

   public double getMinNodes() {
      return minNodes;
   }

   public void setMinNodes(double minNodes) {
      this.minNodes = minNodes;
   }

   public double getMaxNodes() {
      return maxNodes;
   }

   public double getStepThreads() {
      return stepThreads;
   }

   public void setStepThreads(double stepThreads) {
      this.stepThreads = stepThreads;
   }

   public double getStepNodes() {
      return stepNodes;
   }

   public void setStepNodes(double stepNodes) {
      this.stepNodes = stepNodes;
   }

   public String getNodeSet() {
      return nodeSet;
   }

   public void setNodeSet(String nodeSet) {
      this.nodeSet = nodeSet;
   }

   public String getThreadSet() {
      return threadSet;
   }

   public void setThreadSet(String threadSet) {
      this.threadSet = threadSet;
   }

   public void setMaxNodes(double maxNodes) {
      this.maxNodes = maxNodes;
   }

   public double getMinReads() {
      return minReads;
   }

   public void setMinReads(double minReads) {
      this.minReads = minReads;
   }

   public double getMaxReads() {
      return maxReads;
   }

   public void setMaxReads(double maxReads) {
      this.maxReads = maxReads;
   }

   public double getMinWrites() {
      return minWrites;
   }

   public void setMinWrites(double minWrites) {
      this.minWrites = minWrites;
   }

   public double getMaxWrites() {
      return maxWrites;
   }

   public void setMaxWrites(double maxWrites) {
      this.maxWrites = maxWrites;
   }

   public String getPathToData() {
      return pathToData;
   }

   public void setPathToData(String pathToData) {
      this.pathToData = pathToData;
   }

   public enum TYPE {
      LOAD_DEPENDENT, BYTE_NET_THROUGHPUT
   }

   public enum LISTING {
      LIST, INTERVAL
   }

   public double[] nodes() {
      if (minNodes != NOT)
         return nodeList();
      return nodeSet();
   }

   public double[] threads() {
      if (minThreads != NOT)
         return threadList();
      return threadSet();
   }

   public double[] warehouses() {
      if (minWarehouses != NOT)
         return warehouseList();
      return warehouseSet();
   }


   private double[] nodeList() {
      ArrayList<Double> list = new ArrayList<Double>();
      for (double d = minNodes; d <= maxNodes; d += stepNodes) {
         list.add(d);
      }

      double[] ret = new double[list.size()];
      int i = 0;
      for (Double d : list)
         ret[i++] = d;
      return ret;
   }

   private double[] threadList() {
      ArrayList<Double> list = new ArrayList<Double>();
      for (double d = minThreads; d <= maxThreads; d += stepThreads) {
         list.add(d);
      }
      double[] ret = new double[list.size()];
      int i = 0;
      for (Double d : list)
         ret[i++] = d;
      return ret;
   }

   private double[] warehouseList() {
      ArrayList<Double> list = new ArrayList<Double>();
      for (double d = minWarehouses; d <= maxWarehouses; d += stepWarehouses) {
         list.add(d);
      }

      double[] ret = new double[list.size()];
      int i = 0;
      for (Double d : list)
         ret[i++] = d;
      return ret;
   }

   private double[] threadSet() {
      return parseSet(this.threadSet);
   }

   private double[] nodeSet() {
      return parseSet(this.nodeSet);
   }

   private double[] warehouseSet() {
      return parseSet(this.warehouseSet);
   }

   public String getWarehouseSet() {
      return warehouseSet;
   }

   public void setWarehouseSet(String warehouseSet) {
      this.warehouseSet = warehouseSet;
   }

   public double getStepWarehouses() {
      return stepWarehouses;
   }

   public void setStepWarehouses(double stepWarehouses) {
      this.stepWarehouses = stepWarehouses;
   }

   private double[] parseSet(String set) {
      String[] temp = set.split(",");
      double[] ret = new double[temp.length];
      int i = 0;
      for (String s : temp)
         ret[i++] = Double.parseDouble(s);
      return ret;
   }


}

