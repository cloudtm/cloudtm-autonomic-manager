/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 */

package org.radargun.jmx.metadata;

import org.radargun.jmx.annotations.ManagedAttribute;

import java.lang.reflect.Method;

/**
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class JmxAttributeMetadata {
   private String name;
   private String description;
   private Method getter;
   private Method setter;

   public JmxAttributeMetadata(Method getter) {
      ManagedAttribute annotation = getter.getAnnotation(ManagedAttribute.class);
      description = annotation.description();
      if (annotation.attributeName().equals("")) {
         name = extractFieldName(getter.getName());
      } else {
         name = annotation.attributeName();
      }
      this.getter = getter;

   }

   public String getName() {
      return name;
   }

   public String getDescription() {
      return description;
   }

   public Method getGetter() {
      return getter;
   }

   public Method getSetter() {
      return setter;
   }

   public void setSetter(Method setter) {
      this.setter = setter;
   }

   public static String extractFieldName(String setterOrGetter) {
      String field = null;
      if (setterOrGetter.startsWith("set") || setterOrGetter.startsWith("get"))
         field = setterOrGetter.substring(3);
      else if (setterOrGetter.startsWith("is"))
         field = setterOrGetter.substring(2);

      if (field != null && field.length() > 1) {
         StringBuilder sb = new StringBuilder();
         sb.append(Character.toLowerCase(field.charAt(0)));
         if (field.length() > 2) sb.append(field.substring(1));
         return sb.toString();
      }
      return null;
   }
}
