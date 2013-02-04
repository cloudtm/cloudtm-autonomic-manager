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

package Tas2.logicalModel.solver.probabilities;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */
public class Tas2Probabilities implements Probabilities {

   private double localAbortProbability;
   private double remoteAbortProbability;
   private double daggerProbability;

   private double prepareProbability;
   private double commitProbability;
   private double coherProbability;
   private double remoteCoherProbability;
   private double okOneNodeCommitDaggerProbability;


   public Tas2Probabilities() {


   }


   public Tas2Probabilities(double localAbortProbability, double remoteAbortProbability, double daggerProbability) {
      this.localAbortProbability = localAbortProbability;
      this.remoteAbortProbability = remoteAbortProbability;
      this.daggerProbability = daggerProbability;
   }

   public double getRemoteCoherProbability() {
      return remoteCoherProbability;
   }

   public void setRemoteCoherProbability(double remoteCoherProbability) {
      this.remoteCoherProbability = remoteCoherProbability;
   }

   public double getOkOneNodeCommitDaggerProbability() {
      return okOneNodeCommitDaggerProbability;
   }

   public void setOkOneNodeCommitDaggerProbability(double okOneNodeCommitDaggerProbability) {
      this.okOneNodeCommitDaggerProbability = okOneNodeCommitDaggerProbability;
   }

   public double getCoherProbability() {
      return coherProbability;
   }

   public void setCoherProbability(double coherProbability) {
      this.coherProbability = coherProbability;
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

   /*
   @Override
   public String toString() {
      return "Tas2Probabilities{" +
              "localAbortProbability=" + localAbortProbability +
              ", remoteAbortProbability=" + remoteAbortProbability +
              ", daggerProbability=" + daggerProbability +
              ", prepareProbability=" + prepareProbability +
              ", commitProbability=" + commitProbability +
              ", coherProbability=" + coherProbability +
              ", remoteCoherProbability=" + remoteCoherProbability +
              ", okOneNodeCommitDaggerProbability=" + okOneNodeCommitDaggerProbability +
              '}';
   }
   */

   /*
   @Override
   public String toString() {
      return "Tas2Probabilities{" +
              "dap=" + daggerProbability +
              ", lap=" + localAbortProbability +
              ", rap=" + remoteAbortProbability +
              '}';
   }
   */

   @Override
   public String toString() {
      return "Tas2Probabilities{" +
              "localAbortProbability=" + localAbortProbability +
              ", remoteAbortProbability=" + remoteAbortProbability +
              ", daggerProbability=" + daggerProbability +
              ", prepareProbability=" + prepareProbability +
              ", commitProbability=" + commitProbability +
              ", coherProbability=" + coherProbability +
              ", remoteCoherProbability=" + remoteCoherProbability +
              ", okOneNodeCommitDaggerProbability=" + okOneNodeCommitDaggerProbability +
              '}';
   }

   public double getLocalAbortProbability() {
      return localAbortProbability;
   }

   public void setLocalAbortProbability(double localAbortProbability) {
      this.localAbortProbability = localAbortProbability;
   }

   public double getRemoteAbortProbability() {
      return remoteAbortProbability;
   }

   public void setRemoteAbortProbability(double remoteAbortProbability) {
      this.remoteAbortProbability = remoteAbortProbability;
   }

   public double getDaggerProbability() {
      return daggerProbability;
   }

   public void setDaggerProbability(double daggerProbability) {
      this.daggerProbability = daggerProbability;
   }


}
