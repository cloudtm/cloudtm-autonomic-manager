package org.radargun.utils;

import org.radargun.CacheWrapper;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Keeps information about the transaction workload, i.e, the percentage of read-write transactions and the bounds
 * of the transactions operations
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class TransactionWorkload {

   private static final int NOT_VALID = -1;

   private final Object writeTxWritesLock = new Object();
   private final Object writeTxReadsLock = new Object();
   private final Object readTxLock = new Object();

   private int writeTxPercentage;

   private final EnumMap<Operation, Integer> operationBounds;

   public static enum Operation {
      WRITE_TX_LOWER_BOUND_WRITE,
      WRITE_TX_UPPER_BOUND_WRITE,
      WRITE_TX_LOWER_BOUND_READ,
      WRITE_TX_UPPER_BOUND_READ,
      READ_TX_LOWER_BOUND,
      READ_TX_UPPER_BOUND
   }

   private static final ParserPattern[] writePatterns = new ParserPattern[] {
         new ParserPattern("(\\d+),(\\d+);(\\d+),(\\d+)", new int[] {3,4}, new int[] {1,2}),
         new ParserPattern("(\\d+);(\\d+),(\\d+)", new int[] {2,3}, new int[] {1,1}),
         new ParserPattern("(\\d+),(\\d+);(\\d+)", new int[] {3,3}, new int[] {1,2}),
         new ParserPattern("(\\d+);(\\d+)", new int[] {2,2}, new int[] {1,1}),
         new ParserPattern("(\\d+),(\\d+);", null, new int[] {1,2}),
         new ParserPattern(";(\\d+),(\\d+)", new int[] {1, 2}, null),
         new ParserPattern("(\\d+);", null, new int[] {1,1}),
         new ParserPattern(";(\\d+)", new int[] {1, 1}, null)
   };

   private static final ParserPattern[] readPatterns = new ParserPattern[] {
         new ParserPattern("(\\d+),(\\d+)", null, new int[] {1,2}),
         new ParserPattern("(\\d+)", null, new int[] {1,1}),
   };

   public TransactionWorkload() {
      this(50);
   }

   public TransactionWorkload(int defaultValue) {
      operationBounds = new EnumMap<Operation, Integer>(Operation.class);
      for (Operation operation : Operation.values()) {
         operationBounds.put(operation, defaultValue);
      }
   }

   public final OperationIterator chooseTransaction(CacheWrapper cacheWrapper, Random random) {
      boolean readOnly = cacheWrapper.canExecuteReadOnlyTransactions() &&
            (!cacheWrapper.canExecuteWriteTransactions() || random.nextInt(100) >= writeTxPercentage);
      if (readOnly) {
         return new OperationIterator(true, random, readTxReads(random), 0);
      } else {
         return new OperationIterator(false, random, writeTxReads(random), writeTxWrites(random));
      }
   }

   public final void writeTx(String writeTxWorkload) {
      EnumMap<Operation, Integer> updates = new EnumMap<Operation, Integer>(Operation.class);
      boolean needUpdateRead = false, needUpdateWrite = false;

      for (ParserPattern parserPattern : writePatterns) {
         Matcher matcher = parserPattern.pattern.matcher(writeTxWorkload);
         if (matcher.matches()) {
            needUpdateRead = parseWriteTxReads(matcher, updates, parserPattern);
            needUpdateWrite = parseWriteTxWrites(matcher, updates, parserPattern);
            break;
         }
      }

      if (needUpdateRead && needUpdateWrite) {
         synchronized (writeTxReadsLock) {
            synchronized (writeTxWritesLock) {
               operationBounds.putAll(updates);
            }
         }
      } else if (needUpdateRead && !needUpdateWrite) {
         synchronized (writeTxReadsLock) {
            operationBounds.putAll(updates);
         }
      } else if (needUpdateWrite) {
         synchronized (writeTxWritesLock) {
            operationBounds.putAll(updates);
         }
      }
   }
   
   public final Map<Operation, Integer> getOperationBounds() {
      return Collections.unmodifiableMap(operationBounds);
   }
   
   public final int getWriteTxPercentage() {
      return writeTxPercentage;
   }   

   public final void readTx(String readTxWorkload) {
      EnumMap<Operation, Integer> updates = new EnumMap<Operation, Integer>(Operation.class);
      boolean needUpdateRead = false;

      for (ParserPattern parserPattern : readPatterns) {
         Matcher matcher = parserPattern.pattern.matcher(readTxWorkload);
         if (matcher.matches()) {
            needUpdateRead = parseReadTxReads(matcher, updates, parserPattern);
            break;
         }
      }

      if (needUpdateRead) {
         synchronized (readTxLock) {
            operationBounds.putAll(updates);
         }
      }
   }

   public final void setWriteTxPercentage(int writeTxPercentage) {
      this.writeTxPercentage = writeTxPercentage;
   }

   public final int readTxReads(Random random) {
      synchronized (readTxLock) {
         return operations(operationBounds.get(Operation.READ_TX_LOWER_BOUND),
                           operationBounds.get(Operation.READ_TX_UPPER_BOUND),
                           random);
      }
   }

   public final int writeTxWrites(Random random) {
      synchronized (writeTxWritesLock) {
         return operations(operationBounds.get(Operation.WRITE_TX_LOWER_BOUND_WRITE),
                           operationBounds.get(Operation.WRITE_TX_UPPER_BOUND_WRITE),
                           random);
      }
   }

   public final int writeTxReads(Random random) {
      synchronized (writeTxReadsLock) {
         return operations(operationBounds.get(Operation.WRITE_TX_LOWER_BOUND_READ),
                           operationBounds.get(Operation.WRITE_TX_UPPER_BOUND_READ),
                           random);
      }
   }

   @Override
   public String toString() {
      return "TransactionWorkload{" +
            "writeTxPercentage=" + writeTxPercentage +
            ", operationBounds=" + operationBounds +
            '}';
   }

   private boolean parseReadTxReads(Matcher matcher, EnumMap<Operation, Integer> updates, ParserPattern parserPattern) {
      if (parserPattern.readPositions == null) {
         return false;
      }
      int value1 = parseInt(matcher.group(parserPattern.readPositions[0]));
      int value2 = parseInt(matcher.group(parserPattern.readPositions[1]));

      boolean needUpdate = value1 != NOT_VALID && value2 != NOT_VALID;
      if (needUpdate) {
         updates.put(Operation.READ_TX_LOWER_BOUND, Math.min(value1, value2));
         updates.put(Operation.READ_TX_UPPER_BOUND, Math.max(value1, value2));
      }
      return needUpdate;
   }

   private boolean parseWriteTxWrites(Matcher matcher, EnumMap<Operation, Integer> updates, ParserPattern parserPattern) {
      if (parserPattern.writePositions == null) {
         return false;
      }
      int value1 = parseInt(matcher.group(parserPattern.writePositions[0]));
      int value2 = parseInt(matcher.group(parserPattern.writePositions[1]));

      boolean needUpdate = value1 != NOT_VALID && value2 != NOT_VALID;
      if (needUpdate) {
         updates.put(Operation.WRITE_TX_LOWER_BOUND_WRITE, Math.min(value1, value2));
         updates.put(Operation.WRITE_TX_UPPER_BOUND_WRITE, Math.max(value1, value2));
      }
      return needUpdate;
   }

   private boolean parseWriteTxReads(Matcher matcher, EnumMap<Operation, Integer> updates, ParserPattern parserPattern) {
      if (parserPattern.readPositions == null) {
         return false;
      }
      int value1 = parseInt(matcher.group(parserPattern.readPositions[0]));
      int value2 = parseInt(matcher.group(parserPattern.readPositions[1]));

      boolean needUpdate = value1 != NOT_VALID && value2 != NOT_VALID;
      if (needUpdate) {
         updates.put(Operation.WRITE_TX_LOWER_BOUND_READ, Math.min(value1, value2));
         updates.put(Operation.WRITE_TX_UPPER_BOUND_READ, Math.max(value1, value2));
      }
      return needUpdate;
   }

   private int parseInt(String number) {
      try {
         int val =  Integer.parseInt(number);
         return val <= 0 ? NOT_VALID : val;
      } catch (Exception e) {
         return NOT_VALID;
      }
   }

   private int operations(int lowerBound, int upperBound, Random random) {
      return lowerBound == upperBound ? lowerBound : random.nextInt(upperBound - lowerBound + 1) + lowerBound;
   }

   public static class OperationIterator {
      private final boolean isReadOnly;
      private final Random random;

      private int writeOperations;
      private int readOperations;

      private OperationIterator(boolean readOnly, Random random, int readOperations,
                                int writeOperations) {
         isReadOnly = readOnly;
         this.random = random;
         this.writeOperations = writeOperations;
         this.readOperations = readOperations;
         if (!readOnly && writeOperations == 0) {
            this.writeOperations = 1;
         }
      }

      public final boolean hasNext() {
         return writeOperations > 0 || readOperations > 0;
      }

      public final boolean isNextOperationARead() {
         boolean canBeARead = readOperations > 0;
         boolean canBeAWrite = writeOperations > 0;

         if (canBeARead && !canBeAWrite) {
            readOperations--;
            return true;
         } else if (!canBeARead && canBeAWrite) {
            writeOperations--;
            return false;
         }

         if (random.nextInt(100) >= 50) {
            readOperations--;
            return true;
         } else {
            writeOperations--;
            return false;
         }
      }

      public final boolean isReadOnly() {
         return isReadOnly;
      }
   }

   private static class ParserPattern {
      private final Pattern pattern;
      private final int[] writePositions;
      private final int[] readPositions;

      public ParserPattern(String pattern, int[] writePositions, int[] readPositions) {
         this.pattern = Pattern.compile(pattern);
         this.writePositions = writePositions;
         this.readPositions = readPositions;
      }
   }
}
