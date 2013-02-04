package eu.cloudtm.reporter.collector;

import eu.cloudtm.reporter.MemoryUnit;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Instead of returning the data, it smooths it based on exponential smooth:
 * 
 *    alpha * newValue + (1 - alpha) * oldValue         
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class SmoothAwareDataCollector extends DataCollector{
   private final double alpha;
   private final Set<Integer> smoothAttrIdx;

   private double oldCPUUsage, oldMemoryUsage, oldMemoryFree;
   private double[] oldAvgAttributes;
   private long [] oldSumAttributes;

   public SmoothAwareDataCollector(int sumSize, int avgSize, MemoryUnit memoryUnit, double alpha, Collection<Integer> smoothAttributes) {
      super(sumSize, avgSize, memoryUnit);
      this.alpha = alpha;
      this.smoothAttrIdx = new TreeSet<Integer>(smoothAttributes);
      oldCPUUsage = oldMemoryUsage = oldMemoryFree = -1;
      oldAvgAttributes = new double[avgSize];
      oldSumAttributes = new long[sumSize];

      for (int i = 0; i < avgSize; ++i) {
         oldAvgAttributes[i] = -1;
      }
      for (int i = 0; i < sumSize; ++i) {
         oldSumAttributes[i] = -1;
      }
   }

   public String[] getLine() {
      String[] data = new String[3 + sumSize + avgSize];

      int dataIdx = 0;
      oldCPUUsage = putDoubleValue(dataIdx++, data, oldCPUUsage, actual.cpuUsage.getValue());
      oldMemoryUsage = putDoubleValue(dataIdx++, data, oldMemoryUsage, memoryUnit.convert(actual.memoryUsage.getValue()));
      oldMemoryFree = putDoubleValue(dataIdx++, data, oldMemoryFree, memoryUnit.convert(actual.memoryFree.getValue()));

      for (int i = 0; i < sumSize; ++i) {
         oldSumAttributes[i] = putLongValue(dataIdx++, data, oldSumAttributes[i], actual.sumAttrs[i].getValue());
      }
      for (int i = 0; i < avgSize; ++i) {
         oldAvgAttributes[i] = putDoubleValue(dataIdx++, data, oldAvgAttributes[i], actual.avgAttrs[i].getValue());
      }

      actual = new Report();

      for (String s : data) {
         if (!s.equals("")) {
            return data;
         }
      }
      return new String[0];
   }

   private double putDoubleValue(int dataIdx, String[] data, double oldValue, double newValue) {
      if (newValue == -1) {
         data[dataIdx] = "";
         return oldValue;
      }
      if (smoothAttrIdx.contains(dataIdx)) {
         double actualValue = oldValue == -1 ? newValue :
               (alpha * newValue + (1 - alpha) * oldValue);
         data[dataIdx] = Double.toString(actualValue);
         return actualValue;
      } else {
         data[dataIdx] = Double.toString(newValue);
         return newValue;
      }
   }

   private long putLongValue(int dataIdx, String[] data, long oldValue, long newValue) {
      if (newValue == -1) {
         data[dataIdx] = "";
         return oldValue;
      }
      if (smoothAttrIdx.contains(dataIdx)) {
         long actualValue = oldValue == -1 ? newValue :
               (long) (alpha * newValue + (1 - alpha) * oldValue);
         data[dataIdx] = Long.toString(actualValue);
         return actualValue;
      } else {
         data[dataIdx] = Long.toString(newValue);
         return newValue;
      }
   }
}
