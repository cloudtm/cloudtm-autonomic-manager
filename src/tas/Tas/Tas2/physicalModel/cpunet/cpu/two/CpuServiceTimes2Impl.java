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

package Tas2.physicalModel.cpunet.cpu.two;

import Tas2.exception.Tas2Exception;
import Tas2.util.Tas2Util;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 07/12/12
 */
public class CpuServiceTimes2Impl implements CpuServiceTimes2 {

   private double updateTxLocalExecutionS;
   private double updateTxPrepareS;
   private double updateTxCommitS;
   private double updateTxLocalLocalRollbackS;
   private double updateTxLocalRemoteRollbackS;

   private double updateTxRemoteExecutionS;
   private double updateTxRemoteCommitS;
   private double updateTxRemoteRollbackS;

   private double readOnlyTxLocalExecutionS;
   private double readOnlyTxPrepareS;
   private double readOnlyTxCommitS;


   public void checkCorrectness() throws Tas2Exception {
      Tas2Util.checkPositive(updateTxLocalExecutionS, false, "ExecNoCont");
      Tas2Util.checkPositive(updateTxPrepareS, false, "Prepare");
      Tas2Util.checkPositive(updateTxCommitS, false, "Commit");
      Tas2Util.checkPositive(updateTxLocalLocalRollbackS, false, "LocalLocalRollback");
      Tas2Util.checkPositive(updateTxLocalRemoteRollbackS, false, "LocalRemoteRollback");

      Tas2Util.checkPositive(updateTxRemoteExecutionS, false, "Replay");
      Tas2Util.checkPositive(updateTxRemoteCommitS, false, "Flush");
      Tas2Util.checkPositive(updateTxRemoteRollbackS, false, "RemoteRollback");

      Tas2Util.checkPositive(readOnlyTxLocalExecutionS, false, "ReadOnly");
      Tas2Util.checkPositive(readOnlyTxPrepareS, false, "RoPrepare");
      Tas2Util.checkPositive(readOnlyTxCommitS, false, "RoCommit");

   }

   public double getUpdateTxLocalExecutionS() {
      return updateTxLocalExecutionS;
   }

   public void setUpdateTxLocalExecutionS(double updateTxLocalExecutionS) {
      this.updateTxLocalExecutionS = updateTxLocalExecutionS;
   }

   public double getUpdateTxPrepareS() {
      return updateTxPrepareS;
   }

   public void setUpdateTxPrepareS(double updateTxPrepareS) {
      this.updateTxPrepareS = updateTxPrepareS;
   }

   public double getUpdateTxCommitS() {
      return updateTxCommitS;
   }

   public void setUpdateTxCommitS(double updateTxCommitS) {
      this.updateTxCommitS = updateTxCommitS;
   }

   public double getUpdateTxLocalLocalRollbackS() {
      return updateTxLocalLocalRollbackS;
   }

   public void setUpdateTxLocalLocalRollbackS(double updateTxLocalLocalRollbackS) {
      this.updateTxLocalLocalRollbackS = updateTxLocalLocalRollbackS;
   }

   public double getUpdateTxLocalRemoteRollbackS() {
      return updateTxLocalRemoteRollbackS;
   }

   public void setUpdateTxLocalRemoteRollbackS(double updateTxLocalRemoteRollbackS) {
      this.updateTxLocalRemoteRollbackS = updateTxLocalRemoteRollbackS;
   }

   public double getUpdateTxRemoteExecutionS() {
      return updateTxRemoteExecutionS;
   }

   public void setUpdateTxRemoteExecutionS(double updateTxRemoteExecutionS) {
      this.updateTxRemoteExecutionS = updateTxRemoteExecutionS;
   }

   public double getUpdateTxRemoteCommitS() {
      return updateTxRemoteCommitS;
   }

   public void setUpdateTxRemoteCommitS(double updateTxRemoteCommitS) {
      this.updateTxRemoteCommitS = updateTxRemoteCommitS;
   }

   public double getUpdateTxRemoteRollbackS() {
      return updateTxRemoteRollbackS;
   }

   public void setUpdateTxRemoteRollbackS(double updateTxRemoteRollbackS) {
      this.updateTxRemoteRollbackS = updateTxRemoteRollbackS;
   }

   public double getReadOnlyTxLocalExecutionS() {
      return readOnlyTxLocalExecutionS;
   }

   public void setReadOnlyTxLocalExecutionS(double readOnlyTxLocalExecutionS) {
      this.readOnlyTxLocalExecutionS = readOnlyTxLocalExecutionS;
   }

   public double getReadOnlyTxPrepareS() {
      return readOnlyTxPrepareS;
   }

   public void setReadOnlyTxPrepareS(double readOnlyTxPrepareS) {
      this.readOnlyTxPrepareS = readOnlyTxPrepareS;
   }

   public double getReadOnlyTxCommitS() {
      return readOnlyTxCommitS;
   }

   public void setReadOnlyTxCommitS(double readOnlyTxCommitS) {
      this.readOnlyTxCommitS = readOnlyTxCommitS;
   }


   @Override
   public String toString() {
      return "CpuServiceTimes2Impl{" +
              "updateTxLocalExecutionS=" + updateTxLocalExecutionS +
              ", updateTxPrepareS=" + updateTxPrepareS +
              ", updateTxCommitS=" + updateTxCommitS +
              ", updateTxLocalLocalRollbackS=" + updateTxLocalLocalRollbackS +
              ", updateTxLocalRemoteRollbackS=" + updateTxLocalRemoteRollbackS +
              ", updateTxRemoteExecutionS=" + updateTxRemoteExecutionS +
              ", updateTxRemoteCommitS=" + updateTxRemoteCommitS +
              ", updateTxRemoteRollbackS=" + updateTxRemoteRollbackS +
              ", readOnlyTxLocalExecutionS=" + readOnlyTxLocalExecutionS +
              ", readOnlyTxPrepareS=" + readOnlyTxPrepareS +
              ", readOnlyTxCommitS=" + readOnlyTxCommitS +
              '}';
   }


}
