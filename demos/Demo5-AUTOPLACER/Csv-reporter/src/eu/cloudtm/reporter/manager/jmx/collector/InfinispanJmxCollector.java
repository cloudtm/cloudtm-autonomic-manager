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
public class InfinispanJmxCollector extends AbstractJmxCollector {

   public InfinispanJmxCollector() throws Exception {
      super();
   }

   private ObjectName getObjectName(String domain, String cacheName, String componentName, MBeanServerConnection mBeanServer) throws IOException {
      for (ObjectName name : mBeanServer.queryNames(null, null)) {
         if (name.getDomain().equals(domain)) {
            if ("Cache".equals(name.getKeyProperty("type")) &&
                  name.getKeyProperty("name").startsWith("\"" + cacheName + "(") &&
                  name.getKeyProperty("component").equals(componentName)) {
               return name;
            }
         }
      }
      return null;
   }

   private void tryResetStats(ObjectName component, MBeanServerConnection mBeanServer) {
      Object[] emptyArgs = new Object[0];
      String[] emptySig = new String[0];
      try {
         mBeanServer.invoke(component, "resetStatistics", emptyArgs, emptySig);
         return;
      } catch (Exception e) {
         //no-op
      }
      try {
         mBeanServer.invoke(component, "resetStats", emptyArgs, emptySig);
         return;
      } catch (Exception e) {
         //no-op
      }
      try {
         mBeanServer.invoke(component, "reset", emptyArgs, emptySig);
         return;
      } catch (Exception e) {
         //no-op
      }
      log.warn("[" + getResourceInfo() + "]: Cannot reset statistics for " + component);
   }

   @Override
   protected void reset(MBeanServerConnection mBeanServerConnection) {
      for (JmxComponent jmxComponent : jmxComponentList) {
         tryResetStats(jmxComponent.getObjectName(), mBeanServerConnection);
      }
   }

   @Override
   protected void internalSetUp(Properties configuration, MBeanServerConnection mBeanServerConnection, String[] attributes) throws Exception {
      String domain = configuration.getProperty("reporter.resource.jmx.ispn_jmx.domain", "org.infinispan");
      String cacheName = configuration.getProperty("reporter.resource.jmx.ispn_jmx.cache_name", "x");
      String[] componentNames = configuration.getProperty("reporter.resource.jmx.ispn_jmx.components", "ExtendedStatistics").split(",");

      for (String componentName : componentNames) {
         ObjectName component = getObjectName(domain, cacheName, componentName, mBeanServerConnection);
         if (component != null) {
            Collection<String> componentAttributeList = extractAttributeList(component, mBeanServerConnection, attributes);
            register(component, componentAttributeList);
         } else {
            log.warn("Component " + componentName + " not found in " + getResourceInfo());
         }
      }
   }
}
