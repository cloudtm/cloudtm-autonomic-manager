package eu.cloudtm.reporter.manager.jmx.collector;

import eu.cloudtm.reporter.manager.ResourceInfo;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 4.0
 */
public interface JmxCollector {
   
   void setUp(Properties configuration, MBeanServerConnection mBeanServerConnection, ResourceInfo resourceInfo, 
              String[] attributes) throws Exception;
   
   Map<String, Object> updateAndReset(MBeanServerConnection mBeanServerConnection);
   
   boolean hasObjectNames();
   
}
