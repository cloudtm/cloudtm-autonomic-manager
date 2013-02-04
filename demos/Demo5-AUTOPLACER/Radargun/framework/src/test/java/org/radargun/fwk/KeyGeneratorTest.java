package org.radargun.fwk;

import org.radargun.keygen2.KeyGenerator;
import org.radargun.keygen2.KeyGeneratorFactory;
import org.radargun.keygen2.RadargunKey;
import org.radargun.keygen2.WarmupEntry;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Simple test to key generator
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
@Test
public class KeyGeneratorTest {

   public void testSingleMastWarmup() {
      int numberOfNodes = 10;
      int numberOfThreads = 8;
      int numberOfKeys = 1000;

      KeyGeneratorFactory factory = new KeyGeneratorFactory();
      factory.setNoContention(false);
      factory.setNumberOfKeys(numberOfKeys);
      factory.setNumberOfNodes(numberOfNodes);
      factory.setNumberOfThreads(numberOfThreads);
      factory.calculate();

      Iterator<WarmupEntry> iterator = factory.warmupAll();

      for (int nodeIdx = 0; nodeIdx < numberOfNodes; ++nodeIdx) {
         int threadIdx = 0;
         while (threadIdx < 4) {
            assertIterator(iterator, nodeIdx, threadIdx++, 13);
         }
         while (threadIdx < 8) {
            assertIterator(iterator, nodeIdx, threadIdx++, 12);
         }
      }
   }

   public void testMultiMasterWarmup() {
      int numberOfNodes = 10;
      int numberOfThreads = 8;
      int numberOfKeys = 1000;

      KeyGeneratorFactory factory = new KeyGeneratorFactory();
      factory.setNoContention(false);
      factory.setNumberOfKeys(numberOfKeys);
      factory.setNumberOfNodes(numberOfNodes);
      factory.setNumberOfThreads(numberOfThreads);
      factory.calculate();

      for (int nodeIdx = 0; nodeIdx < numberOfNodes; ++nodeIdx) {
         Iterator<WarmupEntry> iterator = factory.warmup(nodeIdx);

         int threadIdx = 0;
         while (threadIdx < 4) {
            assertIterator(iterator, nodeIdx, threadIdx++, 13);
         }
         while (threadIdx < 8) {
            assertIterator(iterator, nodeIdx, threadIdx++, 12);
         }
      }
   }

   public void testNoContention() {
      int numberOfNodes = 10;
      int numberOfThreads = 8;
      int numberOfKeys = 1000;

      KeyGeneratorFactory factory = new KeyGeneratorFactory();
      factory.setNoContention(true);
      factory.setNumberOfKeys(numberOfKeys);
      factory.setNumberOfNodes(numberOfNodes);
      factory.setNumberOfThreads(numberOfThreads);
      factory.calculate();

      for (int nodeIdx = 0; nodeIdx < numberOfNodes; ++nodeIdx) {
         int threadIdx = 0;
         while (threadIdx < 4) {
            KeyGenerator keyGenerator = factory.createKeyGenerator(nodeIdx, threadIdx);
            assertKeys(keyGenerator.getUniqueRandomKeys(numberOfKeys), nodeIdx, threadIdx++, 13);
         }
         while (threadIdx < 8) {
            KeyGenerator keyGenerator = factory.createKeyGenerator(nodeIdx, threadIdx);
            assertKeys(keyGenerator.getUniqueRandomKeys(numberOfKeys), nodeIdx, threadIdx++, 12);
         }
      }
   }

