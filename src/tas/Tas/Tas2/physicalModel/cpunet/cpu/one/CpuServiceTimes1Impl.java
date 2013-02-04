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

package Tas2.physicalModel.cpunet.cpu.one;

import Tas2.exception.Tas2Exception;
import Tas2.util.Tas2Util;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */
public class CpuServiceTimes1Impl implements CpuServiceTimes1 {

   private double readOnlyS;
   private double execNoContS;
   private double prepareS;
   private double commitUpdateTxS;
   private double commitReadOnlyTxS;
   private double coordFlushS;
   private double replayS;
   private double cohortFlushS;
   private double rollbackS;


   public void checkCorrectness() throws Tas2Exception {
      Tas2Util.checkPositive(readOnlyS, false, "readOnlyS");
      Tas2Util.checkPositive(execNoContS, false, "execNoContS");
      Tas2Util.checkPositive(prepareS, false, "prepareS");
      Tas2Util.checkPositive(commitUpdateTxS, false, "commitUpdateTxS");
      Tas2Util.checkPositive(replayS, false, "replayS");
      Tas2Util.checkPositive(coordFlushS, false, "cohortFlushS");
      Tas2Util.checkPositive(rollbackS, false, "rollbackS)");
      Tas2Util.checkPositive(commitReadOnlyTxS, false, "commitReadOnlyTxS");
      Tas2Util.debug(false, this.toString());
   }

   public double getCommitReadOnlyTxS() {
      return commitReadOnlyTxS;
   }

   public void setCommitReadOnlyTxS(double commitReadOnlyTxS) {
      this.commitReadOnlyTxS = commitReadOnlyTxS;
   }

   public double getReadOnlyS() {
      return readOnlyS;
   }

   public void setReadOnlyS(double readOnlyS) {
      this.readOnlyS = readOnlyS;
   }

   public double getExecNoContS() {
      return execNoContS;
   }

   public void setExecNoContS(double execNoContS) {
      this.execNoContS = execNoContS;
   }

   public double getPrepareS() {
      return prepareS;
   }

   public void setPrepareS(double prepareS) {
      this.prepareS = prepareS;
   }

   public double getCommitUpdateTxS() {
      return commitUpdateTxS;
   }

   public void setCommitUpdateTxS(double commitUpdateTxS) {
      this.commitUpdateTxS = commitUpdateTxS;
   }

   public double getCoordFlushS() {
      return coordFlushS;
   }

   public void setCoordFlushS(double coordFlushS) {
      this.coordFlushS = coordFlushS;
   }

   public double getReplayS() {
      return replayS;
   }

   public void setReplayS(double replayS) {
      this.replayS = replayS;
   }

   public double getCohortFlushS() {
      return cohortFlushS;
   }

   public void setCohortFlushS(double cohortFlushS) {
      this.cohortFlushS = cohortFlushS;
   }

   public double getRollbackS() {
      return rollbackS;
   }

   public void setRollbackS(double rollbackS) {
      this.rollbackS = rollbackS;
   }

   @Override
   public String toString() {
      return "CpuServiceTimes1Impl{" +
              "readOnlyS=" + readOnlyS +
              ", execNoContS=" + execNoContS +
              ", prepareS=" + prepareS +
              ", commitUpdateTxS=" + commitUpdateTxS +
              ", coordFlushS=" + coordFlushS +
              ", replayS=" + replayS +
              ", cohortFlushS=" + cohortFlushS +
              ", rollbackS=" + rollbackS +
              '}';
   }
}
