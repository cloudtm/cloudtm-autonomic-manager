package eu.cloudtm.reporter.manager.jmx;

import eu.cloudtm.reporter.logging.Log;
import eu.cloudtm.reporter.logging.LogFactory;
import eu.cloudtm.reporter.manager.Resource;
import eu.cloudtm.reporter.manager.ResourceInfo;
import eu.cloudtm.reporter.manager.ResourceManager;

import javax.management.remote.JMXConnector;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Manages JMX resources
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class JmxResourceManager implements ResourceManager {

   private static final Log log = LogFactory.getLog(JmxResourceManager.class);
   
   private final LinkedList<JmxResource> resources = new LinkedList<JmxResource>();
   private String[] attributes;
   private Properties config;

   @Override
   public void init(Properties config, String[] attributesToCollect) {
      this.config = config;
      this.attributes = attributesToCollect;
      
      String username = config.getProperty("reporter.resource.jmx.username", null);
      String password = config.getProperty("reporter.resource.jmx.password", null);
      
      if (username != null && !username.isEmpty()) {
         JmxConnection.ENVIRONMENT.put(JMXConnector.CREDENTIALS, new String[] {username, password});
      }
   }

   @Override
   public void addResource(ResourceInfo resourceInfo) {
      try {
         resources.add(new JmxResource(resourceInfo, attributes, config));
      } catch (Exception e) {
         log.warn("Error adding %s. %s", resourceInfo, e.getMessage());         
      }
   }

   @Override
   public void collect() {
      for (JmxResource resource : resources) {
         resource.updateAndReset();
      }
   }

   @Override
   public Collection<Resource> resources() {
      return Collections.<Resource>unmodifiableList(resources);
   }
}
