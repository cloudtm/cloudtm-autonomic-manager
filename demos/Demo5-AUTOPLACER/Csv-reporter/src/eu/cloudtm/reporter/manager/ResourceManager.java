package eu.cloudtm.reporter.manager;

import java.util.Collection;
import java.util.Properties;

/**
 * The resource manager interface
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public interface ResourceManager {
   
   void init(Properties config, String[] attributesToCollect);
   
   void addResource(ResourceInfo ip);
   
   void collect();
   
   Collection<Resource> resources();   
   
}
