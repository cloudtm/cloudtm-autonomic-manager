package org.radargun.stages;

import org.radargun.CacheWrapper;
import org.radargun.DistStageAck;
import org.radargun.state.MasterState;
import org.radargun.stressors.IncrementCounterStressor;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.radargun.utils.IncrementCounterUtil.STRESSOR_INCREMENTS;
import static org.radargun.utils.IncrementCounterUtil.STRESSOR_RESULT;

/**
 * Created by IntelliJ IDEA.
 * Date: 3/4/12
 * Time: 8:10 PM
 *
 * @author Pedro Ruivo
 */
public class IncrementCounterBenchmarkStage extends AbstractDistStage {

    //the number of threads that will work on this slave
    private int numOfThreads = 10;

    //simulation time
    private long perThreadSimulTime = 30000000; //30 seconds

    private CacheWrapper cacheWrapper;

    @Override
    public DistStageAck executeOnSlave() {
        DefaultDistStageAck result = new DefaultDistStageAck(slaveIndex, slaveState.getLocalAddress());
        this.cacheWrapper = slaveState.getCacheWrapper();
        if (cacheWrapper == null) {
            log.info("Not running test on this slave as the cacheWrapper hasn't been configured.");
            return result;
        }

        log.info("Starting WebSessionBenchmarkStage: " + this.toString());

        IncrementCounterStressor stressor = new IncrementCounterStressor();
        stressor.setNumOfThreads(numOfThreads);
        stressor.setSimulationTime(perThreadSimulTime);
        stressor.setSlaveIdx(getSlaveIndex());

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

    @Override
    public boolean processAckOnMaster(List<DistStageAck> acks, MasterState masterState) {
        logDurationInfo(acks);
        boolean success = true;
        TreeSet<Integer> allIncrements = new TreeSet<Integer>();

        for (DistStageAck ack : acks) {
            DefaultDistStageAck wAck = (DefaultDistStageAck) ack;
            if (wAck.isError()) {
                success = false;
                log.warn("Received error ack: " + wAck);
            } else {
                if (log.isTraceEnabled())
                    log.trace(wAck);
            }
            Map<String, String> benchResult = (Map<String, String>) wAck.getPayload();
            if (benchResult != null) {
                boolean ok = Boolean.valueOf(benchResult.get(STRESSOR_RESULT));

                if(!ok) {
                    log.warn("Error received from slave " + ack.getSlaveIndex());
                    success = false;
                    continue;
                }

                SortedSet<Integer> increments = IncrementCounterStressor.convertStringToSet(
                        benchResult.get(STRESSOR_INCREMENTS));

                for (Integer i : increments) {
                    if(!allIncrements.add(i)) {
                        log.warn("Received a duplicated increment from slave" + ack.getSlaveIndex());
                        success = false;
                        break;
                    }
                }
            } else {
                log.trace("No report received from slave: " + ack.getSlaveIndex());
            }
        }
        return success;
    }

    @Override
    public String toString() {
        return "IncrementCounterBenchmarkStage{" +
                "numOfThreads=" + numOfThreads +
                ", perThreadSimulTime=" + perThreadSimulTime +
                ", cacheWrapper=" + cacheWrapper +
                '}';
    }

    public void setPerThreadSimulTime(long l){
        this.perThreadSimulTime = l;
    }

    public void setNumOfThreads(int numOfThreads) {
        this.numOfThreads = numOfThreads;
    }
}
