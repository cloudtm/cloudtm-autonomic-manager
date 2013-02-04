/*
 *
 *  * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
 *  * Copyright 2013 INESC-ID and/or its affiliates and other
 *  * contributors as indicated by the @author tags. All rights reserved.
 *  * See the copyright.txt in the distribution for a full listing of
 *  * individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 3.0 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package oracle.cubist.datasetBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 29/10/12
 */
public abstract class CubistDatasetBuilder {


   protected static final String sep = ",";
   private Map<String, Filter> filters = new HashMap<String, Filter>();
   private static final Log log = LogFactory.getLog(CubistDatasetBuilder.class);
   //private static final DecimalFormat dcf = new DecimalFormat("0,0000000000");
   //private RangeScenario[] scenarios;


   /**
    * @param output         the .data file that is going to be created
    * @param rangeScenarios
    * @throws java.io.IOException
    */
   public final void createDataset(String output, RangeScenario[] rangeScenarios) throws IOException {
      File out = new File(output);
      if (!out.exists()) {
         if (!out.createNewFile())
            throw new IOException("Unable to create " + out);
      }
      FileWriter fw = new FileWriter(new File(output));
      fw.write(__createDataset(rangeScenarios));
      fw.close();
   }

   protected abstract String __createDataset(RangeScenario[] r);

   public void addFilter(String feat, double lowerBound, double upperBound) {
      log.trace("Filtering values for " + feat + " outside the range [" + lowerBound + ", " + upperBound + "]");
      this.filters.put(feat, new Filter(feat, lowerBound, upperBound));
   }

   protected boolean filterIfNecessary(String readLine, String header) {
      if (this.filters.isEmpty())
         return false;
      Filter f;
      String[] featValues = readLine.split(";");
      String[] featNames = header.split(";");
      Set<String> toFilter = this.filters.keySet();
      int index;
      double value;
      for (String feat : toFilter) {
         index = featureIndex(feat, featNames);
         value = parseDoubleFromCsv(featValues[index]);
          /*
          try{
         value = Double.parseDouble(featValues[index]);
          }
          catch(NumberFormatException n){
              log.debug(n);
              String ff = featValues[index];
              String fff = ff.replace(",",";");
              ff = fff.replace(".","");
              fff = ff.replace(";",".");
              value = Double.parseDouble(fff);
          }
          */
         if ((f = this.filters.get(feat)) != null && !f.accept(value)) {
            log.debug("Filtering out " + feat + " = " + value);
            return true;
         }
      }
      return false;
   }

   private int featureIndex(String s, String[] ss) {
      int index = 0;
      for (String sss : ss) {
         if (s.equals(sss))
            return index;
         index++;
      }
      throw new RuntimeException(s + " not found");
   }

   protected double parseDoubleFromCsv(String s) {
        /*try {
            double ret = Double.parseDouble(s);
            System.out.println("Safely parsed "+s+" into "+ret);
            return ret;
        } catch (NumberFormatException n) {     */
      log.trace("Double.parseDouble failed on " + s);
      DecimalFormatSymbols symbols = new DecimalFormatSymbols();
      symbols.setDecimalSeparator(',');
      DecimalFormat dcf = new DecimalFormat();
      dcf.setDecimalFormatSymbols(symbols);
      try {
         double ret = dcf.parse(s).doubleValue();
         log.trace("Parsed " + s + " into " + dcf.parse(s).doubleValue());
         return ret;
      } catch (ParseException e) {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         throw new RuntimeException("ciao");
      }
      //}
   }

}
