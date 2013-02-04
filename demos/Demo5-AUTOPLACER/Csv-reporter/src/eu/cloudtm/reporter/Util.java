package eu.cloudtm.reporter;

import eu.cloudtm.reporter.collector.DataCollector;
import eu.cloudtm.reporter.collector.DefaultDataCollector;
import eu.cloudtm.reporter.collector.SmoothAwareDataCollector;
import eu.cloudtm.reporter.customattributes.CustomAttribute;
import eu.cloudtm.reporter.manager.Resource;
import eu.cloudtm.reporter.manager.ResourceInfo;
import eu.cloudtm.reporter.manager.ResourceManager;
import eu.cloudtm.reporter.manager.jmx.collector.JmxCollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 4.0
 */
public class Util {

   public static ClassLoader[] getClassLoaders() {
      return new ClassLoader[] {Util.class.getClassLoader(), Thread.currentThread().getContextClassLoader()};
   }

   public static InputStream openResource(String filename) {
      InputStream ret = null;
      for (ClassLoader cl : getClassLoaders()) {
         if (ret != null) {
            break;
         }

         ret = cl.getResourceAsStream(filename);
      }
      return ret;
   }

   public static CustomAttribute loadCalculateAttribute(String className) {
      Object instance = loadClass(className);
      if (instance == null || !(instance instanceof CustomAttribute)) {
         return null;
      }

      return (CustomAttribute) instance;
   }

   public static JmxCollector loadJmxCollector(String className) {
      Object instance = loadClass(className);
      if (instance == null || !(instance instanceof JmxCollector)) {
         return null;
      }

      return (JmxCollector) instance;
   }

   public static ResourceManager loadResourceManager(String className) {
      Object instance = loadClass(className);
      if (instance == null || !(instance instanceof ResourceManager)) {
         return null;
      }

      return (ResourceManager) instance;
   }

   public static Object loadClass(String className) {
      for (ClassLoader cl : getClassLoaders()) {
         try {
            Class<?> clazz = cl.loadClass(className);
            return  clazz.newInstance();
         } catch (Exception e) {
            //no op
         }

      }
      System.err.println("Class " + className + " not found!");
      return null;
   }


   public static DataCollector constructDataCollector(int sumSize, int avgSize, MemoryUnit memoryUnit, double alpha,
                                                      List<Integer> smoothingIndexes) {
      if (smoothingIndexes.isEmpty()) {
         return new DefaultDataCollector(sumSize, avgSize, memoryUnit);
      } else {
         return new SmoothAwareDataCollector(sumSize, avgSize, memoryUnit, alpha, smoothingIndexes);
      }

   }

   @SuppressWarnings("ResultOfMethodCallIgnored")
   public static boolean checkAndCreateHeaders(String filename, List<CustomAttribute> customAttributes, String[] attributes) {
      File file = new File(filename);
      file.delete();
      try {
         file.createNewFile();
         BufferedWriter bw = getWriter(filename, false);
         bw.write("Observations,CPU,Memory.Usage,Memory.Free");

         for (String s : attributes) {
            bw.write(",");
            bw.write(s);
         }

         for (CustomAttribute ca : customAttributes) {
            for (String header : ca.getHeaders()) {
               bw.write(",");
               bw.write(header);
            }
         }

         bw.newLine();
         bw.close();
      } catch (Exception e) {
         System.err.println("Error creating file header: " + e.getMessage());
         return false;
      }
      return true;
   }

   public static BufferedWriter getWriter(String filename, boolean append) {
      try {
         return new BufferedWriter(new FileWriter(filename, append));
      } catch (IOException e) {
         System.err.println("Error getting writer. " + e.getMessage());
         return null;
      }
   }

   public static void updateDataCollector(DataCollector dataCollector, Resource resource, String[] attrSum, String[] attrAvg) {
      dataCollector.addCpu(resource.getCpuUsage());
      dataCollector.addMemoryFree(resource.getMemoryFree());
      dataCollector.addMemoryUsage(resource.getMemoryUsage());
      for (int i = 0; i < attrSum.length; ++i) {
         dataCollector.addSumAttr(i, resource.getLongIspnAttribute(attrSum[i]));
      }
      for (int i = 0; i < attrAvg.length; ++i) {
         dataCollector.addAvgAttr(i, resource.getDoubleIspnAttribute(attrAvg[i]));
      }
   }

   public static Collection<ResourceInfo> parseResourcesIps(Properties config) {
      String[] ips = config.getProperty("reporter.ips", "").split(",");
      Set<ResourceInfo> resourcesInfo = new HashSet<ResourceInfo>();
      for (String s : ips) {
         int port = 9998;
         String[] addressAndPort = s.split(":");
         if (addressAndPort.length == 2) {
            try {
               port = Integer.parseInt(addressAndPort[1]);
            } catch (Exception e) {
               //ignore
            }
         }
         resourcesInfo.add(new ResourceInfo(addressAndPort[0], port));
      }
      return resourcesInfo;
   }
}
