package org.radargun.stressors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.CacheWrapper;
import org.radargun.CacheWrapperStressor;
import org.radargun.jmx.JmxRegistration;
import org.radargun.jmx.annotations.MBean;
import org.radargun.jmx.annotations.ManagedAttribute;
import org.radargun.jmx.annotations.ManagedOperation;
import org.radargun.keygen2.KeyGenerator;
import org.radargun.keygen2.KeyGeneratorFactory;
import org.radargun.utils.TransactionWorkload;
import org.radargun.utils.Utils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.radargun.utils.TransactionWorkload.Operation;
import static org.radargun.utils.TransactionWorkload.OperationIterator;
import static org.radargun.utils.Utils.convertNanosToMillis;

/**
 * On multiple threads executes put and get operations against the CacheWrapper, and returns the result as an Map.
 *
 * @author Mircea.Markus@jboss.com 
 * @author Pedro Ruivo
 * @since 1.0
 */
@MBean(objectName = "Benchmark", description = "Executes a defined workload over a cache")
public class PutGetStressor implements CacheWrapperStressor {

   private static Log log = LogFactory.getLog(PutGetStressor.class);

   private CacheWrapper cacheWrapper;

   private long startTime;

   private volatile CountDownLatch startPoint;

   private int nodeIndex = 0;

   private final KeyGeneratorFactory factory;

   private final Timer stopBenchmarkTimer = new Timer("stop-benchmark-timer");

   private final AtomicBoolean running = new AtomicBoolean(true);

   private final List<Stresser> stresserList = new LinkedList<Stresser>();

   private final TransactionWorkload transactionWorkload;


   //indicates that the coordinator execute or not txs -- PEDRO
   private boolean coordinatorParticipation = true;

   //simulation time (default: 30 seconds)
   private long simulationTime = 30L;

   public PutGetStressor(KeyGeneratorFactory factory) {
      this.factory = factory == null ? new KeyGeneratorFactory() : factory;
      transactionWorkload = new TransactionWorkload();
      JmxRegistration.getInstance().processStage(this);
   }

   @SuppressWarnings("UnusedDeclaration") //loaded dynamically
   public PutGetStressor() {
      this(new KeyGeneratorFactory());
   }

   public Map<String, String> stress(CacheWrapper wrapper) {
      this.cacheWrapper = wrapper;
      factory.calculate();

      startTime = System.currentTimeMillis();
      log.info("Executing: " + this.toString());

      try {
         log.warn("Resetting statistics before the PutGetStressors start executing");
         wrapper.resetAdditionalStats();
         executeOperations();
      } catch (Exception e) {
         log.warn("exception when stressing the cache wrapper", e);
         throw new RuntimeException(e);
      }
      return processResults();
   }

   public void destroy() throws Exception {
      cacheWrapper.empty();
      cacheWrapper = null;
   }

