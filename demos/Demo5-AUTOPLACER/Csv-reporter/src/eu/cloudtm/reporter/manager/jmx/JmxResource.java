package eu.cloudtm.reporter.manager.jmx;

import eu.cloudtm.reporter.Util;
import eu.cloudtm.reporter.manager.AbstractResource;
import eu.cloudtm.reporter.manager.ResourceInfo;
import eu.cloudtm.reporter.manager.jmx.collector.JmxCollector;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Resource that uses a JMX connection to obtain the data from a Infinispan instance
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class JmxResource extends AbstractResource {

   private final MBeanServerConnection mBeanServerConnection;
   private final ObjectName memoryObjectName;
   private final ObjectName operatingSystemObjectName;
   private final ObjectName runtimeObjectName;
   private final List<JmxCollector> jmxCollectorsList;

   private long prevUpTime = 0;
   private long prevProcessCpuTime = 0;

   public JmxResource(ResourceInfo resourceInfo, String[] attributes, Properties configuration) throws Exception {
      super(resourceInfo);
      mBeanServerConnection = JmxConnection.connect(resourceInfo);

      String[] collectorClassArray = configuration.getProperty("reporter.resource.jmx.collectors", "").split(",");
      jmxCollectorsList = new ArrayList<JmxCollector>(collectorClassArray.length);

      for (String collectorClass : collectorClassArray) {
         JmxCollector jmxCollector = Util.loadJmxCollector(collectorClass);
         if (jmxCollector != null) {
            jmxCollector.setUp(configuration, mBeanServerConnection, resourceInfo, attributes);
            if (jmxCollector.hasObjectNames()) {
               jmxCollectorsList.add(jmxCollector);
            }
         }
      }

      //just to set the previous values
      updateCpu();

      this.memoryObjectName = new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME);
      this.operatingSystemObjectName = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
      this.runtimeObjectName = new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);
   }

   public final void updateAndReset() {
      Map<String, Object> values = new LinkedHashMap<String, Object>();
      try {
         resetOldValues();

         for (JmxCollector jmxCollector : jmxCollectorsList) {
            values.putAll(jmxCollector.updateAndReset(mBeanServerConnection));
         }

         updateMemory();
         updateCpu();
      } catch (Exception e) {
         log.warn("Exception while updating values: " + e.getMessage());
      }

      for (Map.Entry<String, Object> entry : values.entrySet()) {
         updateAttribute(entry.getKey(), entry.getValue());
      }

      logCollectedData();
   }

   private void updateMemory() {
      try {
         MemoryMXBean memoryMXBean = ManagementFactory.newPlatformMXBeanProxy(mBeanServerConnection,
                                                                              memoryObjectName.getCanonicalName(),
                                                                              MemoryMXBean.class);
         MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
         MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();

         long usage = heap.getUsed() + nonHeap.getUsed();
         long total = heap.getMax() + nonHeap.getMax();

         updateMemoryUsage(usage);
         updateMemoryFree(total - usage);
      } catch (Exception e) {
         log.warn("[" + resourceInfo + "]: Error updating memory: " + e.getMessage());
      }
   }

   private void updateCpu() {
      Number currentUpTimeN = getAsNumberAttribute(mBeanServerConnection, runtimeObjectName, "Uptime");
      Number currentProcessCpuTimeN = getAsNumberAttribute(mBeanServerConnection, operatingSystemObjectName, "ProcessCpuTime");
      Number nCPUsN = getAsNumberAttribute(mBeanServerConnection, operatingSystemObjectName, "AvailableProcessors");

      if (currentProcessCpuTimeN == null || currentUpTimeN == null || nCPUsN == null) {
         return;
      }

      int nCPUs = nCPUsN.intValue();
      long currentUpTime = currentUpTimeN.longValue();
      long currentProcessCpuTime = currentProcessCpuTimeN.longValue();

      if (prevUpTime > 0L && currentUpTime > prevUpTime) {
         // elapsedCpu is in ns and elapsedTime is in ms.
         long elapsedCpu = currentProcessCpuTime - prevProcessCpuTime;
         long elapsedTime = currentUpTime - prevUpTime;
         // cpuUsage could go higher than 100% because elapsedTime
         // and elapsedCpu are not fetched simultaneously. Limit to
         // 99% to avoid Plotter showing a scale from 0% to 200%.
         double cpuUsage = Math.min(100F, elapsedCpu / (elapsedTime * 10000F * nCPUs));
         updateCpuUsage(cpuUsage / 100);
      }
      this.prevUpTime = currentUpTime;
      this.prevProcessCpuTime = currentProcessCpuTime;
   }

   private Number getAsNumberAttribute(MBeanServerConnection mBeanServer, ObjectName component, String attr) {
      try {
         return (Number) mBeanServer.getAttribute(component, attr);
      } catch (Exception e) {
         log.warn("[" + resourceInfo + "]: Error updating attribute [" + attr + "]: " + e.getMessage());
      }
      return null;
   }

   @Override
   public String toString() {
      return "JmxResource{" +
            "jmxCollectorsList=" + jmxCollectorsList +
            "resourceInfo=" + resourceInfo +
            '}';
   }
}