   public void testContention() {
      int numberOfNodes = 10;
      int numberOfThreads = 8;
      int numberOfKeys = 1000;

      KeyGeneratorFactory factory = new KeyGeneratorFactory();
      factory.setNoContention(false);
      factory.setNumberOfKeys(numberOfKeys);
      factory.setNumberOfNodes(numberOfNodes);
      factory.setNumberOfThreads(numberOfThreads);
      factory.calculate();

      for (int nodeIdx = 0; nodeIdx < numberOfNodes; ++nodeIdx) {
         for (int threadIdx = 0; threadIdx < numberOfThreads; ++threadIdx) {
            KeyGenerator keyGenerator = factory.createKeyGenerator(nodeIdx, threadIdx);
            Object[] keys = keyGenerator.getUniqueRandomKeys(numberOfKeys);
            assert keys.length == numberOfKeys : "expected " + numberOfKeys + " keys but it has " + keys.length;

            for (int tmpNodeIdx = 0; tmpNodeIdx < numberOfNodes; ++tmpNodeIdx) {
               int tmpThreadIdx = 0;
               while (tmpThreadIdx < 4) {
                  assertKeys(keys, tmpNodeIdx, tmpThreadIdx++, 13);
               }
               while (tmpThreadIdx < 8) {
                  assertKeys(keys, tmpNodeIdx, tmpThreadIdx++, 12);
               }
            }
         }
      }
   }

   public void testConverter() {

      for (int numberOfNodes = 2; numberOfNodes < 10; ++numberOfNodes) {
         for (int numberOfThreads = 1; numberOfThreads < 8; ++numberOfThreads) {
            for (int numberOfKeys = 1000; numberOfKeys < 10000000; numberOfKeys *= 10) {
               KeyGeneratorFactory factory = new KeyGeneratorFactory();
               factory.setNoContention(false);
               factory.setNumberOfKeys(numberOfKeys);
               factory.setNumberOfNodes(numberOfNodes);
               factory.setNumberOfThreads(numberOfThreads);
               factory.calculate();

               Iterator<WarmupEntry> iterator = factory.warmupAll();
               int index = 0;
               while (iterator.hasNext()) {
                  WarmupEntry entry = iterator.next();
                  Object key = factory.convertIndexToKey(factory.getCurrentWorkload(), index++);
                  assert entry.getKey().equals(key) : "Wrong key: " + entry.getKey() + " != " + key;
               }
               //assert index == numberOfKeys;
               System.out.println(String.format("Nodes=%s,Threads=%s,Keys=%s done. last index=%s",
                                                numberOfNodes, numberOfThreads, numberOfKeys, index));
            }
         }
      }

      for (int numberOfNodes = 10; numberOfNodes < 100; numberOfNodes += 10) {
         for (int numberOfThreads = 1; numberOfThreads < 8; ++numberOfThreads) {
            for (int numberOfKeys = 1000; numberOfKeys < 10000000; numberOfKeys *= 10) {
               KeyGeneratorFactory factory = new KeyGeneratorFactory();
               factory.setNoContention(false);
               factory.setNumberOfKeys(numberOfKeys);
               factory.setNumberOfNodes(numberOfNodes);
               factory.setNumberOfThreads(numberOfThreads);
               factory.calculate();

               Iterator<WarmupEntry> iterator = factory.warmupAll();
               int index = 0;
               while (iterator.hasNext()) {
                  WarmupEntry entry = iterator.next();
                  Object key = factory.convertIndexToKey(factory.getCurrentWorkload(), index++);
                  assert entry.getKey().equals(key) : "Wrong key: " + entry.getKey() + " != " + key;
               }
               //assert index == numberOfKeys;
               System.out.println(String.format("Nodes=%s,Threads=%s,Keys=%s done. last index=%s",
                                                numberOfNodes, numberOfThreads, numberOfKeys, index));
            }
         }
      }
   }

   private void assertKeys(Object[] uniqueRandomKeys, int nodeIdx, int threadIdx, int maxKeys) {
      Set<Object> keys = new HashSet<Object>(Arrays.asList(uniqueRandomKeys));

      for (int i = 0; i < maxKeys; ++i) {
         RadargunKey key = new RadargunKey(nodeIdx, threadIdx, i);
         assert keys.contains(key) : "keys not contains key " + key;
      }
   }

   private void assertIterator(Iterator<WarmupEntry> iterator, int nodeIdx, int threadIdx, int maxKeys) {
      for (int i = 0; i < maxKeys; ++i) {
         assert iterator.hasNext();
         assert new RadargunKey(nodeIdx, threadIdx, i).equals(iterator.next().getKey());
      }
   }
}
