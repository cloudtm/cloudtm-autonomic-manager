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
public class KeysFinalValueCheck {
   
public static void main(String[] args) {
      if (args.length == 0) {
         System.err.println("Error. file expected");
         System.exit(1);
      }

      HashMap<String, String> keysFinalValues = new HashMap<String, String>();

      for (String filePath : args) {
         processFile(filePath, keysFinalValues);
      }
   }

   public static void processFile(String filePath, Map<String, String> finalValues) {
      try {
         BufferedReader br = new BufferedReader(new FileReader(filePath));

         String line;

         while ((line = br.readLine()) != null) {            

            String key = getKey(line);
            String value = getValue(line);
            
            String existing = finalValues.get(key);
            if (existing == null) {
               finalValues.put(key, value);
            } else if (!existing.equals(value)) {
               System.out.println("Key " + key + " has a different value, while processing " + filePath);
            }
            
         }
         br.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();  // TODO: Customise this generated block
      } catch (IOException e) {
         e.printStackTrace();  // TODO: Customise this generated block
      }
   }
   
   public static String getKey(String line) {
      String[] split = line.split("=>");
      return split.length > 0 ? split[0] : "";
   }
   
   public static String getValue(String line) {
      String[] split = line.split("=>");
      if (split.length <= 1) {
         return "";
      }
            
      return split[1].length() > 10 ? split[1].substring(0,10) : "";
   }
   
   
}
