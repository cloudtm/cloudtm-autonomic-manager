package org.radargun.stressors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.CacheWrapper;
import org.radargun.CacheWrapperStressor;

import java.util.Collections;
import java.util.Map;

import static org.radargun.utils.IncrementCounterUtil.COUNTER_KEY;
import static org.radargun.utils.IncrementCounterUtil.DEFAULT_BUCKET_PREFIX;

/**
 * // TODO: Document this
 *
 * @author pruivo
 * @since 4.0
 */
public class IncrementCounterWarmupStressor implements CacheWrapperStressor {

    private static Log log = LogFactory.getLog(IncrementCounterWarmupStressor.class);
    private int slaveIdx = 0;

    @Override
    public Map<String, String> stress(CacheWrapper wrapper) {
        if (wrapper == null) {
            throw new IllegalStateException("Null wrapper not allowed");
        }
        boolean success = false;
        while (!success) {
            try {
                if (slaveIdx == 0) {
                    wrapper.put(DEFAULT_BUCKET_PREFIX, COUNTER_KEY, 0);
                }
                success = true;
            } catch (Exception e) {
                log.warn("Exception occurred while initializing the counter." + e.getLocalizedMessage());
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public void destroy() throws Exception {
        //Do nothing... we don't want to loose the keys
    }

    public void setSlaveIdx(int slaveIdx) {
        this.slaveIdx = slaveIdx;
    }
}