   private Map<String, String> processResults() {
      //total duration
      long totalDuration = 0;

      long commitFailedReadOnlyTxDuration = 0;
      int numberOfCommitFailedReadOnlyTx = 0;

      long commitFailedWriteTxDuration = 0;
      int numberOfCommitFailedWriteTx = 0;

      long execFailedReadOnlyTxDuration = 0;
      int numberOfExecFailedReadOnlyTx = 0;


      long execFailedWriteTxDuration = 0;
      int numberOfExecFailedWriteTx = 0;

      long readOnlyTxDuration = 0;
      int numberOfReadOnlyTx = 0;

      long writeTxDuration = 0;
      int numberOfWriteTx = 0;

      long readOnlyTxCommitSuccessDuration = 0;
      long writeTxCommitSuccessDuration = 0;

      long readOnlyTxCommitFailDuration = 0;
      long writeTxCommitFailDuration = 0;

      long readOnlyTxRollbackDuration = 0;
      long writeTxRollbackDuration = 0;

      for (Stresser stresser : stresserList) {
         totalDuration += stresser.delta;

         commitFailedReadOnlyTxDuration += stresser.commitFailedReadOnlyTxDuration;
         numberOfCommitFailedReadOnlyTx += stresser.numberOfCommitFailedReadOnlyTx;

         commitFailedWriteTxDuration += stresser.commitFailedWriteTxDuration;
         numberOfCommitFailedWriteTx += stresser.numberOfCommitFailedWriteTx;

         execFailedReadOnlyTxDuration += stresser.execFailedReadOnlyTxDuration;
         numberOfExecFailedReadOnlyTx += stresser.numberOfExecFailedReadOnlyTx;

         execFailedWriteTxDuration += stresser.execFailedWriteTxDuration;
         numberOfExecFailedWriteTx += stresser.numberOfExecFailedWriteTx;

         readOnlyTxDuration += stresser.readOnlyTxDuration;
         numberOfReadOnlyTx += stresser.numberOfReadOnlyTx;

         writeTxDuration += stresser.writeTxDuration;
         numberOfWriteTx += stresser.numberOfWriteTx;

         readOnlyTxCommitSuccessDuration += stresser.readOnlyTxCommitSuccessDuration;
         writeTxCommitSuccessDuration += stresser.writeTxCommitSuccessDuration;

         readOnlyTxCommitFailDuration += stresser.readOnlyTxCommitFailDuration;
         writeTxCommitFailDuration += stresser.writeTxCommitFailDuration;

         readOnlyTxRollbackDuration += stresser.readOnlyTxTollbackDuration;
         writeTxRollbackDuration += stresser.writeTxTollbackDuration;
      }

      Map<String, String> results = new LinkedHashMap<String, String>();
      int numOfThreads = stresserList.size();

      results.put("DURATION(msec)", str(convertNanosToMillis(totalDuration) / numOfThreads));
      results.put("TX_PER_SEC", str(calculateTxPerSec(numberOfReadOnlyTx + numberOfWriteTx,convertNanosToMillis(totalDuration), numOfThreads)));
      results.put("RO_TX_PER_SEC", str(calculateTxPerSec(numberOfReadOnlyTx, convertNanosToMillis(totalDuration), numOfThreads)));
      results.put("WRT_TX_SEC", str(calculateTxPerSec(numberOfWriteTx, convertNanosToMillis(totalDuration), numOfThreads)));

      results.put("WRT_TX_DUR(msec)", str(convertNanosToMillis(writeTxDuration / numOfThreads)));
      results.put("RO_TX_DUR(msec)", str(convertNanosToMillis(readOnlyTxDuration / numOfThreads)));

      if(numberOfReadOnlyTx != 0) {
         results.put("AVG_RO_OK_TX_DUR(msec)",str(convertNanosToMillis(readOnlyTxDuration / numOfThreads) / numberOfReadOnlyTx));
      } else {
         results.put("AVG_RO_OK_TX_DUR(msec)",str(0));
      }

      if(numberOfExecFailedReadOnlyTx != 0) {
         results.put("AVG_RO_EXEC_ERR_TX_DUR(msec)",str(convertNanosToMillis(execFailedReadOnlyTxDuration / numOfThreads) / numberOfExecFailedReadOnlyTx));
      } else {
         results.put("AVG_RO_EXEC_ERR_TX_DUR(msec)",str(0));
      }

      if(numberOfCommitFailedReadOnlyTx != 0) {
         results.put("AVG_RO_COMMIT_ERR_TX_DUR(msec)",str(convertNanosToMillis(commitFailedReadOnlyTxDuration / numOfThreads) / numberOfCommitFailedReadOnlyTx));
      } else {
         results.put("AVG_RO_COMMIT_ERR_TX_DUR(msec)",str(0));
      }

      if(numberOfWriteTx != 0) {
         results.put("AVG_WRT_OK_TX_DUR(msec)",str(convertNanosToMillis(writeTxDuration / numOfThreads) / numberOfWriteTx));
      } else {
         results.put("AVG_WRT_OK_TX_DUR(msec)",str(0));
      }

      if(numberOfExecFailedWriteTx != 0) {
         results.put("AVG_WRT_EXEC_ERR_TX_DUR(msec)",str(convertNanosToMillis(execFailedWriteTxDuration / numOfThreads) / numberOfExecFailedWriteTx));
      } else {
         results.put("AVG_WRT_EXEC_ERR_TX_DUR(msec)",str(0));
      }

      if(numberOfCommitFailedWriteTx != 0) {
         results.put("AVG_WRT_COMMIT_ERR_TX_DUR(sec)",str(convertNanosToMillis(commitFailedWriteTxDuration / numOfThreads) / numberOfCommitFailedWriteTx));
      } else {
         results.put("AVG_WRT_COMMIT_ERR_TX_DUR(sec)",str(0));
      }

      if(numberOfReadOnlyTx != 0) {
         results.put("AVG_OK_RO_COMMIT_DUR(msec)",str(convertNanosToMillis(readOnlyTxCommitSuccessDuration / numOfThreads) / numberOfReadOnlyTx));
      } else {
         results.put("AVG_OK_RO_COMMIT_DUR(msec)",str(0));
      }

      if(numberOfCommitFailedReadOnlyTx != 0) {
         results.put("AVG_ERR_RO_COMMIT_DUR(msec)",str(convertNanosToMillis(readOnlyTxCommitFailDuration / numOfThreads) / numberOfCommitFailedReadOnlyTx));
      } else {
         results.put("AVG_ERR_RO_COMMIT_DUR(msec)",str(0));
      }

      if(numberOfExecFailedReadOnlyTx != 0) {
         results.put("AVG_RO_ROLLBACK_DUR(msec)",str(convertNanosToMillis(readOnlyTxRollbackDuration / numOfThreads) / numberOfExecFailedReadOnlyTx));
      } else {
         results.put("AVG_RO_ROLLBACK_DUR(msec)",str(0));
      }

      if(numberOfWriteTx != 0) {
         results.put("AVG_OK_WRT_COMMIT_DUR(msec)",str(convertNanosToMillis(writeTxCommitSuccessDuration / numOfThreads) / numberOfWriteTx));
      } else {
         results.put("AVG_OK_WRT_COMMIT_DUR(msec)",str(0));
      }

      if(numberOfCommitFailedWriteTx != 0) {
         results.put("AVG_ERR_WRT_COMMIT_DUR(msec)",str(convertNanosToMillis(writeTxCommitFailDuration / numOfThreads) / numberOfCommitFailedWriteTx));
      } else {
         results.put("AVG_ERR_WRT_COMMIT_DUR(msec)",str(0));
      }

      if(numberOfExecFailedWriteTx != 0) {
         results.put("AVG_WRT_ROLLBACK_DUR(msec)",str(convertNanosToMillis(writeTxRollbackDuration / numOfThreads) / numberOfExecFailedWriteTx));
      } else {
         results.put("AVG_WRT_ROLLBACK_DUR(msec)",str(0));
      }

      results.put("RO_TX_COUNT", str(numberOfReadOnlyTx));
      results.put("RO_EXEC_ERR_TX_COUNT", str(numberOfExecFailedReadOnlyTx));
      results.put("RO_COMMIT_ERR_TX_COUNT", str(numberOfCommitFailedReadOnlyTx));
      results.put("WRT_TX_COUNT", str(numberOfWriteTx));
      results.put("WRT_EXEC_ERR_TX_COUNT", str(numberOfExecFailedWriteTx));
      results.put("WRT_COMMIT_ERR_TX_COUNT", str(numberOfCommitFailedWriteTx));

      int totalFailedTx = numberOfExecFailedReadOnlyTx + numberOfCommitFailedReadOnlyTx + numberOfExecFailedWriteTx +
            numberOfCommitFailedWriteTx;

      results.putAll(cacheWrapper.getAdditionalStats());

      log.info("Finished generating report. Nr of failed transactions on this node is: " + totalFailedTx +
                     ". Test duration is: " + Utils.getDurationString(System.currentTimeMillis() - startTime));

      return results;
   }

