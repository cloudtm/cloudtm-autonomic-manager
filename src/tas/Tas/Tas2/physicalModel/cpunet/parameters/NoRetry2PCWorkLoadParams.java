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

package Tas2.physicalModel.cpunet.parameters;

import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.logicalModel.solver.probabilities.Probabilities;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 22/11/12
 */
public class NoRetry2PCWorkLoadParams implements WorkloadParams {

   private double nanoTxLambda;
   private double nanoTxNetLambda;
   private double wrTxPercentage;
   private double rdTxPercentage;
   private double numberOfNodes;
   private double messageSize;
   private double prepareProbability;
   private double commitProbability;
   private double localAbortProbability;
   private double wrPerTransaction;
   private double pDagger;
   private double coherProbability;
   private double coherProbabilityOneNodeLess;
   private double daggerReplicationOkOnOneNodeProbability;

   public double getpDagger() {
      return pDagger;
   }

   public void setpDagger(double pDagger) {
      this.pDagger = pDagger;
   }

   public double getCoherProbability() {
      return coherProbability;
   }

   public void setCoherProbability(double coherProbability) {
      this.coherProbability = coherProbability;
   }

   public double getCoherProbabilityOneNodeLess() {
      return coherProbabilityOneNodeLess;
   }


   public void setCoherProbabilityOneNodeLess(double coherProbabilityOneNodeLess) {
      this.coherProbabilityOneNodeLess = coherProbabilityOneNodeLess;
   }

   public NoRetry2PCWorkLoadParams(DSTMScenarioTas2 scenario, Probabilities probabilities) {

      nanoTxLambda = this.nanoTxLambda(scenario, probabilities);
      nanoTxNetLambda = this.nanoTxNetLambda(scenario, probabilities);
      wrTxPercentage = scenario.getWorkParams().getWritePercentage();
      rdTxPercentage = 1.0D - wrTxPercentage;
      numberOfNodes = scenario.getWorkParams().getNumNodes();
      messageSize = scenario.getWorkParams().getPrepareMessageSize();
      prepareProbability = this.prepareProbability(scenario, probabilities);
      commitProbability = this.commitProbability(scenario, probabilities);
      localAbortProbability = probabilities.getLocalAbortProbability();
      wrPerTransaction = scenario.getWorkParams().getWriteOpsPerTx();
      pDagger = probabilities.getDaggerProbability();
      daggerReplicationOkOnOneNodeProbability = this.daggerReplicationOkOnOneNodeProbability(scenario, probabilities);
      coherProbability = this.coherProbability(scenario, probabilities);
      coherProbabilityOneNodeLess = this.coherProbabilityOneNodeLess(scenario, probabilities);

   }

   private double coherProbabilityOneNodeLess(DSTMScenarioTas2 scenario, Probabilities probabilities) {
      double rap = probabilities.getRemoteAbortProbability();
      double nl = scenario.getWorkParams().getWriteOpsPerTx();
      double nodes = scenario.getWorkParams().getNumNodes() - 2;
      double coher = Math.pow(1.0D - rap, nl);
      coher = Math.pow(coher, nodes);
      return coher;
   }

   private double coherProbability(DSTMScenarioTas2 scenario, Probabilities probabilities) {
      double rap = probabilities.getRemoteAbortProbability();
      double nl = scenario.getWorkParams().getWriteOpsPerTx();
      double nodes = scenario.getWorkParams().getNumNodes() - 1;
      double coher = Math.pow(1.0D - rap, nl);
      coher = Math.pow(coher, nodes);
      return coher;
   }

   private double prepareProbability(DSTMScenarioTas2 scenario, Probabilities probabilities) {
      return Math.pow(1.0D - probabilities.getLocalAbortProbability(), scenario.getWorkParams().getWriteOpsPerTx());
   }

   private double daggerReplicationOkOnOneNodeProbability(DSTMScenarioTas2 scenario, Probabilities probabilities) {
      return Math.pow(1.0D - probabilities.getDaggerProbability(), scenario.getWorkParams().getWriteOpsPerTx());
   }

