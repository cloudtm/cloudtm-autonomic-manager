package controllerTas.Main;

import controllerTas.config.TasControllerConfigurationFactory;
import controllerTas.config.configs.TasControllerConfiguration;
import controllerTas.controller.TasController;
import org.apache.log4j.PropertyConfigurator;

public class GnuplotMain {

   public static void main(String args[]) throws Exception {
      PropertyConfigurator.configure("conf/log4j.properties");
      TasControllerConfiguration config = TasControllerConfigurationFactory.buildConfiguration("conf/controller.xml", "controllerTas.config.configs.");
      TasController t = new TasController(config);
      t.consumeStats(null, null);
   }
}