   private double calculateTxPerSec(int txCount, double txDuration, int numOfThreads) {
      if (txDuration <= 0) {
         return 0;
      }
      return txCount / ((txDuration / numOfThreads) / 1000.0);
   }

   private void executeOperations() throws Exception {
      startPoint = new CountDownLatch(1);

      for (int threadIndex = 0; threadIndex < factory.getNumberOfThreads(); threadIndex++) {
         Stresser stresser = new Stresser(threadIndex);
         stresserList.add(stresser);

         try{
            stresser.start();
         } catch (Throwable t){
            log.warn("Error starting all the stressers", t);
         }
      }

      log.info("Cache private class Stresser extends Thread { wrapper info is: " + cacheWrapper.getInfo());
      startPoint.countDown();
      stopBenchmarkTimer.schedule(new TimerTask() {
         @Override
         public void run() {
            finishBenchmark();
         }
      }, simulationTime * 1000);
      for (Stresser stresser : stresserList) {
         stresser.join();
         log.info("stresser[" + stresser.getName() + "] finished");
      }
      log.info("All stressers have finished their execution");


      /*BucketsKeysTreeSet bucketsKeysTreeSet = new BucketsKeysTreeSet();
    for(Stresser s : stresserList) {
       bucketsKeysTreeSet.addKeySet(s.keyGenerator.getBucket(), s.keyGenerator.getAllKeys());
    }
    cacheWrapper.saveKeysStressed(bucketsKeysTreeSet);
    log.info("Keys stressed saved");*/
   }

