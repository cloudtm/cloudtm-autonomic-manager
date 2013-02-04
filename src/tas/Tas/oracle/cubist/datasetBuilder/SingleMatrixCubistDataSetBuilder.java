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

package oracle.cubist.datasetBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 29/10/12
 */
public class SingleMatrixCubistDataSetBuilder extends CubistDatasetBuilder {
   private int offset = 5; //reads writes nodes threads  wareHouses
   private static final int N_READS = 0;
   private static final int N_THREADS = 2;
   private static final int N_NODES = 3;
   private static final int N_WRITES = 1;
   private static final int N_WARE = 4;

   private static final boolean orderedFeatures = true;

   private boolean filter = true;


   private double[][] values;
   private HashMap<String, Integer> featureToIndex;
   private LinkedList<featAndIndex> featToIndex;

   private String[] features;
   private String matrix;

   public SingleMatrixCubistDataSetBuilder(String[] features, String matrix) {
      this.features = features;
      this.matrix = matrix;
   }

   private void build() {
      String[][] wholeMatrix = null;
      try {
         wholeMatrix = wholeMatrix(matrix);
      } catch (IOException e) {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         System.exit(-1);
      }
      if (!orderedFeatures) {
         this.featureToIndex = featIndex(wholeMatrix[0], features);
         this.values = populateValues(wholeMatrix, featureToIndex);
      } else {
         //
         this.featToIndex = featToIndex(wholeMatrix[0], features);
         //
         this.values = orderedPopulateValues(wholeMatrix, featToIndex);
      }
   }

   private void debug(Object[][] strings) {
      for (int i = 0; i < strings.length; i++) {
         for (int j = 0; j < strings[0].length; j++) {
            System.out.print(strings[i][j] + "  ");
         }
         System.out.println("");
      }
   }

   private void debug(double[][] strings) {
      for (double[] string : strings) {
         for (int j = 0; j < strings[0].length; j++) {
            System.out.print(string[j] + "  ");
         }
         System.out.println("");
      }
   }

   private double[][] populateValues(String[][] strings, HashMap<String, Integer> feat) {
      int row = strings.length - 1;
      int col = feat.size() + offset;
      double[][] ret = new double[row][col];
      for (int i = 0, j = 0; i < row; i++, j = 0) {
         for (int k = 0; k < offset; k++) {
            ret[i][k] = parseDoubleFromCsv(strings[i + 1][k]);
         }
         for (int k : feat.values()) {
            ret[i][offset + (j++)] = parseDoubleFromCsv(strings[i + 1][k]);
         }
      }
      return ret;
   }

   private double[][] orderedPopulateValues(String[][] strings, LinkedList<featAndIndex> feat) {
      int row = strings.length - 1;
      int col = feat.size() + offset;
      double[][] ret = new double[row][col];
      for (int i = 0, j = 0; i < row; i++, j = 0) {
         for (int k = 0; k < offset; k++) {
            ret[i][k] = parseDoubleFromCsv(strings[i + 1][k]);
         }
         for (featAndIndex aFeat : feat) {
            ret[i][offset + (j++)] = parseDoubleFromCsv(strings[i + 1][aFeat.getIndex()]);
         }
      }
      return ret;
   }

   private String[][] wholeMatrix(String matrix) throws IOException {
      BufferedReader br = new BufferedReader(new FileReader(new File(matrix)));
      String line;
      ArrayList<String> lines = new ArrayList<String>();
      String head = br.readLine();
      lines.add(head);
      int numIndices = head.split(";").length;
      int numPoints = 0;
      while ((line = br.readLine()) != null) {
         if (!filterString(line, head)) {
            lines.add(line);
            numPoints++;
         }
      }
      String ret[][] = new String[numPoints + 1][numIndices];
      setRow(ret, head.split(";"), 0);
      numPoints = 0;

      for (String s : lines) {
         if (!okRow(s, numIndices)) {
            throw new RuntimeException("SingleMatrixCubistDataSetBuilder: Check the csv file! The number of elements in each row does not match the length of the header\n" +
                    "String " + s + "\nhas a length of " + (s.split(";").length) + "\nIndices are " + numIndices);

         }
         setRow(ret, s.split(";"), numPoints++);

      }
      return ret;
   }


   private boolean filterString(String s, String header) {

      return filter && (filterIfNecessary(s, header));
   }

   //If there are N columns in the header but N+k, k>0 in the "payload", then we get an exception!!!
   private boolean okRow(String s, int num) {
      return (s.split(";").length) == num;
   }

   private void setRow(String[][] target, String[] input, int row) {
      //System.out.println(Arrays.toString(input));
      int col = input.length;
      System.arraycopy(input, 0, target[row], 0, col);
   }

   private HashMap<String, Integer> featIndex(String[] head, String[] features) {
      HashMap<String, Integer> ret = new HashMap<String, Integer>();
      for (String s : features)
         ret.put(s, featureIndex(s, head));
      return ret;
   }

   private LinkedList<featAndIndex> featToIndex(String[] head, String[] features) {
      LinkedList<featAndIndex> ret = new LinkedList<featAndIndex>();
      for (String s : features)
         ret.addLast(new featAndIndex(s, featureIndex(s, head)));
      return ret;

   }

   private int featureIndex(String s, String[] ss) {
      int index = 0;
      for (String sss : ss) {
         if (s.equals(sss))
            return index;
         index++;
      }
      throw new RuntimeException(s + " not found");
   }

   @Override
   protected String __createDataset(RangeScenario rangeScenario[]) {
      build();
      StringBuilder sb = new StringBuilder();
      int feat = 0;
      if (!orderedFeatures)
         feat = featureToIndex.size();
      else
         feat = featToIndex.size();

      for (int i = 0; i < values.length; i++) {
         if (in(values, i, rangeScenario)) {
            sb.append(line(values, i, feat));

         }
      }
      return sb.toString();
   }

   private boolean in(double[][] values, int row, RangeScenario[] r) {
      //System.out.println(this.getClass()+ " "+extractScenario(values,row));
      return RangeScenario.in(extractScenario(values, row), r);
   }

   private Scenario extractScenario(double[][] values, int row) {
      double numR = values[row][N_READS];
      double numW = values[row][N_WRITES];
      double numT = values[row][N_THREADS];
      double numN = values[row][N_NODES];
      double numWh = values[row][N_WARE];
      return new Scenario(numR, numW, numN, numT, numWh);
   }


   private String line(double[][] values, int row, int feat) {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (int i = offset; i < feat + offset; i++) {
         if (first) {
            first = false;
         } else {
            sb.append(sep);
         }
         sb.append(values[row][i]);
      }
      sb.append("\n");
      return sb.toString();
   }


   private class featAndIndex {
      private String feature;
      private int index;

      private featAndIndex(String feature, int index) {
         this.feature = feature;
         this.index = index;
      }

      public String getFeature() {
         return feature;
      }

      public void setFeature(String feature) {
         this.feature = feature;
      }

      public int getIndex() {
         return index;
      }

      public void setIndex(int index) {
         this.index = index;
      }
   }

}
