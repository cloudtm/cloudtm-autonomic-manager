package org.radargun.stages;

import org.radargun.CacheWrapper;
import org.radargun.DistStageAck;
import org.radargun.keygen2.KeyGeneratorFactory;
import org.radargun.state.MasterState;
import org.radargun.stressors.PutGetStressor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Double.parseDouble;
import static org.radargun.utils.Utils.numberFormat;

/**
 * Simulates the work with a distributed web sessions.
 *
 * @author Mircea.Markus@jboss.com
 */
public class WebSessionBenchmarkStage extends AbstractDistStage {

   private static final String SCRIPT_LAUNCH = "_script_launch_";
   private static final String SCRIPT_PATH = System.getProperty("user.home") + "/beforeBenchmark.sh";

   //for each session there will be created fixed number of attributes. On those attributes all the GETs and PUTs are
   // performed (for PUT is overwrite)
   private int numberOfKeys = -1;

   //Each attribute will be a byte[] of this size
   private int sizeOfValue = -1;

   //the number of threads that will work on this slave
   private int numOfThreads = -1;

   //indicates that the coordinator executes transactions or not
   private boolean coordinatorParticipation = true;

   private String writeTxWorkload = "50;50";

   private String readTxWorkload = "100";

   //The percentage of write transactions generated
   private int writeTransactionPercentage = 100;

   private String bucketPrefix = null;

   //simulation time (in seconds)
   private long perThreadSimulTime = -1;

   //allows execution without contention
   private boolean noContention = false;

   //the probability of the key to be local, assuming key_x_?? is store in node x
   private int localityProbability = -1;

   //for gaussian keys
   private double stdDev = -1;

   private CacheWrapper cacheWrapper;
   private boolean reportNanos = false;

   @Override
   public void initOnMaster(MasterState masterState, int slaveIndex) {
      super.initOnMaster(masterState, slaveIndex);
      Boolean started = (Boolean) masterState.get(SCRIPT_LAUNCH);
      if (started == null || !started) {
         masterState.put(SCRIPT_LAUNCH, startScript());
      }
   }

   private Boolean startScript() {
      try {
         Runtime.getRuntime().exec(SCRIPT_PATH);
         log.info("Script " + SCRIPT_PATH + " started successfully");
         return Boolean.TRUE;
      } catch (Exception e) {
         log.warn("Error starting script " + SCRIPT_PATH + ". " + e.getMessage());
         return Boolean.FALSE;
      }
   }

   public DistStageAck executeOnSlave() {
      DefaultDistStageAck result = new DefaultDistStageAck(slaveIndex, slaveState.getLocalAddress());
      this.cacheWrapper = slaveState.getCacheWrapper();
      if (cacheWrapper == null) {
         log.info("Not running test on this slave as the wrapper hasn't been configured.");
         return result;
      }

      log.info("Starting WebSessionBenchmarkStage: " + this.toString());

      PutGetStressor stressor = new PutGetStressor((KeyGeneratorFactory) slaveState.get("key_gen_factory"));
      stressor.setNodeIndex(getSlaveIndex());
      stressor.setSimulationTime(perThreadSimulTime);
      stressor.setWriteTxPercentage(writeTransactionPercentage);
      stressor.setCoordinatorParticipation(coordinatorParticipation);
      stressor.setWriteTxWorkload(writeTxWorkload);
      stressor.setReadTxWorkload(readTxWorkload);
      stressor.setBucketPrefix(bucketPrefix);
      stressor.setSizeOfValue(sizeOfValue);
      stressor.setNoContention(noContention);
      stressor.setNumberOfKeys(numberOfKeys);
      stressor.setNumberOfNodes(getActiveSlaveCount());
      stressor.setNumberOfThreads(numOfThreads);
      stressor.setLocalityProbability(localityProbability);
      stressor.setStdDev(stdDev);

      try {
         Map<String, String> results = stressor.stress(cacheWrapper);
         result.setPayload(results);
         return result;
      } catch (Exception e) {
         log.warn("Exception while initializing the test", e);
         result.setError(true);
         result.setRemoteException(e);
         result.setErrorMessage(e.getMessage());
         return result;
      }
   }

   public boolean processAckOnMaster(List<DistStageAck> acks, MasterState masterState) {
      logDurationInfo(acks);
      boolean success = true;
      Map<Integer, Map<String, Object>> results = new HashMap<Integer, Map<String, Object>>();
      masterState.put("results", results);
      for (DistStageAck ack : acks) {
         DefaultDistStageAck wAck = (DefaultDistStageAck) ack;
         if (wAck.isError()) {
            success = false;
            log.warn("Received error ack: " + wAck);
         } else {
            if (log.isTraceEnabled())
               log.trace(wAck);
         }
         Map<String, Object> benchResult = (Map<String, Object>) wAck.getPayload();
         if (benchResult != null) {
            results.put(ack.getSlaveIndex(), benchResult);
            Object reqPerSes = benchResult.get("TX_PER_SEC");
            if (reqPerSes == null) {
               throw new IllegalStateException("This should be there!");
            }
            log.info("On slave " + ack.getSlaveIndex() + " we had " + numberFormat(parseDouble(reqPerSes.toString())) + " requests per second");
         } else {
            log.trace("No report received from slave: " + ack.getSlaveIndex());
         }
      }
      return success;
   }

   @Override
   public String toString() {
      return "WebSessionBenchmarkStage{" +
            "numberOfKeys=" + numberOfKeys +
            ", sizeOfValue=" + sizeOfValue +
            ", numOfThreads=" + numOfThreads +
            ", coordinatorParticipation=" + coordinatorParticipation +
            ", writeTxWorkload='" + writeTxWorkload + '\'' +
            ", readTxWorkload='" + readTxWorkload + '\'' +
            ", writeTransactionPercentage=" + writeTransactionPercentage +
            ", bucketPrefix='" + bucketPrefix + '\'' +
            ", perThreadSimulTime=" + perThreadSimulTime +
            ", noContention=" + noContention +
            ", localityProbability=" + localityProbability +
            ", stdDev=" + stdDev +
            ", " + super.toString();
   }

   public void setPerThreadSimulTime(long perThreadSimulTime){
      this.perThreadSimulTime = perThreadSimulTime;
   }

   public void setNumberOfKeys(int numberOfKeys) {
      this.numberOfKeys = numberOfKeys;
   }

   public void setSizeOfValue(int sizeOfValue) {
      this.sizeOfValue = sizeOfValue;
   }

   public void setWriteTxWorkload(String writeTxWorkload) {
      this.writeTxWorkload = writeTxWorkload;
   }

   public void setReadTxWorkload(String readTxWorkload) {
      this.readTxWorkload = readTxWorkload;
   }

   public void setNumOfThreads(int numOfThreads) {
      this.numOfThreads = numOfThreads;
   }

   public void setReportNanos(boolean reportNanos) {
      this.reportNanos = reportNanos;
   }

   public void setWriteTransactionPercentage(int writeTransactionPercentage) {
      this.writeTransactionPercentage = writeTransactionPercentage;
   }

   public void setNoContention(boolean noContention) {
      this.noContention = noContention;
   }

   public void setCoordinatorParticipation(boolean coordinatorParticipation) {
      this.coordinatorParticipation = coordinatorParticipation;
   }

   public void setLocalityProbability(int localityProbability) {
      this.localityProbability = localityProbability;
   }

   public void setStdDev(double stdDev) {
      this.stdDev = stdDev;
   }


}