   private class Stresser extends Thread {

      private int threadIndex;
      private KeyGenerator keyGenerator;
      private final Random random;

      private long delta = 0;
      private long startTime = 0;

      //execution successful and commit successful
      private long readOnlyTxCommitSuccessDuration;
      private long writeTxCommitSuccessDuration;

      //execution successful but the commit fails
      private long readOnlyTxCommitFailDuration;
      private long writeTxCommitFailDuration;

      //execution failed
      private long readOnlyTxTollbackDuration;
      private long writeTxTollbackDuration;

      //exec: OK, commit: ERR
      private long commitFailedReadOnlyTxDuration;
      private int numberOfCommitFailedReadOnlyTx;

      //exec: OK, commit: ERR
      private long commitFailedWriteTxDuration;
      private int numberOfCommitFailedWriteTx;

      //exec: ERR
      private long execFailedReadOnlyTxDuration;
      private int numberOfExecFailedReadOnlyTx;

      //exec: ERR
      private long execFailedWriteTxDuration;
      private int numberOfExecFailedWriteTx;

      //exec: OK, commit: OK
      private long readOnlyTxDuration = 0;
      private int numberOfReadOnlyTx = 0;

      //exec: OK, commit: OK
      private long writeTxDuration = 0;
      private int numberOfWriteTx = 0;

      public Stresser(int threadIndex) {
         super("Stresser-" + threadIndex);
         this.threadIndex = threadIndex;
         this.random = new Random(System.nanoTime());
         this.keyGenerator = factory.createKeyGenerator(nodeIndex, threadIndex);
      }

