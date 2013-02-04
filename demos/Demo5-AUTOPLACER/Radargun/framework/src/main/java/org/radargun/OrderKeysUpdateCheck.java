package org.radargun;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * // TODO: Document this
 *
 * @author pruivo
 * @since 4.0
 */
public class OrderKeysUpdateCheck {

   public static void main(String[] args) {
      if (args.length == 0) {
         System.err.println("Error. file expected");
         System.exit(1);
      }

      HashMap<String, String> allKeys = new HashMap<String, String>(1024);

      //KEY_986|N@KSSONBIF:null]|EXCM@XONLK:null]
      processInitialFile(args[0], allKeys);
      for (int i = 1; i < args.length; ++i) {
         processRemainingFiles(args[i], allKeys);
      }

      System.exit(0);
   }

   private static void processInitialFile(String filePath, Map<String, String> allKeys) {
      System.out.println("Processing initial file " + filePath);
      try {
         BufferedReader br = new BufferedReader(new FileReader(filePath));

         String line;
         while ((line = br.readLine()) != null) {
            allKeys.put(getKey(line), getValue(line));
         }
         br.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();  // TODO: Customise this generated block
      } catch (IOException e) {
         e.printStackTrace();  // TODO: Customise this generated block
      }
   }

   private static void processRemainingFiles(String filePath, Map<String, String> allKeys) {
      System.out.println("Processing file " + filePath);
      try {
         BufferedReader br = new BufferedReader(new FileReader(filePath));

         String line;
         while ((line = br.readLine()) != null) {
            String key = getKey(line);
            String value = getValue(line);

            String expectedValue = allKeys.get(key);
            if (expectedValue == null) {
               allKeys.put(key, value);
            } else if (!value.equals(expectedValue)) {
               System.err.println("Error in key " + key);
               System.exit(1);
            }
         }
         br.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();  // TODO: Customise this generated block
      } catch (IOException e) {
         e.printStackTrace();  // TODO: Customise this generated block
      }
   }

   private static String getKey(String line) {
      return line.split("[|]", 2)[0];
   }

   private static String getValue(String line) {
      return line.split("[|]", 2)[1];
   }
}
