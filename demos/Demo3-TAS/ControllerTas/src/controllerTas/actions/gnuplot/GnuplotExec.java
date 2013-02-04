package controllerTas.actions.gnuplot;  /*
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/02/13
 */
public class GnuplotExec {

   private String[] execCommand;
   private static final Log log = LogFactory.getLog(GnuplotExec.class);

   public GnuplotExec(String command, String script) {
      execCommand = new String[]{command, script};
   }

   public void exec() throws GnuplotException {
      try {
         Process p = Runtime.getRuntime().exec(execCommand);
         checkForError(p);
         p.destroy();
      } catch (Exception e) {
         throw new GnuplotException(e.getMessage());
      }
   }

   protected void checkForError(Process p) throws IOException, GnuplotException {
      BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      String read;

      StringBuilder errorString = new StringBuilder();
      boolean error = false;

      while ((read = stderr.readLine()) != null) {
         errorString.append(read);
         error = true;
      }

      if (error) {
         log.warn(errorString.toString());
         throw new GnuplotException((read));
      }
   }
}

