package controllerTas.actions.gnuplot;   /*
 * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
 * Copyright 2013 INESC-ID and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/02/13
 */

import controllerTas.config.configs.GnuplotConfig;
import controllerTas.common.KPI;
import controllerTas.common.Scale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/02/13
 */
public class ThroughputPlotter implements Gnuplotter {

   private String dataPath;
   private String scriptPath;
   private String plotPath;
   private GnuplotConfig config;
   private static final Log log = LogFactory.getLog(ThroughputPlotter.class);

   public ThroughputPlotter(GnuplotConfig config) {
      this.config = config;
      dataPath = slashedPath(config.getPathToData());
      scriptPath = slashedPath(config.getPathToScript());
      plotPath = slashedPath(config.getPathToPlot());

   }


   public void plot(Set<KPI> kpis) throws GnuplotException {
      String header = "NumNodes NumThreads Throughput";
      log.trace("Going to generate PlottableData");
      PlottableData data = dataFromKPIs(kpis, header);
      String name = "Throughput";//_" + System.currentTimeMillis();
      log.trace("Going to produce file " + name + " out of PlottableData");
      produceDataFile(data, name, "txt");
      GnuplotExec gexec = new GnuplotExec(config.getExec(), scriptPath+""+name+".p");
      gexec.exec();
      long now = System.currentTimeMillis();
      move(name+".eps", plotPath+""+name+"_" + now + ".eps");
      move(dataPath+""+name+".txt",dataPath+""+name+"_"+now+".txt");
      log.info("New plot produced and stored in "+plotPath+""+name+"_" + now + ".eps");
   }

   private void move(String file, String dest) {
      try {
         String[] command = new String[]{"mv", file, dest};
         log.trace(Arrays.toString(command));
         Runtime.getRuntime().exec(command);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private PlottableData dataFromKPIs(Set<KPI> kpis, String header) {
      PlottableData data = new PlottableData(header);
      PlottableDataLine pdl;
      for (KPI kpi : kpis) {
         pdl = new PlottableDataLine(kpi.getScale(), kpi.getThroughput()*1e9);
         log.trace("Insert " + pdl);
         data.addDataLine(pdl);
      }
      return data;
   }

   public void produceDataFile(PlottableData data, String name, String extension) {
      try {
         PrintWriter pw = new PrintWriter(new FileWriter(new File(slashedPath(dataPath) + name + "." + extension)));
         String header = header(data.getHeader());
         pw.println(header);
         TreeSet<PlottableDataLine> set = new TreeSet<PlottableDataLine>(new LineComparator());
         set.addAll(data.getLines());
         int lastNodes = -1, newNodes;
         for (PlottableDataLine l : set) {
            newNodes = l.getScale().getNumNodes();
            if (newNodes(lastNodes, newNodes))
               pw.println("");
            lastNodes = newNodes;
            log.trace("Writing " + l.toString());
            pw.println(line(l));
         }
         pw.flush();
         pw.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private boolean newNodes(int exNodes, int newNodes) {
      return exNodes != -1 && exNodes != newNodes;
   }

   private String line(PlottableDataLine data) {
      StringBuilder sb = new StringBuilder();
      String sep = " ";
      Scale scale = data.getScale();
      sb.append(scale.getNumNodes());
      sb.append(sep);
      sb.append(scale.getNumThreads());
      for (double d : data.getValues()) {
         sb.append(sep);
         sb.append(d);
      }
      return sb.toString();
   }

   private String slashedPath(String s) {
      if (s.endsWith("/"))
         return s;
      return s.concat("/");
   }

   private String header(String header) {
      return "#" + header;
   }

}