      @Override
      public void run() {
         int i = 0;
         boolean executionSuccessful;
         boolean commitSuccessful;
         long startTx;
         long startCommit = 0;
         Object lastReadValue;

         try {
            startPoint.await();
            log.info("Starting thread: " + getName());
         } catch (InterruptedException e) {
            log.warn(e);
         }

         startTime = System.nanoTime();
         if(coordinatorParticipation || !cacheWrapper.isCoordinator()) {

            while(running.get()){

               OperationIterator operationIterator = transactionWorkload.chooseTransaction(cacheWrapper, random);

               startTx = System.nanoTime();

               cacheWrapper.startTransaction();
               log.trace("*** [" + getName() + "] new transaction: " + i + "***");

               try{
                  lastReadValue = executeTransaction(operationIterator);
                  executionSuccessful = true;
               } catch (TransactionExecutionFailedException e) {
                  lastReadValue = e.getLastValueRead();
                  logException(e, "Execution");
                  executionSuccessful = false;
               }

               try{
                  startCommit = System.nanoTime();
                  cacheWrapper.endTransaction(executionSuccessful);
                  commitSuccessful = true;
               }
               catch(Throwable e){
                  logException(e, "Commit");
                  commitSuccessful = false;
               }

               long endCommit = System.nanoTime();

               log.trace("*** [" + getName() + "] end transaction: " + i++ + "***");

               boolean readOnlyTransaction = operationIterator.isReadOnly();
               long commitDuration = endCommit - startCommit;
               long execDuration = endCommit - startTx;

               //update stats
               if(executionSuccessful) {
                  if (commitSuccessful) {
                     if (readOnlyTransaction) {
                        readOnlyTxCommitSuccessDuration += commitDuration;
                        readOnlyTxDuration += execDuration;
                        numberOfReadOnlyTx++;
                     } else {
                        writeTxCommitSuccessDuration += commitDuration;
                        writeTxDuration += execDuration;
                        numberOfWriteTx++;
                     }
                  } else {
                     if (readOnlyTransaction) {
                        readOnlyTxCommitFailDuration += commitDuration;
                        commitFailedReadOnlyTxDuration += execDuration;
                        numberOfCommitFailedReadOnlyTx++;
                     } else {
                        writeTxCommitFailDuration += commitDuration;
                        commitFailedWriteTxDuration += execDuration;
                        numberOfCommitFailedWriteTx++;
                     }
                  }
               } else {
                  //it is a rollback                  
                  if (readOnlyTransaction) {
                     readOnlyTxTollbackDuration += commitDuration;
                     execFailedReadOnlyTxDuration += execDuration;
                     numberOfExecFailedReadOnlyTx++;
                  } else {
                     writeTxTollbackDuration += commitDuration;
                     execFailedWriteTxDuration += execDuration;
                     numberOfExecFailedWriteTx++;
                  }
               }

               this.delta = System.nanoTime() - startTime;
               logProgress(i, lastReadValue);
            }
         } else {
            long sleepTime = simulationTime / 1000000; //nano to millis
            log.info("I am a coordinator and I wouldn't execute transactions. sleep for " + sleepTime + "(ms)");
            try {
               Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
               log.info("interrupted exception when sleeping (I'm coordinator and I don't execute transactions)");
            }
         }
      }

      private void logException(Throwable e, String where) {
         String msg = "[" + getName() + "] exception caught in " + where + ": " + e.getLocalizedMessage();
         if (log.isDebugEnabled()) {
            log.debug(msg, e);
         } else {
            log.warn(msg);
         }
      }

      private Object executeTransaction(OperationIterator operationIterator)
            throws TransactionExecutionFailedException {
         Object lastReadValue = null;

         while(operationIterator.hasNext()){
            Object key = keyGenerator.getRandomKey();

            if (operationIterator.isNextOperationARead()) {
               try {
                  lastReadValue = cacheWrapper.get(keyGenerator.getBucket(), key);
               } catch (Throwable e) {
                  TransactionExecutionFailedException tefe = new TransactionExecutionFailedException(
                        "Error while reading " + key, e);
                  tefe.setLastValueRead(lastReadValue);
                  throw tefe;
               }
            } else {
               Object payload = keyGenerator.getRandomValue();

               try {
                  cacheWrapper.put(keyGenerator.getBucket(), key, payload);
               } catch (Throwable e) {
                  TransactionExecutionFailedException tefe = new TransactionExecutionFailedException(
                        "Error while writing " + key, e);
                  tefe.setLastValueRead(lastReadValue);
                  throw tefe;
               }

            }
         }
         return lastReadValue;
      }

      private void logProgress(int i, Object result) {
         int opsCountStatusLog = 5000;
         if ((i + 1) % opsCountStatusLog == 0) {
            long elapsedTime = System.nanoTime() - startTime;
            //this is printed here just to make sure JIT doesn't
            // skip the call to cacheWrapper.get
            log.info("Thread index '" + threadIndex + "' executed " + (i + 1) + " transactions. Elapsed time: " +
                           Utils.getDurationString((long) convertNanosToMillis(elapsedTime)) +
                           ". Last value read is " + result);
         }
      }
   }

   private String str(Object o) {
      return String.valueOf(o);
   }

   private void finishBenchmark() {
      running.set(false);
   }

   @ManagedOperation
   public void stopBenchmark() {
      stopBenchmarkTimer.cancel();
      running.set(false);
   }

