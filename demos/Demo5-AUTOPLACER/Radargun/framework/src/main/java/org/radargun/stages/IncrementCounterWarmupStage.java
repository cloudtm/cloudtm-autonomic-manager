package org.radargun.stages;

import org.radargun.CacheWrapper;
import org.radargun.DistStageAck;
import org.radargun.state.MasterState;
import org.radargun.stressors.IncrementCounterWarmupStressor;

import java.util.List;

/**
 * // TODO: Document this
 *
 * @author pruivo
 * @since 4.0
 */
public class IncrementCounterWarmupStage extends AbstractDistStage {

    @Override
    public DistStageAck executeOnSlave() {
        DefaultDistStageAck ack = newDefaultStageAck();
        CacheWrapper wrapper = slaveState.getCacheWrapper();
        if (wrapper == null) {
            log.info("Not executing any test as the wrapper is not set up on this slave ");
            return ack;
        }
        IncrementCounterWarmupStressor stressor = new IncrementCounterWarmupStressor();
        stressor.setSlaveIdx(this.slaveIndex);

        long startTime = System.currentTimeMillis();
        stressor.stress(wrapper);
        long duration = System.currentTimeMillis() - startTime;
        log.info("The init stage took: " + (duration / 1000) + " seconds.");
        ack.setPayload(duration);
        return ack;
    }

    public boolean processAckOnMaster(List<DistStageAck> acks, MasterState masterState) {
        logDurationInfo(acks);
        for (DistStageAck ack : acks) {
            DefaultDistStageAck dAck = (DefaultDistStageAck) ack;
            if (log.isTraceEnabled()) {
                log.trace("Init on slave " + dAck.getSlaveIndex() + " finished in " + dAck.getPayload() + " millis.");
            }
        }
        return true;
    }
}
