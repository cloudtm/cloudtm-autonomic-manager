package eu.cloudtm.reporter.manager.jmx;

import eu.cloudtm.reporter.logging.Log;
import eu.cloudtm.reporter.logging.LogFactory;
import eu.cloudtm.reporter.manager.ResourceInfo;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;

/**
 * Tries to creates a JMX connection using different protocols
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class JmxConnection {

   private static final Log log = LogFactory.getLog(JmxConnection.class);

   private static final JmxProtocol[] PROTOCOLS = new JmxProtocol[] {
         new JmxProtocol() {
            @Override
            public String url(String hostname, String port) {
               return String.format("service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi", hostname, port);
            }
         },
         new JmxProtocol() {
            @Override
            public String url(String hostname, String port) {
               return String.format("service:jmx:remoting-jmx://%s:%s", hostname, port);
            }
         }
   };
   
   public static final Map<String, Object> ENVIRONMENT = new HashMap<String, Object>();   

   public static MBeanServerConnection connect(ResourceInfo resourceInfo) {
      MBeanServerConnection connection = null;
      for (JmxProtocol protocol : PROTOCOLS) {
         String url = protocol.url(resourceInfo.getAddress(), resourceInfo.getPort());
         try {
            connection = JMXConnectorFactory.connect(new JMXServiceURL(url), ENVIRONMENT).getMBeanServerConnection();
            log.info("Connection successfully with %s", url);
            return connection;
         } catch (Exception e) {
            log.warn("Connection failed with %s. %s", url, e.getMessage());
         }
      }
      log.info("Connection with %s %s", resourceInfo, connection == null ? "FAILED" : "OK");
      return connection;
   }

   private static abstract class JmxProtocol {
      String url(String hostname, int port) {
         return url(hostname, Integer.toString(port));
      }
      abstract String url(String hostname, String port);
   }

}
