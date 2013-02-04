package eu.cloudtm.reporter;

import eu.cloudtm.reporter.collector.DataCollector;
import eu.cloudtm.reporter.customattributes.CustomAttribute;
import eu.cloudtm.reporter.logging.Log;
import eu.cloudtm.reporter.logging.LogFactory;
import eu.cloudtm.reporter.manager.Resource;
import eu.cloudtm.reporter.manager.ResourceInfo;
import eu.cloudtm.reporter.manager.ResourceManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static eu.cloudtm.reporter.Util.*;

/**
 * Main class that has the application logic: collect the data from all Infinispan instances and write it in a 
 * CSV file
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class CsvReporter {

   private static final Log log = LogFactory.getLog(CsvReporter.class);

   @SuppressWarnings("InfiniteLoopStatement")
   public static void main(String[] args) throws IOException, InterruptedException {
      log.info("Starting Csv Reporter, v1.2");
      String configFile = "config.properties";
      if (args.length > 0) {
         configFile = args[0];
      }

      log.info("Trying to load configuration from %s", configFile);

      InputStream is = openResource(configFile);
      if (is == null) {
         log.error("Cannot open the following config file '%s'", configFile);
         System.exit(1);
      }

      Properties config = new Properties();
      config.load(is);

      String logLevel = config.getProperty("reporter.logging.level", "TRACE");
      String logFile = config.getProperty("reporter.logging.file", "csv-reporter.log");

      log.info("Initialize logging system with %s level and output file '%s'", logLevel, logFile);

      LogFactory.setLogLevel(logLevel);
      LogFactory.setOutputFile(logFile);

      log.info("Initializing Csv Reporter");

      long counter = 0;

      //reporter config
      int updateInterval = Integer.parseInt(config.getProperty("reporter.updateInterval", "5"));
      Collection<ResourceInfo> resourceInfos = parseResourcesIps(config);
      String destinationFilePath = config.getProperty("reporter.output_file", "/tmp/report.csv");

      //non-trivial attributes and memory
      String memoryUnitString = config.getProperty("reporter.memory_units", "MB");
      String[] customAttributesName = config.getProperty("reporter.custom_attr", "").split(",");

      //resource manager
      String resourceManager = config.getProperty("reporter.resource_manager",
                                                  "eu.cloudtm.reporter.manager.wpm.WpmResourceManager");

      //smoothing
      boolean smoothing = Boolean.parseBoolean(config.getProperty("reporter.smoothing.enable", "false"));
      double alpha = Double.parseDouble(config.getProperty("reporter.smoothing.alpha", "0.2"));
      String[] smoothedAttributes = config.getProperty("reporter.smoothing.attr", "").split(",");

      //infinispan config      
      String[] ispnSum = config.getProperty("reporter.ispn.sum_attr", "Throughput").split(",");
      String[] ispnAvg = config.getProperty("reporter.ispn.avg_attr", "AbortRate").split(",");

      List<String> allAttributesList = new LinkedList<String>();
      allAttributesList.addAll(Arrays.asList(ispnSum));
      allAttributesList.addAll(Arrays.asList(ispnAvg));

      List<CustomAttribute> customAttributes = new LinkedList<CustomAttribute>();

      log.info("Loading custom attributes...");

      for (String cAttr : customAttributesName) {
         CustomAttribute ca = loadCalculateAttribute(cAttr);
         if (ca != null) {
            customAttributes.add(ca);
         }
      }

      log.info("Attributes to be reported are %s", allAttributesList);

      String[] allAttributesArray = allAttributesList.toArray(new String[allAttributesList.size()]);

      if (!checkAndCreateHeaders(destinationFilePath, customAttributes, allAttributesArray)) {
         log.error("Cannot create or write to %s ", destinationFilePath);
         System.exit(1);
      }

      for (CustomAttribute ca : customAttributes) {
         allAttributesList.addAll(Arrays.asList(ca.getAttributes()));
      }

      log.info("Attributes to be gathered are %s", allAttributesList);

      allAttributesArray = allAttributesList.toArray(new String[allAttributesList.size()]);

      List<Integer> smoothingIndexes = new LinkedList<Integer>();
      if (smoothing) {
         for (String attr : smoothedAttributes) {
            if (attr.equalsIgnoreCase("cpuUsage")) {
               smoothingIndexes.add(1);
            } else if (attr.equalsIgnoreCase("memoryUsage")) {
               smoothingIndexes.add(2);
            } else if (attr.equalsIgnoreCase("memoryFree")) {
               smoothingIndexes.add(3);
            } else {
               for (int i = 0; i < allAttributesArray.length; ++i) {
                  if (attr.equalsIgnoreCase(allAttributesArray[i])) {
                     smoothingIndexes.add(i + 3); //we have CPU, memory usage and memory free in the first 3 indexes
                     break;
                  }
               }
            }
         }
      }

      log.info("Loading resource manager");

      ResourceManager manager = loadResourceManager(resourceManager);

      if (manager == null) {
         log.error("Resource manager not found!");
         System.exit(2);
      }
      manager.init(config, allAttributesArray);

      for (ResourceInfo ip : resourceInfos) {
         manager.addResource(ip);
      }

      Collection<Resource> resources = manager.resources();
      log.info("Resources registered are %s", manager.resources());

      if (resources.isEmpty()) {
         log.error("No resources found!");
         System.exit(3);
      }

      MemoryUnit memoryUnit = MemoryUnit.fromString(memoryUnitString);
      DataCollector dataCollector = constructDataCollector(ispnSum.length, ispnAvg.length, memoryUnit, alpha, smoothingIndexes);

      while (true) {
         Thread.sleep(updateInterval * 1000);
         counter += updateInterval;

         log.trace("=== UPDATE RESOURCES IN %s ===", counter);

         manager.collect();
         for (Resource resource : resources) {
            updateDataCollector(dataCollector, resource, ispnSum, ispnAvg);
            for (CustomAttribute ca : customAttributes) {
               ca.update(resource);
            }
         }

         String[] line = dataCollector.getLine();
         if (line.length == 0) {
            continue;
         }
         BufferedWriter bw =getWriter(destinationFilePath, true);
         bw.write(Long.toString(counter));
         for (String s : line) {
            bw.write(",");
            bw.write(s);
         }

         for (CustomAttribute ca : customAttributes) {
            for (String value : ca.getValues()) {
               bw.write(",");
               bw.write(value);
            }
         }
         bw.newLine();
         bw.close();
      }

   }


}
