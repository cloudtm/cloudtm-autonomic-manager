package eu.cloudtm.reporter.collector;

import eu.cloudtm.reporter.MemoryUnit;

/**
 * Normal data collector
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class DefaultDataCollector extends DataCollector{

   public DefaultDataCollector(int sumSize, int avgSize, MemoryUnit memoryUnit) {
      super(sumSize, avgSize, memoryUnit);
   }


   public String[] getLine() {
      String[] data = new String[3 + sumSize + avgSize];

      int dataIdx = 0;

      putDoubleValue(dataIdx++, data, actual.cpuUsage.getValue());
      putDoubleValue(dataIdx++, data, memoryUnit.convert(actual.memoryUsage.getValue()));
      putDoubleValue(dataIdx++, data, memoryUnit.convert(actual.memoryFree.getValue()));

      for (int i = 0; i < sumSize; ++i) {
         putLongValue(dataIdx++, data, actual.sumAttrs[i].getValue());
      }
      for (int i = 0; i < avgSize; ++i) {
         putDoubleValue(dataIdx++, data, actual.avgAttrs[i].getValue());
      }

      actual = new Report();

      for (String s : data) {
         if (!s.equals("")) {
            return data;
         }
      }
      return new String[0];
   }

   private void putDoubleValue(int dataIdx, String[] data, double newValue) {
      if (newValue == -1) {
         data[dataIdx] = "";
         return ;
      }
      data[dataIdx] = Double.toString(newValue);
   }

   private void putLongValue(int dataIdx, String[] data, long newValue) {
      if (newValue == -1) {
         data[dataIdx] = "";
         return ;
      }
      data[dataIdx] = Long.toString(newValue);
   }
}