   private double commitProbability(DSTMScenarioTas2 scenario, Probabilities probabilities) {
      double coher = coherProbability(scenario, probabilities);
      return coher * prepareProbability(scenario, probabilities);
   }

   private double nanoTxLambda(DSTMScenarioTas2 scenario, Probabilities probabilities) {
      return scenario.getWorkParams().getLambda();
   }

   private double nanoTxNetLambda(DSTMScenarioTas2 scenario, Probabilities probabilities) {
      double nodes = scenario.getWorkParams().getNumNodes();
      double wrLambda = nanoTxLambda(scenario, probabilities) * scenario.getWorkParams().getWritePercentage();
      double localLambda = probabilities.getPrepareProbability() * wrLambda / nodes;
      double remoteLambda = localLambda * (nodes - 1);
      return localLambda + remoteLambda;
   }


   public double getNanoTxLambda() {
      return nanoTxLambda;
   }

   public void setNanoTxLambda(double nanoTxLambda) {
      this.nanoTxLambda = nanoTxLambda;
   }

   public double getNanoTxNetLambda() {
      return nanoTxNetLambda;
   }

   public void setNanoTxNetLambda(double nanoTxNetLambda) {
      this.nanoTxNetLambda = nanoTxNetLambda;
   }

   public double getWrTxPercentage() {
      return wrTxPercentage;
   }

   public void setWrTxPercentage(double wrTxPercentage) {
      this.wrTxPercentage = wrTxPercentage;
   }

   public double getRdTxPercentage() {
      return rdTxPercentage;
   }

   public void setRdTxPercentage(double rdTxPercentage) {
      this.rdTxPercentage = rdTxPercentage;
   }

   public double getNumberOfNodes() {
      return numberOfNodes;
   }

   public void setNumberOfNodes(double numberOfNodes) {
      this.numberOfNodes = numberOfNodes;
   }

   public double getMessageSize() {
      return messageSize;
   }

   public void setMessageSize(double messageSize) {
      this.messageSize = messageSize;
   }

   public double getPrepareProbability() {
      return prepareProbability;
   }

   public void setPrepareProbability(double prepareProbability) {
      this.prepareProbability = prepareProbability;
   }

   public double getCommitProbability() {
      return commitProbability;
   }

   public void setCommitProbability(double commitProbability) {
      this.commitProbability = commitProbability;
   }

   public double getLocalAbortProbability() {
      return localAbortProbability;
   }

   public void setLocalAbortProbability(double localAbortProbability) {
      this.localAbortProbability = localAbortProbability;
   }

   public double getWrPerTransaction() {
      return wrPerTransaction;
   }

   public void setWrPerTransaction(double wrPerTransaction) {
      this.wrPerTransaction = wrPerTransaction;
   }

   public double getPDagger() {
      return pDagger;
   }

   public void setPDagger(double pDagger) {
      this.pDagger = pDagger;
   }

   public double getDaggerReplicationOkOnOneNodeProbability() {
      return daggerReplicationOkOnOneNodeProbability;
   }

   public void setDaggerReplicationOkOnOneNodeProbability(double daggerReplicationOkOnOneNodeProbability) {
      this.daggerReplicationOkOnOneNodeProbability = daggerReplicationOkOnOneNodeProbability;
   }

   @Override
   public String toString() {
      return "NoRetry2PCWorkLoadParams{" +
              "nanoTxLambda=" + nanoTxLambda +
              ", nanoTxNetLambda=" + nanoTxNetLambda +
              ", wrTxPercentage=" + wrTxPercentage +
              ", rdTxPercentage=" + rdTxPercentage +
              ", numberOfNodes=" + numberOfNodes +
              ", messageSize=" + messageSize +
              ", prepareProbability=" + prepareProbability +
              ", commitProbability=" + commitProbability +
              ", localAbortProbability=" + localAbortProbability +
              ", wrPerTransaction=" + wrPerTransaction +
              ", pDagger=" + pDagger +
              ", coherProbability=" + coherProbability +
              ", coherProbabilityOneNodeLess=" + coherProbabilityOneNodeLess +
              ", daggerReplicationOkOnOneNodeProbability=" + daggerReplicationOkOnOneNodeProbability +
              '}';
   }
}
