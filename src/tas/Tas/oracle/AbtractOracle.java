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

package oracle;


import oracle.exceptions.OracleException;

import java.io.*;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 29/10/12
 */
public abstract class AbtractOracle implements Oracle {
   protected static final String CASES = ".cases";
   protected static final String DATA = ".data";
   protected static final String MODEL = ".model";
   protected String pathToCubist;
   protected String datasetFolder;
   private int committee = 0;   //now they're oracleWide
   private int instances = 0;

   public AbtractOracle(String pathToCubist) throws OracleException {
      if (checkFile(pathToCubist))
         this.pathToCubist = pathToCubist;
      else
         throw new OracleException(pathToCubist + " not found");
   }

   public AbtractOracle(String pathToCubist, int instances, int committee) throws OracleException {
      if (checkFile(pathToCubist))
         this.pathToCubist = pathToCubist;
      else
         throw new OracleException(pathToCubist + " not found");
      this.instances = instances;
      this.committee = committee;
   }

   protected final boolean checkFile(String s) {
      return new File(s).exists();
   }


   public synchronized void addPoint(String features, String target, boolean init) throws OracleException {
      File f = new File(slashedPath(datasetFolder) + target + DATA);
      if (f.exists() && init) {
         f.delete();
      }
      f = new File(slashedPath(datasetFolder) + target + DATA);
      try {
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
         out.println(features);
         out.flush();
         out.close();
      } catch (IOException e) {
         throw new OracleException(e.getMessage());
      }
   }

   public synchronized void removePoint(String features, String target) {
      throw new RuntimeException("removePoint method not supported yet");
   }


   protected String slashedPath(String s) {
      return s.endsWith("/") ? s : s.concat("/");

   }

   public void loadDataset(String name, Object pointer) throws OracleException {
      String slashedPath = slashedPath((String) pointer);
      if (checkFile(slashedPath + name + DATA)) {
         datasetFolder = slashedPath;
      } else {
         throw new OracleException("dataSet " + name + " creation impossible: check " + pointer);
      }
      //
   }

   public void loadDataset(String name, Object pointer, boolean createIfAbsent) throws OracleException {
      try {
         loadDataset(name, pointer);
      } catch (OracleException oe) {
         if (!createIfAbsent)
            throw oe;
         String slashedPath = slashedPath((String) pointer);
         System.out.println("Creating empty file " + (slashedPath + name + DATA));
         datasetFolder = slashedPath;
      }
   }


   public void loadModel(String name, Object pointer, boolean overWrite) {
      String slashedPath = slashedPath((String) pointer) + name + MODEL;
      boolean loadNew = !(checkFile(slashedPath) && !overWrite);
      if (loadNew) {
         createCubistModel(name);
      }
   }


   private String[] buildCommand(String filestem) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.pathToCubist);
      sb.append(";");
      sb.append("-f");
      sb.append(datasetFolder);
      sb.append(filestem);
      if (instances > 0) {
         sb.append(";");
         sb.append("-n");
         sb.append(instances);
      }
      if (committee > 0) {
         sb.append(";");
         sb.append("-C");
         sb.append(committee);
      }
      return sb.toString().split(";");

   }

   private void createCubistModel(String filestem) {
      /*String params = this.createParams(this.instances,this.committee);
      String fileStem = "-f" + datasetFolders.get(filestem) + filestem + params;
      String command[] = {pathToCubist, fileStem};
      */
      try {
         //System.out.println("Invoking "+ Arrays.toString(buildCommand(filestem)));
         Process p = Runtime.getRuntime().exec(buildCommand(filestem));
         checkForError(p);
         p.destroy();
      } catch (Exception e) {
         e.printStackTrace();
         throw new RuntimeException("createCubistModel in mona " + e.getMessage());
      }
   }

   protected void checkForError(Process p) throws OracleException {
      BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      String read;

      StringBuilder errorString = null;
      try {
         while ((read = stderr.readLine()) != null) {
            if (errorString == null)
               errorString = new StringBuilder();
            errorString.append(read);
            System.out.println(read);
         }
      } catch (IOException e) {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         throw new OracleException(e.getMessage());
      }
      if (errorString != null)
         throw new OracleException((read));
   }


}
