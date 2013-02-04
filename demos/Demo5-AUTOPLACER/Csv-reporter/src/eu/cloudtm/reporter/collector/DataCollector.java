package eu.cloudtm.reporter.collector;

import eu.cloudtm.reporter.MemoryUnit;

/**
 * Collects the data from all resources and calculates the averages or the sums between them. It returns the data in 
 * an array where which position corresponds to an entry in a csv line
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public abstract class DataCollector {

   protected Report actual;

   protected final MemoryUnit memoryUnit;
   protected final int sumSize;
   protected final int avgSize;   

   public DataCollector(int sumSize, int avgSize, MemoryUnit memoryUnit) {
      this.sumSize = sumSize;
      this.avgSize = avgSize;
      this.memoryUnit = memoryUnit;
      this.actual = new Report();
   }

   public void addCpu(double value) {
      actual.cpuUsage.add(value);
   }

   public void addMemoryUsage(double value) {
      actual.memoryUsage.add(value);
   }

   public void addMemoryFree(double value) {
      actual.memoryFree.add(value);
   }

   public void addSumAttr(int idx, long value) {
      actual.sumAttrs[idx].add(value);
   }

   public void addAvgAttr(int idx, double value) {
      actual.avgAttrs[idx].add(value);
   }

   public abstract String[] getLine();


   protected class Report {
      public final Average cpuUsage = new Average();
      public final Average memoryUsage = new Average();
      public final Average memoryFree = new Average();

      public final Sum[] sumAttrs = new Sum[sumSize];
      public final Average[] avgAttrs = new Average[avgSize];

      public Report() {
         for (int i = 0 ; i < sumSize; ++i) {
            sumAttrs[i] = new Sum();
         }
         for (int i = 0 ; i < avgSize; ++i) {
            avgAttrs[i] = new Average();
         }
      }
   }

   protected class Sum {
      private long value = 0;
      private boolean updated = false;

      public void add(long newValue) {
         if (newValue == -1) {
            return;
         }
         updated = true;
         value += newValue;
      }

      public long getValue() {
         return updated ? value : -1;
      }
   }

   protected class Average {
      private double value = 0;
      private int counter = 0;

      public void add(double newValue) {
         if (newValue == -1) {
            return;
         }
         counter++;
         value += newValue;
      }

      public double getValue() {
         return counter > 0 ? value / counter : -1;
      }
   }
}
