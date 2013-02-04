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
public class TxOutcomeCheck {

   public static void main(String[] args) {
      if (args.length == 0) {
         System.err.println("Error. file expected");
         System.exit(1);
      }

      Map<String, Boolean> outcomes = new HashMap<String, Boolean>();

      for (String filePath : args) {
         processFile(filePath, outcomes);
      }
   }

   public static void processFile(String filePath, Map<String, Boolean> outcomes) {
      try {
         BufferedReader br = new BufferedReader(new FileReader(filePath));

         String line;

         while ((line = br.readLine()) != null) {            

            String transaction = getTransaction(line);
            boolean outcome = getOutcome(line);
            
            Boolean existing = outcomes.get(transaction);
            if (existing == null) {
               outcomes.put(transaction, outcome);
            } else if (existing != outcome) {
               System.out.println("Transaction " + transaction + " has a different outcome, while processing " + filePath);
            }
            
         }
         br.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();  // TODO: Customise this generated block
      } catch (IOException e) {
         e.printStackTrace();  // TODO: Customise this generated block
      }
   }
   
   public static String getTransaction(String line) {
      String[] split = line.split(" ");
      return split.length > 0 ? split[0] : "";
   }
   
   public static boolean getOutcome(String line) {
      String[] split = line.split("[(]");
      if (split.length <= 0) {
         return false;
      }
      
      String[] split2 = split[1].split("[)]");
      return split2.length > 0 && split2[0].equals("ok");
   }

}
