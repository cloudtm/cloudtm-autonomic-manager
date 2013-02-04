package org.radargun;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class ProcessCsv {

   private enum Option {
      SUM("-sum", false),
      AVG("-avg", false),
      MAX("-min", false),
      MIN("-max", false),
      CSV("-csv", true),
      HELP("-help", true);

      private final String argumentName;
      private final boolean isBoolean;

      Option(String argumentName, boolean aBoolean) {
         isBoolean = aBoolean;
         if (argumentName == null) {
            throw new IllegalArgumentException("Null not allowed");
         }
         this.argumentName = argumentName;
      }

      public final String toString() {
         return argumentName;
      }

      public static Option fromString(String optionName) {
         if (optionName == null) {
            return null;
         }
         for (Option option : values()) {
            if (optionName.startsWith(option.argumentName)) {
               return option;
            }
         }
         return null;
      }
   }


   public static void main(String[] args) throws InterruptedException {
      Arguments arguments = new Arguments();
      arguments.parse(args);
      arguments.validate();

      if (arguments.hasOption(Option.HELP)) {
         StringBuilder stringBuilder = new StringBuilder("usage: java ")
               .append(ProcessCsv.class.getCanonicalName())
               .append(" -sum=attr1,...,attrN")
               .append(" -avg=attr1,...,attrN")
               .append(" -min=attr1,...,attrN")
               .append(" -max=attr1,...,attrN")
               .append(" file1 ... fileN");
         System.out.println(stringBuilder);
         System.exit(0);
      }

      System.out.println("Start processing CSV with options " + arguments.printOptions());

      List<CsvFileProcessor> processorList = new LinkedList<CsvFileProcessor>();

      Set<String> attributesName = new HashSet<String>();

      CsvHeaderResult[] headers = getCsvHeaders(arguments, attributesName);
      String[] attributesNamesArray = attributesName.toArray(new String[attributesName.size()]);

      for (String filePath : arguments.getFilePaths()) {
         processorList.add(new CsvFileProcessor(attributesNamesArray, filePath));
      }

      StringBuilder csvFormat = new StringBuilder();
      csvFormat.append("filePath");
      for (CsvHeaderResult header : headers) {
         csvFormat.append(",").append(header.getHeader());
      }
      csvFormat.append("\n");

      ExecutorService executor = Executors.newFixedThreadPool(4);
      List<Future<FileResult>> futureList = executor.invokeAll(processorList);

      for (Future<FileResult> future : futureList) {
         try {
            FileResult fileResult = future.get();
            printResults(fileResult, headers, csvFormat, arguments.hasOption(Option.CSV));
         } catch (ExecutionException e) {
            e.printStackTrace();
         }
      }
      executor.shutdown();

      if (arguments.hasOption(Option.CSV)) {
         System.out.println("== CSV OUTPUT ==");
         System.out.println(csvFormat);
         System.out.println("================");
      }

      System.exit(0);
   }

   private static CsvHeaderResult[] getCsvHeaders(Arguments arguments, Set<String> attributesName) {
      String[] sumArray = arguments.hasOption(Option.SUM) ? arguments.getValue(Option.SUM).split(",") : new String[0];
      String[] avgArray = arguments.hasOption(Option.AVG) ? arguments.getValue(Option.AVG).split(",") : new String[0];
      String[] minArray = arguments.hasOption(Option.MIN) ? arguments.getValue(Option.MIN).split(",") : new String[0];
      String[] maxArray = arguments.hasOption(Option.MAX) ? arguments.getValue(Option.MAX).split(",") : new String[0];

      CsvHeaderResult[] array = new CsvHeaderResult[sumArray.length + avgArray.length + minArray.length + maxArray.length];

      int i = 0;
      for (String s : sumArray) {
         attributesName.add(s);
         array[i++] = new SumCsvHeaderResult(s);
      }
      for (String s : minArray) {
         attributesName.add(s);
         array[i++] = new MinCsvHeaderResult(s);
      }
      for (String s : avgArray) {
         attributesName.add(s);
         array[i++] = new AvgCsvHeaderResult(s);
      }
      for (String s : maxArray) {
         attributesName.add(s);
         array[i++] = new MaxCsvHeaderResult(s);
      }

      return array;
   }

   private static void printResults(FileResult fileResult, CsvHeaderResult[] headers, StringBuilder csvFormat, boolean csv) {

      if (fileResult == null) {
         return;
      }

      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("File: ").append(fileResult.getFilePath()).append("\n");

      if (csv) {
         csvFormat.append(fileResult.getFilePath());
      }

      for (CsvHeaderResult header : headers) {
         double value = header.get(fileResult);
         stringBuilder
               .append(header.getHeader()).append("=").append(value).append("\n");

         if (csv) {
            csvFormat.append(",").append(value);
         }
      }

      if (csv) {
         csvFormat.append("\n");
      }
      System.out.println(stringBuilder);
   }

   private static BufferedReader getReader(String filePath) {
      try {
         return new BufferedReader(new FileReader(filePath));
      } catch (FileNotFoundException e) {
         //no-op
      }
      return null;
   }

   private static void close(Closeable closeable) {
      try {
         closeable.close();
      } catch (IOException e) {
         //no-op
      }
   }

   private static class CsvFileProcessor implements Callable<FileResult>, Runnable {

      private final int[] positions;
      private final String[] attributesName;
      private final String filePath;
      private final Map<String, List<Double>> results;
      private boolean hasResults;

      private CsvFileProcessor(String[] attributesName, String filePath) {
         this.filePath = filePath;
         this.positions = new int[attributesName.length];
         this.attributesName = attributesName;
         results = new HashMap<String, List<Double>>();

         for (int i = 0; i < positions.length; ++i) {
            positions[i] = -1;
            results.put(attributesName[i], new LinkedList<Double>());
         }
      }

      @Override
      public void run() {
         BufferedReader reader = getReader(filePath);

         if (reader == null) {
            System.err.println("File " + filePath + " not found!");
            hasResults = false;
            return;
         }

         if (!populateHeaders(reader)) {
            System.err.println("File " + filePath + ": headers not found!");
            close(reader);
            hasResults = false;
            return;
         }

         if (!process(reader)) {
            System.err.println("File " + filePath + ": error processing lines!");
            close(reader);
            hasResults = false;
            return;
         }

         close(reader);
         hasResults = true;
      }

      private boolean populateHeaders(BufferedReader reader) {
         String headers;
         try {
            headers = reader.readLine();
         } catch (IOException e) {
            return false;
         }
         if (headers == null) {
            return false;
         }
         String[] headerArray = headers.split(",");

         for (int i = 0; i < attributesName.length; ++i) {
            for (int j = 0; j < headerArray.length; ++j) {
               if (attributesName[i].equals(headerArray[j])) {
                  positions[i] = j;
                  break;
               }
            }
         }
         return true;
      }

      private boolean process(BufferedReader reader) {
         String line;

         try {
            while ((line = reader.readLine()) != null) {
               String[] values = line.split(",");

               for (int i = 0; i < positions.length; ++i) {
                  int index = positions[i];

                  if (index < 0) {
                     continue;
                  }

                  try {
                     Number number = NumberFormat.getNumberInstance().parse(values[index]);
                     results.get(attributesName[i]).add(number.doubleValue());
                  } catch (ParseException e) {
                     //no-op
                  }
               }
            }
         } catch (IOException e) {
            return false;
         }

         return true;
      }

      @Override
      public FileResult call() throws Exception {
         run();
         return hasResults ? new FileResult(filePath, results) : null;
      }
   }


   private static abstract class CsvHeaderResult {

      protected final String header;

      protected CsvHeaderResult(String header) {
         this.header = header;
      }

      public final String getHeader() {
         return getType() + "(" + header + ")";
      }

      abstract String getType();

      abstract double get(FileResult values);
   }

   private static class SumCsvHeaderResult extends CsvHeaderResult {

      protected SumCsvHeaderResult(String header) {
         super(header);
      }

      @Override
      String getType() {
         return "sum";
      }

      @Override
      double get(FileResult result) {
         Collection<Double> values = result.getResults(header);
         double sum = 0;
         for (double value : values) {
            sum += value;
         }
         return sum;
      }
   }

   private static class AvgCsvHeaderResult extends SumCsvHeaderResult {

      protected AvgCsvHeaderResult(String header) {
         super(header);
      }

      @Override
      String getType() {
         return "avg";
      }

      @Override
      double get(FileResult result) {
         Collection<Double> values = result.getResults(header);
         double sum = super.get(result);
         return values.size() > 0 ? sum / values.size() : 0;
      }
   }

   private static class MinCsvHeaderResult extends CsvHeaderResult {

      protected MinCsvHeaderResult(String header) {
         super(header);
      }

      @Override
      String getType() {
         return "min";
      }

      @Override
      double get(FileResult result) {
         double min = 0;
         boolean first = true;
         Collection<Double> values = result.getResults(header);

         for (double value : values) {
            min = first ? value : calculate(min, value);
            first = false;
         }

         return min;
      }

      protected double calculate(double actual, double value) {
         return Math.min(actual, value);
      }
   }

   private static class MaxCsvHeaderResult extends MinCsvHeaderResult {

      protected MaxCsvHeaderResult(String header) {
         super(header);
      }

      @Override
      String getType() {
         return "max";
      }

      @Override
      protected double calculate(double actual, double value) {
         return Math.max(actual, value);
      }
   }

   private static class FileResult {
      private final String filePath;
      private final Map<String, List<Double>> results;

      private FileResult(String filePath, Map<String, List<Double>> results) {
         this.filePath = filePath;
         this.results = results;
      }

      public String getFilePath() {
         return filePath;
      }

      public List<Double> getResults(String attributeName) {
         return results.get(attributeName);
      }
   }

   private static class Arguments {

      private static final String SEPARATOR = "=";

      private final Map<Option, String> argsValues;
      private final List<String> filePaths;

      private Arguments() {
         argsValues = new EnumMap<Option, String>(Option.class);
         filePaths = new LinkedList<String>();
      }

      public final void parse(String[] args) {
         int idx = 0;
         while (idx < args.length) {
            Option option = Option.fromString(args[idx]);
            if (option == null) {
               filePaths.add(args[idx]);
            } else {
               if (option.isBoolean) {
                  argsValues.put(option, Boolean.toString(true));
               } else {
                  argsValues.put(option, args[idx].split(SEPARATOR, 2)[1]);
               }
            }
            idx++;
         }
      }

      public final void validate() {
         //no-op
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

      public final List<String> getFilePaths() {
         return filePaths;
      }

      @Override
      public final String toString() {
         return "Arguments{" +
               "argsValues=" + argsValues +
               '}';
      }
   }
}
