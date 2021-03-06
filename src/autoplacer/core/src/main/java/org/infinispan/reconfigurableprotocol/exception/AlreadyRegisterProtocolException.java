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
package org.infinispan.reconfigurableprotocol.exception;

import org.infinispan.reconfigurableprotocol.ReconfigurableProtocol;

/**
 * Exception that is thrown when it tries to register a already register protocol
 *
 * @author Pedro Ruivo
 * @since 5.2
 */
public class AlreadyRegisterProtocolException extends Exception {

   public AlreadyRegisterProtocolException() {
   }

   public AlreadyRegisterProtocolException(String s) {
      super(s);
   }

   public AlreadyRegisterProtocolException(String s, Throwable throwable) {
      super(s, throwable);
   }

   public AlreadyRegisterProtocolException(Throwable throwable) {
      super(throwable);
   }

   public AlreadyRegisterProtocolException(ReconfigurableProtocol protocol) {
      super("The protocol " + protocol + " is already register");
   }
}
