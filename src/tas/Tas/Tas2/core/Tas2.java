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

package Tas2.core;


import Tas2.config.Tas2ConfigurationFactory;
import Tas2.config.configs.Tas2Configuration;
import Tas2.core.environment.DSTMScenarioTas2;
import Tas2.exception.Tas2Exception;
import Tas2.logicalModel.solver.Solver;
import open.queues.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 27/11/12
 */
public class Tas2 {

   private final static Log log = LogFactory.getLog(Tas2.class);

   static {

      Util.init(8);
   }

   public ModelResult solve(DSTMScenarioTas2 dstmScenario) throws Tas2Exception {
      ModelResult result;
      Tas2Configuration config = this.buildConfiguration();
      Solver solver = buildSolver(config);

      PropertyConfigurator.configure("conf/log4j.properties");
      try {
         result = solver.solve(dstmScenario);
      } catch (Exception e) {
         return handleException(solver, e, config, dstmScenario);
      }
      return result;
   }

   private ModelResult handleException(Solver s, Exception e, Tas2Configuration conf, DSTMScenarioTas2 scenario) throws Tas2Exception {
      SolverFactory.SOLVERS origin = conf.getSolverFactoryConfig().getSolverType();
      if (resortToMinimalError(origin)) {
         log.warn("An iterative cubist solver could not find a solution. Exploring all solutions");
         conf.getSolverFactoryConfig().setSolverType("CUBIST_MIN_ERROR");
         try {
            return buildSolver(conf).solve(scenario);
         } catch (Exception t) {
            throw new Tas2Exception(t.getMessage());
         } finally {
            conf.getSolverFactoryConfig().setSolverType(origin.name());
         }
      } else {
         //e.printStackTrace();
         throw new Tas2Exception(e.toString());
      }
   }

   private boolean resortToMinimalError(SolverFactory.SOLVERS origin) {
      return origin.equals(SolverFactory.SOLVERS.CUBIST_TAS);
   }

   private Tas2Configuration buildConfiguration() throws Tas2Exception {
      String packageName = "Tas2.config.configs.";
      try {
         return Tas2ConfigurationFactory.buildConfiguration("conf/tas2.xml", packageName);
      } catch (IOException e) {
         throw new Tas2Exception(e.getMessage());
      }

   }

   private Solver buildSolver(Tas2Configuration config) throws Tas2Exception {

      return SolverFactory.buildSolver(config);


   }

}
