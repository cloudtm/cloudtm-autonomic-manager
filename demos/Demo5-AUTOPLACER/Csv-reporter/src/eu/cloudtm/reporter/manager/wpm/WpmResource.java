package eu.cloudtm.reporter.manager.wpm;

import eu.cloudtm.reporter.manager.ResourceInfo;
import eu.cloudtm.reporter.manager.AbstractResource;
import org.infinispan.Cache;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Resource that obtains the data for a Infinispan instance from a WPM cache 
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class WpmResource extends AbstractResource {

   private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

   private final String memoryKey;// = "%s:MEMORY:0";
   private final String cpuKey;// = "%s:CPU:%s";
   private final String ispnKey;// = "%s:Infinispan Cache ( %s ):0";   

   private long cpuTimestamp = -1;
   private long memTimestamp = -1;
   private long ispnTimestamp = -1;
   private long tmpCpuTimestamp = -1;

   private final String[] ispnAttributesNames;

   private final String identifier;

   public WpmResource(ResourceInfo resourceInfo, String cacheName, String... ispnAttributesNames) {
      super(resourceInfo);
      this.identifier = resourceInfo.getAddress();
      this.memoryKey = identifier + ":MEMORY:0";
      this.cpuKey = identifier + ":CPU:";
      this.ispnKey = identifier + ":Infinispan Cache ( " + cacheName + " ):0";
      this.ispnAttributesNames = ispnAttributesNames;
   }

   public final void update(Cache<String, String> cache) {
      resetOldValues();
      updateCpu(cache);
      updateMemory(cache);
      updateIspnAttributes(cache);
      logCollectedData();
   }

   /*
   Put done! Id: 10.100.0.55:MEMORY:0
   Put done! Value: 1339439849551;MemoryInfo.free$$32110157824;MemoryInfo.used$$6837374976;
   */
   private void updateMemory(Cache<String, String> cache) {
      String rawData = getFromCache(cache, memoryKey);
      if (rawData == null) {
         logValueNotFound("Memory");
         return;
      }
      StringTokenizer tokenizer = getAttributes(rawData);
      if (tokenizer.countTokens() == 0 || !shouldUpdateMemory(tokenizer.nextToken())) {
         logTimestampOld("Memory");
         return;
      }

      //1) memory free
      String[] keyValue = getKeyAndValue(tokenizer.nextToken());
      if (keyValue != null) {
         updateMemoryFree(parseLong(keyValue[1]));
      }

      //2) memory usage
      keyValue = getKeyAndValue(tokenizer.nextToken());
      if (keyValue != null) {
         updateMemoryUsage(parseLong(keyValue[1]));
      }
   }

   /*
   Put done! Id: 10.100.0.55:CPU:0
   Put done! Value: 1339439850352;CpuPerc.sys$$0.0030303030303030303;CpuPerc.user$$0.00404040404040404;
   Put done! Id: 10.100.0.55:CPU:1
   Put done! Value: 1339439850352;CpuPerc.sys$$0.0;CpuPerc.user$$0.0010080645161290322;
   ...
   */
   private void updateCpu(Cache<String, String> cache) {
      int cpuCount = 0;
      int cpuIdx = -1;
      double sum = 0;

      while(true) {
         cpuIdx++;
         String rawData = getFromCache(cache, cpuKey + cpuIdx);
         if (rawData == null) {
            break;
         }
         StringTokenizer tokenizer = getAttributes(rawData);
         if (tokenizer.countTokens() == 0 || !shouldUpdateCpu(tokenizer.nextToken())) {
            logTimestampOld("CPU-" + cpuIdx);
            continue;
         }

         //1) CpuPerc.sys
         String[] keyValue = getKeyAndValue(tokenizer.nextToken());
         if (keyValue != null) {
            sum += parseDouble(keyValue[1]);
         }

         //2) CpuPerc.user
         keyValue = getKeyAndValue(tokenizer.nextToken());
         if (keyValue != null) {
            sum += parseDouble(keyValue[1]);
         }

         cpuCount++;
      }

      cpuTimestamp = Math.max(cpuTimestamp, tmpCpuTimestamp);

      if (cpuCount <= 0) {
         logTimestampOld("all CPUs");
         return;
      }

      updateCpuUsage(sum / cpuCount);
   }

   /*
   Put done! Id: 10.100.0.55:Infinispan Cache ( CloudTM ):0
   Put done! Value: 1339439585624;StateTransferInProgress$$false;JoinComplete$$true;Nu
    */
   private void updateIspnAttributes(Cache<String, String> cache) {
      String rawData = getFromCache(cache, ispnKey);
      if (rawData == null) {
         logValueNotFound("Infinispan");
         return;
      }
      StringTokenizer tokenizer = getAttributes(rawData);
      if (tokenizer.countTokens() == 0 || !shouldUpdateIspn(tokenizer.nextToken())) {
         logTimestampOld("Infinispan");
         return;
      }

      Set<String> attrs = new TreeSet<String>(Arrays.asList(ispnAttributesNames));

      while (tokenizer.hasMoreTokens() && !attrs.isEmpty()) {
         String[] keyValue = getKeyAndValue(tokenizer.nextToken());
         if (keyValue != null && attrs.remove(keyValue[0])) {
            updateAttribute(keyValue[0], parseNumber(keyValue[1]));
         }
      }
   }

   private String getFromCache(Cache<String, String> cache, String key) {
      try{
         return cache.get(key);
      } catch (Exception e) {
         return null;
      }
   }

   private StringTokenizer getAttributes(String rawData) {
      return new StringTokenizer(rawData, ";");
   }

   private Number parseNumber(String data) {
      try {
         return NUMBER_FORMAT.parse(data);
      } catch (Exception e) {
         return null;
      }
   }

   private long parseLong(String data) {
      Number number = parseNumber(data);
      return number == null ? UNKNOWN_LONG_ATTR : number.longValue();
   }

   private double parseDouble(String data) {
      Number number = parseNumber(data);
      return number == null ? UNKNOWN_DOUBLE_ATTR : number.doubleValue();
   }

   private String[] getKeyAndValue(String data) {
      String[] tmp = data.split("\\$\\$");
      if (tmp.length != 2) {
         return null;
      }
      return tmp;
   }

   private boolean shouldUpdateMemory(String rawData) {
      try{
         long newTimestamp = Long.parseLong(rawData);
         if (newTimestamp <= memTimestamp) {
            return false;
         }
         memTimestamp = newTimestamp;
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   private boolean shouldUpdateCpu(String rawData) {
      try{
         long newTimestamp = Long.parseLong(rawData);
         tmpCpuTimestamp = Math.max(tmpCpuTimestamp, newTimestamp);
         return newTimestamp > cpuTimestamp;
      } catch (Exception e) {
         return false;
      }
   }

   private boolean shouldUpdateIspn(String rawData) {
      try{
         long newTimestamp = Long.parseLong(rawData);
         if (newTimestamp <= ispnTimestamp) {
            return false;
         }
         ispnTimestamp = newTimestamp;
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   private void logValueNotFound(String where) {
      log.warn("[" + identifier + "] Don't update " + where + ". Value not found in cache");
   }

   private void logTimestampOld(String where) {
      log.warn("[" + identifier + "] Don't update " + where + ". Timestamp is too old");
   }

   @Override
   public String toString() {
      return "WpmResource{" +
            "memoryKey='" + memoryKey + '\'' +
            ", cpuKey='" + cpuKey + '\'' +
            ", ispnKey='" + ispnKey + '\'' +
            '}';
   }
}
