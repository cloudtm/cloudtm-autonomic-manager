package controllerTas.config.xml;/*
 * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
 * Copyright 2013 INESC-ID and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt
 *         Date: 01/02/13
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class TasControllerConfigXmlParser {

   private String filePath;
   private String packageName;// =  //Package name of the element Configuration

   private final static Log log = LogFactory.getLog(TasControllerConfigXmlParser.class);


   public TasControllerConfigXmlParser(String filePath, String packageName) {
      this.filePath = filePath;
      this.packageName = packageName;
   }


   public Object parseObject() throws IOException {
      try {
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         Document doc = docBuilder.parse(new File(this.filePath));
         Node root = doc.getFirstChild();
         return newParsedObject(root);
      } catch (Throwable t) {
         t.printStackTrace();
         System.exit(-1);
      }
      return null;   //unreachable
   }


   private Object newParsedObject(Node root) {
      try {
         return recursiveParseElement(root);
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(-1);
      }
      return null; //unreachable statement
   }


   private Object recursiveParseElement(Node element) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, ClassNotFoundException, InstantiationException {
      log.debug("Parsing " + element.getNodeName());
      Object thisObject = parseElementNode(element);
      NodeList childNodes = element.getChildNodes();
      int size = childNodes.getLength();
      //Base case
      if (size == 0) {
         return thisObject;
      }
      Class cls = Class.forName(packageName + element.getNodeName());
      for (int i = 0; i < size; i++) {
         Node elem = childNodes.item(i);
         if (elem.getNodeType() == Node.ELEMENT_NODE) {
            Object o = recursiveParseElement(elem);
            String nodeName = elem.getNodeName();
            this.invokeSet(thisObject, cls, nodeName, o);
         }
      }
      return thisObject;

   }

   private void typeAwareInvokeSet(Object newInstance, Method m, Object param, Class paramType) throws InvocationTargetException, IllegalAccessException {

      if (param.getClass() == String.class) {
         log.debug("Invoking method " + m.getName() + " with parameter of type " + paramType.getName());
         String paramm = (String) param;
         if (paramType.getName().equals("int")) {
            m.invoke(newInstance, Integer.parseInt(paramm));
         } else if (paramType.getName().equals("double")) {
            m.invoke(newInstance, Double.parseDouble(paramm));
         } else if (paramType.getName().equals("long")) {
            m.invoke(newInstance, Long.parseLong(paramm));
         } else if (paramType.getName().equals("java.lang.String")) {
            m.invoke(newInstance, (String) (paramm));
         } else if (paramType.getName().equals("boolean")) {
            boolean b = param.equals("true");
            m.invoke(newInstance, b);
         }
      } else {
         log.debug("Invoking method " + m.getName() + " with parameter of class " + paramType.getName());

         m.invoke(newInstance, param);
      }
   }

   //This does not support nested Element  yet (I should only put the cycle I use to create the root configuration in a recursive method)
   private Object parseElementNode(Node element) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, InstantiationException {

      Class clazz = Class.forName(packageName + element.getNodeName());
      Object newInstance = clazz.newInstance();
      if (element.hasAttributes()) {
         for (int k = 0; k < element.getAttributes().getLength(); k++) {
            Node attribute = element.getAttributes().item(k);
            String nodeName = attribute.getNodeName();
            String param = attribute.getNodeValue();
            this.invokeSet(newInstance, clazz, nodeName, param);
         }
      }

      return newInstance;
   }

   private void invokeSet(Object o, Class clazz, String nodeName, Object param) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
      String nameMethod = "set" + nodeName.substring(0, 1).toUpperCase() + nodeName.substring(1);
      log.debug("Going to invoke " + nameMethod + " on Object of " + clazz);
      Class returnType = this.getClassMethodIfExists(clazz, nameMethod);
      if (returnType == null) {
         throw new NoSuchMethodException("Non ho trovato il metodo " + nameMethod);
      }
      Method m = clazz.getMethod(nameMethod, returnType);
      this.typeAwareInvokeSet(o, m, param, returnType);
   }

   private Class getClassMethodIfExists(Class c, String method) {
      Method[] declared = c.getDeclaredMethods();
      for (Method aDeclared : declared) {
         if (aDeclared.getName().equals(method)) {
            Class[] array = aDeclared.getParameterTypes();
            if (array.length > 1) {
               throw new RuntimeException("Ho trovato il metodo " + method + "ma non ÔøΩ un setter con un solo parametro!");
            } else
               return array[0];
         }
      }
      return null;
   }


}
