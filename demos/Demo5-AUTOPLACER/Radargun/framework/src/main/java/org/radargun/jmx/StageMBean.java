package org.radargun.jmx;

import org.radargun.jmx.annotations.MBean;
import org.radargun.jmx.annotations.ManagedOperation;
import org.radargun.jmx.metadata.JmxAttributeMetadata;

import javax.management.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class StageMBean implements DynamicMBean {

   private final Object obj;

   private final Map<String, JmxAttributeMetadata> attributes = new ConcurrentHashMap<String, JmxAttributeMetadata>(64);
   private final Map<String, Method> operations = new ConcurrentHashMap<String, Method>(64);

   private final MBeanInfo mBeanInfo;

   public StageMBean(Object instance, List<Method> managedAttributeMethods, List<Method> managedOperationMethods) throws IntrospectionException {
      if (instance == null)
         throw new NullPointerException("Cannot make an MBean wrapper for null instance");

      this.obj = instance;
      Class<?> objectClass = instance.getClass();

      // Load up all fields.                  
      for (Method method : managedAttributeMethods) {
         JmxAttributeMetadata metadata = new JmxAttributeMetadata(method);
         attributes.put(metadata.getName(), metadata);
      }

      // And operations      
      MBeanOperationInfo[] opInfos = new MBeanOperationInfo[managedOperationMethods.size()];
      int i = 0;
      for (Method method : managedOperationMethods) {
         String attributeName = JmxAttributeMetadata.extractFieldName(method.getName());
         if (attributeName != null) {
            //update attribute setter
            JmxAttributeMetadata metadata = attributes.get(attributeName);
            if (metadata != null) {
               metadata.setSetter(method);
            }
         }

         ManagedOperation managedOperation = method.getAnnotation(ManagedOperation.class);
         opInfos[i] = new MBeanOperationInfo(managedOperation.description(), method);
         operations.put(opInfos[i++].getName(), method);
      }

      MBeanAttributeInfo[] attributeInfoArray = new MBeanAttributeInfo[managedAttributeMethods.size()];
      i = 0;
      for (Map.Entry<String, JmxAttributeMetadata> attribute : attributes.entrySet()) {
         JmxAttributeMetadata metadata = attribute.getValue();
         attributeInfoArray[i++] = new MBeanAttributeInfo(metadata.getName(), metadata.getDescription(),
                                                          metadata.getGetter(), metadata.getSetter());
      }

      mBeanInfo = new MBeanInfo(objectClass.getSimpleName(), objectClass.getAnnotation(MBean.class).description(),
                                attributeInfoArray, new MBeanConstructorInfo[0], opInfos, new MBeanNotificationInfo[0]);
   }

   @Override
   public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
      Attribute attr = getAttributeValue(attribute);
      if (attr == null) {
         throw new AttributeNotFoundException("Attribute " + attribute + " not found");
      }
      return attr.getValue();
   }

   @Override
   public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
      setAttributeValue(attribute.getName(), attribute.getValue());
   }

   @Override
   public AttributeList getAttributes(String[] attributesName) {
      AttributeList list = new AttributeList(attributesName.length);

      for (String attribute : attributesName) {
         Attribute attr = getAttributeValue(attribute);
         if (attr != null) {
            list.add(attr);
         }
      }

      return list;
   }

   @Override
   public AttributeList setAttributes(AttributeList objects) {
      AttributeList list = new AttributeList(objects.size());

      for (Attribute attribute : objects.asList()) {
         Attribute ret = setAttributeValue(attribute.getName(), attribute.getValue());
         if (ret != null) {
            list.add(ret);
         }
      }

      return list;
   }

   @Override
   public Object invoke(String operationName, Object[] parameters, String[] signature) throws MBeanException, ReflectionException {
      Method method = operations.get(operationName);
      if (method == null) {
         throw new MBeanException(new OperationsException("Operation " + operationName + " not found"));
      }
      try {
         return method.invoke(obj, parameters);
      } catch (Exception e) {
         throw new ReflectionException(e);
      }
   }

   @Override
   public MBeanInfo getMBeanInfo() {
      return mBeanInfo;
   }

   private Attribute getAttributeValue(String attribute) {
      JmxAttributeMetadata metadata = attributes.get(attribute);
      if (metadata == null) {
         return null;
      }
      Method method = metadata.getGetter();
      Object ret;
      try {
         ret = method.invoke(obj);
      } catch (Exception e) {
         return null;
      }
      return new Attribute(attribute, ret);
   }

   private Attribute setAttributeValue(String attribute, Object value) {
      JmxAttributeMetadata metadata = attributes.get(attribute);
      if (metadata == null) {
         return null;
      }
      Method method = metadata.getSetter();
      try {
         method.invoke(obj, value);
      } catch (Exception e) {
         return null;
      }
      return new Attribute(attribute, value);
   }
}
