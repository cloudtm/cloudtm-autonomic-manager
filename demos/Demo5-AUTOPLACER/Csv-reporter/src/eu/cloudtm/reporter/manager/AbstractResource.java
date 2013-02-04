package eu.cloudtm.reporter.manager;

import eu.cloudtm.reporter.logging.Log;
import eu.cloudtm.reporter.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The common code used by a Resource 
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public abstract class AbstractResource implements Resource {

   protected final Log log = LogFactory.getLog(getClass());

   private double cpuUsage;
   private long memUsage;
   private long memFree;

   private final Map<String, Object> ispnAttrValues;
   protected final ResourceInfo resourceInfo;

   protected AbstractResource(ResourceInfo resourceInfo) {
      ispnAttrValues = new HashMap<String, Object>();
      this.resourceInfo = resourceInfo;
      resetOldValues();
   }

   @Override
   public final double getCpuUsage() {
      return cpuUsage;
   }

   @Override
   public final long getMemoryUsage() {
      return memUsage;
   }

   @Override
   public final long getMemoryFree() {
      return memFree;
   }

   @Override
   public final double getDoubleIspnAttribute(String name) {
      Number number = getNumber(name);
      return number == null ? UNKNOWN_DOUBLE_ATTR : number.doubleValue();
   }

   @Override
   public final long getLongIspnAttribute(String name) {
      Number number = getNumber(name);
      return number == null ? UNKNOWN_LONG_ATTR : number.longValue();
   }

   @Override
   public int getIntIspnAttribute(String name) {
      Number number = getNumber(name);
      return number == null ? UNKNOWN_INT_ATTR : number.intValue();
   }

   @Override
   public String getStringIspnAttribute(String name) {
      Object obj = ispnAttrValues.get(name);
      return obj != null ? obj.toString() : UNKNOWN_STRING_ATTR;
   }

   protected final void resetOldValues() {
      cpuUsage = UNKNOWN_DOUBLE_ATTR;
      memUsage = UNKNOWN_LONG_ATTR;
      memFree = UNKNOWN_LONG_ATTR;
      ispnAttrValues.clear();
   }

   protected final void updateCpuUsage(double value) {
      cpuUsage = value;
   }

   protected final void updateMemoryUsage(long value) {
      memUsage = value;
   }

   protected final void updateMemoryFree(long value) {
      memFree = value;
   }

   protected final void updateAttribute(String attribute, Object value) {
      ispnAttrValues.put(attribute, value);
   }

   protected final void logCollectedData() {
      log.trace("[" + resourceInfo + "]: data collected: cpuUsage=" + cpuUsage + ", memUsage=" + memUsage +
                      ", memFree=" + memFree + ", ispnAttrValues=" + ispnAttrValues);
   }

   private Number getNumber(String name) {
      Object obj = ispnAttrValues.get(name);
      return obj != null && obj instanceof Number ? (Number) obj : null;
   }
}
