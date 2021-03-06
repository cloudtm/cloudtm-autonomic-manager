/*
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
package org.infinispan.remoting.responses;

import org.infinispan.remoting.rpc.ResponseFilter;
import org.infinispan.remoting.transport.Address;

import java.util.Collection;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 5.2
 */
public class KeyValidationFilter implements ResponseFilter {

   private final Collection<Object> keysNeededValidation;
   private final Address localAddress;
   private boolean exception;
   private boolean selfDelivered;

   public KeyValidationFilter(Collection<Object> keysNeededValidation, Address localAddress) {
      this.keysNeededValidation = keysNeededValidation;
      this.localAddress = localAddress;
      this.exception = false;
      this.selfDelivered = false;
   }


   @Override
   public boolean isAcceptable(Response response, Address sender) {
      if (response instanceof ExceptionResponse) {
         exception = true;
      } else if (response instanceof SuccessfulResponse) {
         Object retVal = ((SuccessfulResponse) response).getResponseValue();
         if (retVal instanceof Collection<?>) {
            keysNeededValidation.removeAll((Collection<?>) retVal);
         }
      } else if (localAddress.equals(sender)) {
         selfDelivered = true;
      }
      return true;
   }

   @Override
   public boolean needMoreResponses() {
      return !selfDelivered || (!keysNeededValidation.isEmpty() && !exception);
   }
}