   @Override
   public String toString() {
      return "PutGetStressor{" +
            "keyGeneratorFactory=" + factory +
            "transactionWorkload=" + transactionWorkload +
            ", coordinatorParticipation=" + coordinatorParticipation +
            ", simulationTime=" + simulationTime +
            ", cacheWrapper=" + cacheWrapper.getInfo() +
            "}";
   }

   /*
   * -----------------------------------------------------------------------------------
   * SETTERS
   * -----------------------------------------------------------------------------------
   */

   @ManagedOperation
   public void setWriteTxWorkload(String writeTxWorkload) {
      transactionWorkload.writeTx(writeTxWorkload);
   }

   @ManagedAttribute
   public String getWriteTxWorkload() {
      Map<Operation, Integer> bounds = transactionWorkload.getOperationBounds();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(bounds.get(Operation.WRITE_TX_LOWER_BOUND_READ))
            .append(":")
            .append(bounds.get(Operation.WRITE_TX_UPPER_BOUND_READ));
      stringBuilder.append(";");
      stringBuilder.append(bounds.get(Operation.WRITE_TX_LOWER_BOUND_WRITE))
            .append(":")
            .append(bounds.get(Operation.WRITE_TX_UPPER_BOUND_WRITE));
      return stringBuilder.toString();
   }

   @ManagedOperation
   public void setReadTxWorkload(String readTxWorkload) {
      transactionWorkload.readTx(readTxWorkload);
   }

   @ManagedAttribute
   public String getReadTxWorkload() {
      Map<Operation, Integer> bounds = transactionWorkload.getOperationBounds();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(bounds.get(Operation.READ_TX_LOWER_BOUND))
            .append(":")
            .append(bounds.get(Operation.READ_TX_UPPER_BOUND));
      return stringBuilder.toString();
   }

   public void setNodeIndex(int nodeIndex) {
      this.nodeIndex = nodeIndex;
   }

   public void setNumberOfNodes(int numberOfNodes) {
      factory.setNumberOfNodes(numberOfNodes);
   }

   //NOTE this time is in seconds!
   public void setSimulationTime(long simulationTime) {
      this.simulationTime = simulationTime;
   }

   @ManagedOperation
   public void setNoContention(boolean noContention) {
      factory.setNoContention(noContention);
   }

   @ManagedAttribute
   public boolean isNoContention() {
      return factory.isNoContention();
   }

   public void setBucketPrefix(String bucketPrefix) {
      factory.setBucketPrefix(bucketPrefix);
   }

   @ManagedOperation
   public void setNumberOfKeys(int numberOfKeys) {
      factory.setNumberOfKeys(numberOfKeys);
   }

   @ManagedAttribute
   public int getNumberOfKeys() {
      return factory.getNumberOfKeys();
   }

   @ManagedOperation
   public void setSizeOfValue(int sizeOfValue) {
      factory.setValueSize(sizeOfValue);
   }

   @ManagedAttribute
   public int getSizeOfValue() {
      return factory.getValueSize();
   }

   @ManagedOperation
   public void setNumberOfThreads(int numOfThreads) {
      factory.setNumberOfThreads(numOfThreads);
   }

   @ManagedAttribute
   public int getNumberOfThreads() {
      return factory.getNumberOfThreads();
   }

   public void setCoordinatorParticipation(boolean coordinatorParticipation) {
      this.coordinatorParticipation = coordinatorParticipation;
   }

   @ManagedOperation
   public void setWriteTxPercentage(int writeTransactionPercentage) {
      transactionWorkload.setWriteTxPercentage(writeTransactionPercentage);
   }

   @ManagedAttribute
   public int getWriteTxPercentage() {
      return transactionWorkload.getWriteTxPercentage();
   }

   @ManagedOperation
   public void changeKeysWorkload() {
      factory.calculate();
   }
   
   @ManagedOperation
   public void setLocalityProbability(int localityProbability) {
      factory.setLocalityProbability(localityProbability);
   }
   
   @ManagedAttribute
   public int getLocalityProbability() {
      return factory.getLocalityProbability();
   }

   @ManagedOperation
   public void setStdDev(double stdDev) {
      factory.setStdDev(stdDev);
   }
}

