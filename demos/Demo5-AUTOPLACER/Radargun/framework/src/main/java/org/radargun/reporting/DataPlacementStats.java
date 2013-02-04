package org.radargun.reporting;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class DataPlacementStats {

   private static final String GNUPLOT_SEPARATOR = "\t";
   private static final String GNUPLOT_COMMENT = "#";

   private final int roundId;
   private final int initialNumberOfKeys;
   private long creationTime;
   private int numberOfKeysMoved;
   private int wrongKeysMoved;
   private int wrongOwnersMoved;
   private int objectLookupSize;
   private int bloomFilterSize;
   private int machineLearnerSize;
   private int machineLearnerDeep;
   private long[] queryTime;

   public DataPlacementStats(int roundId, int initialNumberOfKeys) {
      this.roundId = roundId;
      this.initialNumberOfKeys = initialNumberOfKeys;
   }

   public void setCreationTime(long creationTime) {
      this.creationTime = creationTime;
   }

   public void setQueryTime(long[] queryTime) {
      this.queryTime = queryTime;
   }

   public void setNumberOfKeysMoved(int numberOfKeysMoved) {
      this.numberOfKeysMoved = numberOfKeysMoved;
   }

   public void setWrongKeysMoved(int wrongKeysMoved) {
      this.wrongKeysMoved = wrongKeysMoved;
   }

   public void setWrongOwnersMoved(int wrongOwnersMoved) {
      this.wrongOwnersMoved = wrongOwnersMoved;
   }

   public void setObjectLookupSize(int objectLookupSize) {
      this.objectLookupSize = objectLookupSize;
   }

   public void setBloomFilterSize(int bloomFilterSize) {
      this.bloomFilterSize = bloomFilterSize;
   }

   public void setMachineLearnerSize(int machineLearnerSize) {
      this.machineLearnerSize = machineLearnerSize;
   }

   public void setMachineLearnerDeep(int machineLearnerDeep) {
      this.machineLearnerDeep = machineLearnerDeep;
   }

   public static void writeHeader(BufferedWriter writer) throws IOException {
      writer.write(GNUPLOT_COMMENT);
      writer.write("RoundId");
      writer.write(GNUPLOT_SEPARATOR);
      writer.write("InitialNumberOfKeys");
      writer.write(GNUPLOT_SEPARATOR);
      writer.write("CreationTime(nanosec)");
      writer.write(GNUPLOT_SEPARATOR);
      writer.write("NumberOfKeysMoved");
      writer.write(GNUPLOT_SEPARATOR);
      writer.write("WrongKeysMoved");
      writer.write(GNUPLOT_SEPARATOR);
      writer.write("WrongOwnersMoved");
      writer.write(GNUPLOT_SEPARATOR);
      writer.write("ObjectLookupSize(byte)");
      writer.write(GNUPLOT_SEPARATOR);
      writer.write("BloomFilterSize(byte)");
      writer.write(GNUPLOT_SEPARATOR);
      writer.write("MachineLearnerSize(byte)");
      writer.write(GNUPLOT_SEPARATOR);
      writer.write("MachineLearnerDeep");
      writer.write(GNUPLOT_SEPARATOR);
      writer.write("QueriesTime(nanosec)");
      writer.newLine();
      writer.flush();
   }

   public void writeValues(BufferedWriter writer) throws IOException {
      writer.write(Integer.toString(roundId));
      writer.write(GNUPLOT_SEPARATOR);
      writer.write(Integer.toString(initialNumberOfKeys));
      writer.write(GNUPLOT_SEPARATOR);
      writer.write(Long.toString(creationTime));
      writer.write(GNUPLOT_SEPARATOR);
      writer.write(Integer.toString(numberOfKeysMoved));
      writer.write(GNUPLOT_SEPARATOR);
      writer.write(Integer.toString(wrongKeysMoved));
      writer.write(GNUPLOT_SEPARATOR);
      writer.write(Integer.toString(wrongOwnersMoved));
      writer.write(GNUPLOT_SEPARATOR);
      writer.write(Integer.toString(objectLookupSize));
      writer.write(GNUPLOT_SEPARATOR);
      writer.write(Integer.toString(bloomFilterSize));
      writer.write(GNUPLOT_SEPARATOR);
      writer.write(Integer.toString(machineLearnerSize));
      writer.write(GNUPLOT_SEPARATOR);
      writer.write(Integer.toString(machineLearnerDeep));
      writer.write(GNUPLOT_SEPARATOR);
      writer.write(Long.toString(sumQueryTimes()));
      for (long value : queryTime) {
         writer.write(GNUPLOT_SEPARATOR);
         writer.write(Long.toString(value));
      }
      writer.newLine();
      writer.flush();
   }

   private long sumQueryTimes() {
      long sum = 0;
      for (long value : queryTime) {
         sum += value;
      }
      return sum;
   }
}
