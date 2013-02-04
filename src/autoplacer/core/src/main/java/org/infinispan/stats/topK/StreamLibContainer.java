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
package org.infinispan.stats.topK;

import com.clearspring.analytics.stream.Counter;
import com.clearspring.analytics.stream.StreamSummary;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This contains all the stream lib top keys. Stream lib is a space efficient technique to obtains the top-most
 * counters.
 *
 * @author Pedro Ruivo
 * @since 5.2
 */
public class StreamLibContainer {

   private static final StreamLibContainer instance = new StreamLibContainer();
   public static final int MAX_CAPACITY = 20000;

   private int capacity = 100;
   private boolean active = false;

   private final Map<Stat, StreamSummary<Object>> streamSummaryEnumMap;
   private final Map<Stat, Lock> lockMap;

   public static enum Stat {
      REMOTE_GET,
      LOCAL_GET,
      REMOTE_PUT,
      LOCAL_PUT,

      MOST_LOCKED_KEYS,
      MOST_CONTENDED_KEYS,
      MOST_FAILED_KEYS,
      MOST_WRITE_SKEW_FAILED_KEYS
   }

   private StreamLibContainer() {
      streamSummaryEnumMap = Collections.synchronizedMap(new EnumMap<Stat, StreamSummary<Object>>(Stat.class));
      lockMap = new EnumMap<Stat, Lock>(Stat.class);

      for (Stat stat : Stat.values()) {
         lockMap.put(stat, new ReentrantLock());
      }

      clearAll();
      setActive(false);
   }

   public static StreamLibContainer getInstance() {
      return instance;
   }

   public boolean isActive() {
      return active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public void setCapacity(int capacity) {
      if(capacity <= 0) {
         this.capacity = 1;
      } else {
         this.capacity = capacity;
      }
   }

   public int getCapacity(){
	   return capacity;
   }

   public void addGet(Object key, boolean remote) {
      if(!isActive()) {
         return;
      }
      syncOffer(remote ? Stat.REMOTE_GET : Stat.LOCAL_GET, key);
   }

   public void addPut(Object key, boolean remote) {
      if(!isActive()) {
         return;
      }

      syncOffer(remote ? Stat.REMOTE_PUT : Stat.LOCAL_PUT, key);
   }

   public void addLockInformation(Object key, boolean contention, boolean abort) {
      if(!isActive()) {
         return;
      }

      syncOffer(Stat.MOST_LOCKED_KEYS, key);

      if(contention) {
         syncOffer(Stat.MOST_CONTENDED_KEYS, key);
      }
      if(abort) {
         syncOffer(Stat.MOST_FAILED_KEYS, key);
      }
   }

   public void addWriteSkewFailed(Object key) {
      syncOffer(Stat.MOST_WRITE_SKEW_FAILED_KEYS, key);
   }

   public Map<Object, Long> getTopKFrom(Stat stat) {
      return getTopKFrom(stat, capacity);
   }

   public Map<Object, Long> getTopKFrom(Stat stat, int topK) {
      try {
         lockMap.get(stat).lock();
         return getStatsFrom(streamSummaryEnumMap.get(stat), topK);
      } finally {
         lockMap.get(stat).unlock();
      }

   }

   private Map<Object, Long> getStatsFrom(StreamSummary<Object> ss, int topK) {
      List<Counter<Object>> counters = ss.topK(topK <= 0 ? 1 : topK);
      Map<Object, Long> results = new HashMap<Object, Long>(topK);

      for(Counter<Object> c : counters) {
         results.put(c.getItem(), c.getCount());
      }

      return results;
   }

   public void resetAll(){
      clearAll();
   }

   public void resetStat(Stat stat){
      try {
         lockMap.get(stat).lock();
         streamSummaryEnumMap.put(stat, createNewStreamSummary());
      } finally {
         lockMap.get(stat).unlock();
      }
   }

   private StreamSummary<Object> createNewStreamSummary() {
      return new StreamSummary<Object>(Math.max(MAX_CAPACITY, capacity));
   }

   private void clearAll() {
      for (Stat stat : Stat.values()) {
         resetStat(stat);
      }
   }

   private void syncOffer(final Stat stat, Object key) {
      try {
         lockMap.get(stat).lock();
         streamSummaryEnumMap.get(stat).offer(key);
      } finally {
         lockMap.get(stat).unlock();
      }
   }
}
