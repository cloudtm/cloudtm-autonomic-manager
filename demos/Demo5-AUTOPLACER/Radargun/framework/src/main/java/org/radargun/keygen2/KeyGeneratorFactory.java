package org.radargun.keygen2;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * New Key Generator Factory that builds keys with the node index, thread index and a key index. 
 * Useful for the data placement algorithm
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class KeyGeneratorFactory {

   public static final String SEPARATOR = "_";

   private final Object calculateLock = new Object();

   private int numberOfNodes;
   private int numberOfThreads;
   private int numberOfKeys;
   private int valueSize;
   private int localityProbability;
   private double stdDev;
   private boolean noContention;
   private String bucketPrefix;

   private final AtomicReference<Workload> currentWorkload;

   public KeyGeneratorFactory() {
      this(1000, 1, 1, 1000, 0, false, "BUCKET");
   }

   private KeyGeneratorFactory(int numberOfKeys, int numberOfThreads, int numberOfNodes, int valueSize, int localityProbability,
                               boolean noContention, String bucketPrefix) {
      currentWorkload = new AtomicReference<Workload>();
      setBucketPrefix(bucketPrefix);
      setNumberOfKeys(numberOfKeys);
      setNumberOfNodes(numberOfNodes);
      setNumberOfThreads(numberOfThreads);
      setNoContention(noContention);
      setValueSize(valueSize);
      setLocalityProbability(localityProbability);
   }

   public void calculate() {
      synchronized (calculateLock) {
         int nodeIdx = numberOfKeys % numberOfNodes;
         int numberOfKeysPerNode = (numberOfKeys - nodeIdx) / numberOfNodes;
         int threadIdx = numberOfKeysPerNode % numberOfThreads;
         int numberOfKeysPerThread = (numberOfKeysPerNode - threadIdx) / numberOfThreads;

         if (localityProbability >= 0 && stdDev > 0) {
            throw new IllegalArgumentException("Std Dev and Locality Probability cannot be both higher or equals than 0");
         }

         currentWorkload.set(new Workload(numberOfKeys, numberOfNodes, numberOfThreads, numberOfKeysPerThread, nodeIdx, threadIdx,
                                          localityProbability, noContention, stdDev));
      }
   }

   public void setValueSize(int valueSize) {
      if (valueSize > 0) {
         this.valueSize = valueSize;
      }
   }

   public void setNumberOfNodes(int numberOfNodes) {
      if (numberOfNodes > 0) {
         this.numberOfNodes = numberOfNodes;
      }
   }

   public void setBucketPrefix(String bucketPrefix) {
      if (bucketPrefix != null) {
         this.bucketPrefix = bucketPrefix;
      }
   }

   public void setNumberOfThreads(int numberOfThreads) {
      if (numberOfThreads > 0) {
         this.numberOfThreads = numberOfThreads;
      }
   }

   public void setNumberOfKeys(int numberOfKeys) {
      if (numberOfKeys > 0) {
         this.numberOfKeys = numberOfKeys;
      }
   }

   public void setLocalityProbability(int localityProbability) {
      if (localityProbability <= 100) {
         this.localityProbability = localityProbability;
      }
   }

   public void setStdDev(double stdDev) {
      this.stdDev = stdDev;
   }

   public void setNoContention(boolean noContention) {
      this.noContention = noContention;
   }

   public KeyGenerator createKeyGenerator(int nodeIdx, int threadIdx) {
      return new InternalKeyGenerator(nodeIdx, threadIdx);
   }

   public Iterator<WarmupEntry> warmup(int nodeIdx) {
      return new InternalNodeIterator(nodeIdx, currentWorkload.get());
   }

   public Iterator<WarmupEntry> warmupAll() {
      return new InternalAllNodesIterator(currentWorkload.get());
   }

   public int getNumberOfThreads() {
      return numberOfThreads;
   }

   public boolean isNoContention() {
      return noContention;
   }

   public int getValueSize() {
      return valueSize;
   }

   public int getNumberOfKeys() {
      return numberOfKeys;
   }

   public int getLocalityProbability() {
      return localityProbability;
   }

   //debug only
   public Workload getCurrentWorkload() {
      return currentWorkload.get();
   }

   //debug only
   public Object convertIndexToKey(Workload workload, int index) {
      int nodeIndex = 0;
      int threadIndex = 0;
      int sumOfKeys = 0;

      while (true) {
         int keys = maxKeyIdx(workload, nodeIndex, threadIndex);

         sumOfKeys += keys;

         if (sumOfKeys > index) {
            sumOfKeys -= keys;
            break;
         }

         threadIndex++;
         if (threadIndex == workload.numberOfThreads) {
            threadIndex = 0;
            nodeIndex++;
         }
      }

      int keyIndex = index - sumOfKeys;
      return createKey(nodeIndex, threadIndex, keyIndex);
   }

   @Override
   public String toString() {
      return "KeyGeneratorFactory{" +
            "numberOfNodes=" + numberOfNodes +
            ", numberOfThreads=" + numberOfThreads +
            ", numberOfKeys=" + numberOfKeys +
            ", valueSize=" + valueSize +
            ", noContention=" + noContention +
            ", bucketPrefix='" + bucketPrefix + '\'' +
            '}';
   }

   private Object getRandomValue(Random random) {
      // each char is 2 bytes
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < valueSize / 2; i++) {
         sb.append((char) (64 + random.nextInt(26)));
      }
      return sb.toString();
   }

   private Object createKey(int nodeIdx, int threadIdx, int keyIdx) {
      return new RadargunKey(nodeIdx, threadIdx, keyIdx);
   }

   private int maxKeyIdx(Workload workload, int nodeIdx, int threadIdx) {
      return workload.keyPerThread + (nodeIdx < workload.nodeIdx ? 1 : 0) + (threadIdx < workload.threadIdx ? 1 : 0);
   }

   private String getBucket(int threadIdx) {
      return bucketPrefix + SEPARATOR + threadIdx;
   }

   private class InternalNodeIterator implements Iterator<WarmupEntry> {

      protected int nodeIdx;
      protected final Workload workload;
      protected int threadIdx = 0;
      protected int keyIdx = 0;
      protected int maxKeyIdx = 0;
      private final Random random;

      private InternalNodeIterator(int nodeIdx, Workload workload) {
         random = new Random(System.currentTimeMillis() >> nodeIdx);
         this.nodeIdx = nodeIdx;
         this.workload = workload;
         updateMaxKeyIndex();
      }

      @Override
      public boolean hasNext() {
         return nodeIdx < workload.numberOfNodes && threadIdx < workload.numberOfThreads && keyIdx < maxKeyIdx;
      }

      @Override
      public WarmupEntry next() {
         if (!hasNext()) {
            throw new NoSuchElementException();
         }
         WarmupEntry next = new WarmupEntry(getBucket(threadIdx),
                                            createKey(nodeIdx, threadIdx, keyIdx),
                                            getRandomValue(random));
         increment();
         return next;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }

      protected void updateMaxKeyIndex() {
         maxKeyIdx = maxKeyIdx(workload, nodeIdx, threadIdx);
      }

      protected void increment() {
         keyIdx = (keyIdx + 1) % maxKeyIdx;
         if (keyIdx == 0) {
            threadIdx++;
            updateMaxKeyIndex();
         }
      }
   }

   private class InternalAllNodesIterator extends InternalNodeIterator {

      private InternalAllNodesIterator(Workload workload) {
         super(0, workload);
      }

      @Override
      protected void increment() {
         keyIdx = (keyIdx + 1) % maxKeyIdx;
         if (keyIdx == 0) {
            threadIdx = (threadIdx + 1) % workload.numberOfThreads;
            if (threadIdx == 0) {
               nodeIdx++;
            }
            updateMaxKeyIndex();
         }
      }
   }

   private class InternalKeyGenerator implements KeyGenerator {

      private final int nodeIdx;
      private final int threadIdx;
      private final Random random;

      private InternalKeyGenerator(int nodeIdx, int threadIdx) {
         this.nodeIdx = nodeIdx;
         this.threadIdx = threadIdx;
         random = new Random(System.currentTimeMillis() ^ nodeIdx << threadIdx);
      }

      @Override
      public Object getRandomKey() {
         Workload workload = currentWorkload.get();

         boolean useLocalityProbability = workload.localityProbability >= 0;
         boolean useGaussian = workload.stdDev > 0;

         if (workload.noContention) {
            return noContentionKey(workload);
         } else if (!useGaussian && !useLocalityProbability) {
            return randomKey(workload);
         } else if (!useGaussian && useLocalityProbability) {
            return localityProbabilityKey(workload);
         } else {
            return gaussianKey(workload);
         }
      }

      @Override
      public Object[] getUniqueRandomKeys(int size) {
         Set<Object> keys = new HashSet<Object>();
         Workload workload = currentWorkload.get();
         if (workload.noContention) {
            for (int i = 0; i < size; ++i) {
               int keyIdx = random.nextInt(maxKeyIdx(workload, nodeIdx, threadIdx));

               if (!findNoContentedNextUnique(keys, workload, keyIdx)) {
                  break;
               }
            }
         } else {
            for (int i = 0; i < size; ++i) {
               int nodeIdx = random.nextInt(workload.numberOfNodes);
               int threadIdx = random.nextInt(workload.numberOfThreads);
               int keyIdx = random.nextInt(maxKeyIdx(workload, nodeIdx, threadIdx));

               if (!findNextUnique(keys, workload, nodeIdx, threadIdx, keyIdx)) {
                  break;
               }
            }
         }
         return keys.toArray();
      }

      @Override
      public String getBucket() {
         return KeyGeneratorFactory.this.getBucket(threadIdx);
      }

      @Override
      public Object getRandomValue() {
         return KeyGeneratorFactory.this.getRandomValue(random);
      }

      private Object noContentionKey(Workload workload) {
         int keyIdx = random.nextInt(maxKeyIdx(workload, nodeIdx, threadIdx));
         return createKey(nodeIdx, threadIdx, keyIdx);
      }

      private Object randomKey(Workload workload) {
         int nodeIdx = random.nextInt(workload.numberOfNodes);
         int threadIdx = random.nextInt(workload.numberOfThreads);
         int keyIdx = random.nextInt(maxKeyIdx(workload, nodeIdx, threadIdx));
         return createKey(nodeIdx, threadIdx, keyIdx);
      }

      private Object localityProbabilityKey(Workload workload) {
         int nodeIdx;
         if (workload.localityProbability > random.nextInt(100)) {
            nodeIdx = this.nodeIdx;
         } else {
            nodeIdx = random.nextInt(workload.numberOfNodes);
            nodeIdx = nodeIdx == this.nodeIdx ? (++nodeIdx % workload.numberOfNodes) : nodeIdx;
         }
         int threadIdx = random.nextInt(workload.numberOfThreads);
         int keyIdx = random.nextInt(maxKeyIdx(workload, nodeIdx, threadIdx));
         return createKey(nodeIdx, threadIdx, keyIdx);
      }

      private Object gaussianKey(Workload workload) {
         long keyIndex = getGaussKey(workload.numberOfKeys, workload.numberOfNodes, workload.stdDev);
         return convertIndexToKey(workload, (int) keyIndex);
      }

      private long getGaussKey(int numberOfKeys, int numberOfNodes, double stdDev){
         double gauss = random.nextGaussian();
         double avg = (((double)numberOfKeys) *
                             (((double)nodeIdx / ((double)numberOfNodes)) +
                                    0.5D / (double)numberOfNodes));
         long res = Math.round(Math.IEEEremainder(gauss * stdDev + avg, numberOfKeys));
         if(res < 0)
            return numberOfKeys + res;
         else
            return res;
      }

      private boolean findNextUnique(Set<Object> alreadyCollected, Workload workload, int initialNodeIdx, int initialThreadIdx,
                                     int initialKeyIndex) {
         int nodeIdx = initialNodeIdx;
         int threadIdx = initialThreadIdx;
         int maxKeyIdx = maxKeyIdx(workload, nodeIdx, threadIdx);
         int keyIdx = initialKeyIndex;

         do {
            if (alreadyCollected.add(createKey(nodeIdx, threadIdx, keyIdx))) {
               return true;
            }

            keyIdx = (keyIdx + 1) % maxKeyIdx;
            if (keyIdx == 0) {
               threadIdx = (threadIdx + 1) % workload.numberOfThreads;
               if (threadIdx == 0) {
                  nodeIdx = (nodeIdx + 1) % workload.numberOfNodes;
               }
               maxKeyIdx = maxKeyIdx(workload, nodeIdx, threadIdx);
            }
         } while (nodeIdx != initialNodeIdx || threadIdx != initialThreadIdx || keyIdx != initialKeyIndex);

         return false;
      }

      private boolean findNoContentedNextUnique(Set<Object> alreadyCollected, Workload workload, int initialKeyIndex) {
         int maxKeyIdx = maxKeyIdx(workload, nodeIdx, threadIdx);
         int keyIdx = initialKeyIndex;

         do {
            if (alreadyCollected.add(createKey(nodeIdx, threadIdx, keyIdx))) {
               return true;
            }

            keyIdx = (keyIdx + 1) % maxKeyIdx;
         } while (keyIdx != initialKeyIndex);

         return false;
      }

   }

   private class Workload {
      private final int numberOfKeys;
      private final int numberOfNodes;
      private final int numberOfThreads;
      private final int keyPerThread;
      private final int nodeIdx;
      private final int threadIdx;
      private final int localityProbability;
      private final boolean noContention;
      private final double stdDev;

      private Workload(int numberOfKeys, int numberOfNodes, int numberOfThreads, int keyPerThread, int nodeIdx, int threadIdx,
                       int localityProbability, boolean noContention, double stdDev) {
         this.numberOfKeys = numberOfKeys;
         this.numberOfNodes = numberOfNodes;
         this.numberOfThreads = numberOfThreads;
         this.keyPerThread = keyPerThread;
         this.nodeIdx = nodeIdx;
         this.threadIdx = threadIdx;
         this.localityProbability = localityProbability;
         this.noContention = noContention;
         this.stdDev = stdDev;
      }
   }
}
