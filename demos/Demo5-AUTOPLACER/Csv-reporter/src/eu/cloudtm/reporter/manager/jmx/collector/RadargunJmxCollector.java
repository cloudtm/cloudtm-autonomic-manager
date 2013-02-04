package eu.cloudtm.reporter.manager.jmx.collector;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class RadargunJmxCollector extends AbstractJmxCollector {

   public RadargunJmxCollector() {
      super();
   }

   @Override
   protected final void reset(MBeanServerConnection mBeanServerConnection) {
      //no-op
   }

   @Override
   protected void internalSetUp(Properties configuration, MBeanServerConnection mBeanServerConnection,
                                String[] attributes) throws Exception {
      String domain = configuration.getProperty("reporter.resource.jmx.radar_jmx.domain", "org.radargun");
      String[] componentNames = configuration.getProperty("reporter.resource.jmx.radar_jmx.components", "TpccBenchmark").split(",");

      for (String componentName : componentNames) {
         ObjectName component = getObjectName(domain, componentName, mBeanServerConnection);
         if (component != null) {
            Collection<String> componentAttributeList = extractAttributeList(component, mBeanServerConnection, attributes);
            register(component, componentAttributeList);
         } else {
            log.warn("Component " + componentName + " not found in " + getResourceInfo());
         }
      }
   }


   private ObjectName getObjectName(String domain, String componentName, MBeanServerConnection mBeanServer) throws IOException {
      for (ObjectName name : mBeanServer.queryNames(null, null)) {
         if (name.getDomain().equals(domain) && componentName.equals(name.getKeyProperty("stage"))) {
            return name;
         }
      }
      return null;
   }

}
