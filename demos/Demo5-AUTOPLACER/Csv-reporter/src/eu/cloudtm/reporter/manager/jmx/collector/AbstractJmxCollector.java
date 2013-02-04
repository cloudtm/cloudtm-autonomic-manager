package eu.cloudtm.reporter.manager.jmx.collector;

import eu.cloudtm.reporter.logging.Log;
import eu.cloudtm.reporter.logging.LogFactory;
import eu.cloudtm.reporter.manager.ResourceInfo;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 1.0
 */
public abstract class AbstractJmxCollector implements JmxCollector {

   protected final Set<JmxComponent> jmxComponentList;
   protected final Log log = LogFactory.getLog(getClass());
   private ResourceInfo resourceInfo;

   protected AbstractJmxCollector() {
      jmxComponentList = new HashSet<JmxComponent>();
   }

   @Override
   public final void setUp(Properties configuration, MBeanServerConnection mBeanServerConnection,
                           ResourceInfo resourceInfo, String[] attributes) throws Exception {
      this.resourceInfo = resourceInfo;
      internalSetUp(configuration, mBeanServerConnection, attributes);
   }

   @Override
   public Map<String, Object> updateAndReset(MBeanServerConnection mBeanServerConnection) {
      AttributeList attributeList = new AttributeList();

      for (JmxComponent jmxComponent : jmxComponentList) {
         attributeList.addAll(getAttributes(mBeanServerConnection, jmxComponent));
      }

      reset(mBeanServerConnection);

      return convertAttributeListToMap(attributeList);
   }

   @Override
   public boolean hasObjectNames() {
      return !jmxComponentList.isEmpty();
   }

   protected abstract void reset(MBeanServerConnection mBeanServerConnection);

   protected abstract void internalSetUp(Properties configuration, MBeanServerConnection mBeanServerConnection,
                                         String[] attributes) throws Exception;

   protected final Collection<String> extractAttributeList(ObjectName objectName,
                                                           MBeanServerConnection mBeanServerConnection,
                                                           String[] attributes) throws Exception {
      LinkedList<String> result = new LinkedList<String>();
      Set<String> attributesSet = new HashSet<String>(Arrays.asList(attributes));
      for (MBeanAttributeInfo attributeInfo : mBeanServerConnection.getMBeanInfo(objectName).getAttributes()) {
         if (attributesSet.contains(attributeInfo.getName())) {
            result.add(attributeInfo.getName());
         }
      }

      return result;
   }

   protected final AttributeList getAttributes(MBeanServerConnection mBeanServer, JmxComponent jmxComponent) {
      try {
         return mBeanServer.getAttributes(jmxComponent.getObjectName(), jmxComponent.getAttributes());
      } catch (Exception e) {
         log.logErrorUpdatingAttributes(resourceInfo, Arrays.asList(jmxComponent.getAttributes()), e);
      }
      return null;
   }

   protected final void register(ObjectName objectName, Collection<String> attributes) {
      jmxComponentList.add(new JmxComponent(objectName, attributes));
   }

   protected final ResourceInfo getResourceInfo() {
      return resourceInfo;
   }

   protected final class JmxComponent {
      private final ObjectName objectName;
      private final String[] attributes;

      public JmxComponent(ObjectName objectName, Collection<String> attributes) {
         this.objectName = objectName;
         this.attributes = attributes.toArray(new String[attributes.size()]);
      }

      public ObjectName getObjectName() {
         return objectName;
      }

      public String[] getAttributes() {
         return attributes;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;

         JmxComponent that = (JmxComponent) o;

         return objectName.equals(that.objectName);
      }

      @Override
      public int hashCode() {
         return objectName.hashCode();
      }

      @Override
      public String toString() {
         return "JmxComponent{" +
               "objectName=" + objectName.getCanonicalName() +
               ", attributes=" + Arrays.asList(attributes) +
               '}';
      }
   }

   protected final Map<String, Object> convertAttributeListToMap(AttributeList attributeList) {
      Map<String, Object> result = new HashMap<String, Object>();
      for (Attribute attribute : attributeList.asList()) {
         if (attribute.getValue() != null) {
            result.put(attribute.getName(), attribute.getValue());
         }
      }
      return result;
   }

}
