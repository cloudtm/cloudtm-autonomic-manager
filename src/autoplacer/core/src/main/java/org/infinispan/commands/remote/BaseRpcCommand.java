/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
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
package org.infinispan.commands.remote;

import org.infinispan.remoting.responses.ExceptionResponse;
import org.infinispan.remoting.responses.ResponseGenerator;
import org.infinispan.remoting.transport.Address;
import org.jgroups.blocks.MessageRequest;

public abstract class BaseRpcCommand implements CacheRpcCommand {
   protected final String cacheName;

   private Address origin;
   private MessageRequest messageRequest;
   private ResponseGenerator responseGenerator;

   protected BaseRpcCommand(String cacheName) {
      this.cacheName = cacheName;
   }

   @Override
   public String getCacheName() {
      return cacheName;
   }

   @Override
   public String toString() {
      return "BaseRpcCommand{" +
            "cacheName='" + cacheName + '\'' +
            '}';
   }

   @Override
   public Address getOrigin() {
	   return origin;
   }

   @Override
   public void setOrigin(Address origin) {
	   this.origin = origin;
   }

   @Override
   public final void setMessageRequest(MessageRequest request) {
      this.messageRequest = request;
   }

   @Override
   public final void setResponseGenerator(ResponseGenerator responseGenerator) {
      this.responseGenerator = responseGenerator;
   }

   @Override
   public final void sendReply(Object reply, boolean threwException) {
      sendReply(messageRequest, responseGenerator, this, reply, threwException);
   }

   public static void sendReply(MessageRequest messageRequest, ResponseGenerator responseGenerator,
                                CacheRpcCommand command, Object reply, boolean threwException) {
      if (messageRequest == null) {
         return;
      }
      Object realReply;
      if (threwException) {
         realReply = new ExceptionResponse((Exception) reply);
      } else if (responseGenerator == null) {
         realReply = reply;
      } else {
         realReply = responseGenerator.getResponse(command, reply);
      }
      messageRequest.sendReply(realReply, false);
   }
}
