package org.radargun;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * implements a change workload request to radargun
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class WorkloadJmxRequest {

   private enum Option {
      WRITE_PERCENTAGE("-writePercentage", false, "setWriteTxPercentage", "int"),
      WRT_TX_WORKLOAD("-writeTxWorkload", false, "setWriteTxWorkload", "String"),
      RD_TX_WORKLOAD("-readTxWorkload", false, "setReadTxWorkload", "String"),
      VALUE_SIZE("-valueSize", false, "setSizeOfValue", "int"),
      NR_THREADS("-nrThreads", false, "setNumberOfThreads", "int"),
      NR_KEYS("-nrKeys", false, "setNumberOfKeys", "int"),
      CONTENTION("-contention", false, "setNoContention", "boolean"),
      STOP("-stop", true, "stopBenchmark", null),
      JMX_COMPONENT("-jmxComponent", false, null, null),
      JMX_HOSTNAME("-hostname", false, null, null),
      JMX_PORT("-port", false, null, null),
      HELP("-help", true, null, null);

      private final String argumentName;
      private final boolean isBoolean;
      private final String methodName;
      private final String[] types;

      Option(String argumentName, boolean isBoolean, String methodName, String type) {
         if (argumentName == null) {
            throw new IllegalArgumentException("Null not allowed");
         }
         this.methodName = methodName;
         this.types = new String[] {type};
         this.argumentName = argumentName;
         this.isBoolean = isBoolean;
      }

      public final String toString() {
         return argumentName;
      }

      public static Option fromString(String optionName) {
         for (Option option : values()) {
            if (option.argumentName.equalsIgnoreCase(optionName)) {
               return option;
            }
         }
         return null;
      }
   }

   private static final String COMPONENT_PREFIX = "org.radargun:stage=";
   private static final String DEFAULT_COMPONENT = "Benchmark";
   private static final String DEFAULT_JMX_PORT = "9998";

   private final ObjectName benchmarkComponent;
   private final MBeanServerConnection mBeanServerConnection;

   private final EnumMap<Option, Object> optionValues;

   public static void main(String[] args) throws Exception {
      Arguments arguments = new Arguments();
      arguments.parse(args);
      arguments.validate();

      System.out.println("Options are " + arguments.printOptions());
      
      if (arguments.hasOption(Option.HELP)) {
         System.out.println("Available options are:");
         for (Option option : Option.values()) {
            System.out.println("  " + option.argumentName + (option.isBoolean ? "" : " <value>"));
         }
         System.out.println();
         System.exit(0);
      }

      WorkloadJmxRequest request = new WorkloadJmxRequest(arguments.getValue(Option.JMX_COMPONENT),
                                                          arguments.getValue(Option.JMX_HOSTNAME),
                                                          arguments.getValue(Option.JMX_PORT));

      for (Option option : Option.values()) {
         if (!arguments.hasOption(option)) {
            continue;
         }
         switch (option) {
            case STOP:
               request.setStop(arguments.getValueAsBoolean(option));
               break;
            case CONTENTION:
               request.setContention(arguments.getValueAsBoolean(option));
               break;
            case NR_KEYS:
               request.setNumberOfKeys(arguments.getValueAsInt(option));
               break;
            case NR_THREADS:
               request.setNumberOfThreads(arguments.getValueAsInt(option));
               break;
            case RD_TX_WORKLOAD:
               request.setReadTxWorkload(arguments.getValue(option));
               break;
            case VALUE_SIZE:
               request.setValueSize(arguments.getValueAsInt(option));
               break;
            case WRITE_PERCENTAGE:
               request.setWriteTxPercentage(arguments.getValueAsInt(option));
               break;
            case WRT_TX_WORKLOAD:
               request.setWriteTxWorkload(arguments.getValue(option));
               break;
            default:
               //no-op
         }
      }

      request.doRequest();

   }

   private WorkloadJmxRequest(String component, String hostname, String port) throws Exception {
      String connectionUrl = "service:jmx:rmi:///jndi/rmi://" + hostname + ":" + port + "/jmxrmi";

      JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(connectionUrl));
      mBeanServerConnection = connector.getMBeanServerConnection();
      benchmarkComponent = new ObjectName(COMPONENT_PREFIX + component);
      optionValues = new EnumMap<Option, Object>(Option.class);
   }

   public void setWriteTxPercentage(int value) {
      if (value >= 0 && value <= 100) {
         optionValues.put(Option.WRITE_PERCENTAGE, value);
      }
   }

   public void setWriteTxWorkload(String value) {
      if (value == null || value.isEmpty()) {
         return;
      }
      optionValues.put(Option.WRT_TX_WORKLOAD, value);
   }

   public void setReadTxWorkload(String value) {
      if (value == null || value.isEmpty()) {
         return;
      }
      optionValues.put(Option.RD_TX_WORKLOAD, value);
   }

   public void setValueSize(int value) {
      if (value > 0) {
         optionValues.put(Option.VALUE_SIZE, value);
      }
   }

   public void setNumberOfThreads(int value) {
      if (value > 0) {
         optionValues.put(Option.NR_THREADS, value);
      }
   }

   public void setNumberOfKeys(int value) {
      if (value > 0) {
         optionValues.put(Option.NR_KEYS, value);
      }
   }

   public void setContention(boolean value) {
      optionValues.put(Option.CONTENTION, value);
   }

   public void setStop(boolean value) {
      optionValues.put(Option.STOP, value);
   }

   public void doRequest() {
      if (benchmarkComponent == null) {
         throw new NullPointerException("Component does not exists");
      }

      for (Map.Entry<Option, Object> entry : optionValues.entrySet()) {
         Option option = entry.getKey();
         Object value = entry.getValue();
         invoke(option, value);
      }

      try {
         mBeanServerConnection.invoke(benchmarkComponent, "changeKeysWorkload", new Object[0], new String[0]);
      } catch (Exception e) {
         System.out.println("Failed to invoke changeKeysWorkload");
      }

      System.out.println("done!");
   }

   private void invoke(Option option, Object value) {
      System.out.println("Invoking " + option.methodName + " for " + option.argumentName + " with " + value);
      try {
         if (option.isBoolean) {
            mBeanServerConnection.invoke(benchmarkComponent, option.methodName, new Object[0], new String[0]);
         } else {
            mBeanServerConnection.invoke(benchmarkComponent, option.methodName, new Object[] {value}, option.types);
         }
      } catch (Exception e) {
         System.out.println("Failed to invoke " + option.argumentName);
      }
   }

   private static class Arguments {

      private final Map<Option, String> argsValues;

      private Arguments() {
         argsValues = new EnumMap<Option, String>(Option.class);

         //set the default values
         argsValues.put(Option.JMX_COMPONENT, DEFAULT_COMPONENT);
         argsValues.put(Option.JMX_PORT, DEFAULT_JMX_PORT);
      }

      public final void parse(String[] args) {
         int idx = 0;
         while (idx < args.length) {
            Option option = Option.fromString(args[idx]);
            if (option == null) {
               throw new IllegalArgumentException("unkown option: " + args[idx] + ". Possible options are: " +
                                                        Arrays.asList(Option.values()));
            }
            idx++;
            if (option.isBoolean) {
               argsValues.put(option, Boolean.toString(true));
               continue;
            }
            if (idx >= args.length) {
               throw new IllegalArgumentException("expected a value for option " + option);
            }
            argsValues.put(option, args[idx++]);
         }
      }

      public final void validate() {
         if (!hasOption(Option.JMX_HOSTNAME) && !hasOption(Option.HELP)) {
            throw new IllegalArgumentException("Option " + Option.JMX_HOSTNAME + " is required");
         }
      }

      public final String getValue(Option option) {
         return argsValues.get(option);
      }

      public final int getValueAsInt(Option option) {
         return Integer.parseInt(argsValues.get(option));
      }

      public final boolean getValueAsBoolean(Option option) {
         return Boolean.parseBoolean(argsValues.get(option));
      }

      public final boolean hasOption(Option option) {
         return argsValues.containsKey(option);
      }

      public final String printOptions() {
         return argsValues.toString();
      }

      @Override
      public final String toString() {
         return "Arguments{" +
               "argsValues=" + argsValues +
               '}';
      }
   }

}
