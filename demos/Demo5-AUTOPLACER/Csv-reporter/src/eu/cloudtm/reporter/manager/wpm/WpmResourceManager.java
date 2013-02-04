package eu.cloudtm.reporter.manager.wpm;

import eu.cloudtm.reporter.manager.ResourceInfo;
import eu.cloudtm.reporter.manager.Resource;
import eu.cloudtm.reporter.manager.ResourceManager;
import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.manager.DefaultCacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Manages the WPM resources
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class WpmResourceManager implements ResourceManager {

   private String cacheName;
   private String[] attributes;
   private Cache<String, String> cache;
   private final LinkedList<WpmResource> resources = new LinkedList<WpmResource>();

   @Override
   public void init(Properties config, String[] attributesToCollect) {
      this.cache = getCache();
      this.attributes = attributesToCollect;
      this.cacheName = config.getProperty("reporter.resource.wpm.cache_name", "CloudTM");
   }

   @Override
   public void addResource(ResourceInfo resourceInfo) {
      resources.add(new WpmResource(resourceInfo, cacheName, attributes));
   }

   @Override
   public void collect() {
      for (WpmResource wpmResource : resources) {
         wpmResource.update(cache);
      }
   }

   @Override
   public Collection<Resource> resources() {
      return Collections.<Resource>unmodifiableList(resources);
   }

   private Cache<String, String> getCache() {
      GlobalConfiguration gc = GlobalConfiguration.getClusteredDefault();
      gc.setClusterName("LogServiceConnection");
      Configuration c = new Configuration();
      c.setCacheMode(Configuration.CacheMode.REPL_SYNC);
      return new DefaultCacheManager(gc, c).getCache();
   }
}
