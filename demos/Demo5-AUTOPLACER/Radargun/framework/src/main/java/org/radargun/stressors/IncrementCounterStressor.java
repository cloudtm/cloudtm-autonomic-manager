package org.radargun.stressors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.CacheWrapper;
import org.radargun.CacheWrapperStressor;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.radargun.utils.IncrementCounterUtil.*;

/**
 * Date: 1/24/12
 * Time: 3:35 PM
 *
 * @author pruivo
 */
public class IncrementCounterStressor implements CacheWrapperStressor {

    private static Log log = LogFactory.getLog(IncrementCounterStressor.class);

    private CacheWrapper cacheWrapper;
    private volatile CountDownLatch startPoint;
    private int slaveIdx = 1;

    //the number of threads that will work on this cache wrapper.
    private int numOfThreads = 1;

    //simulation time (in nanoseconds) (default: 30 seconds)
    private long simulationTime = 30000000000L;

    @Override
    public Map<String, String> stress(CacheWrapper wrapper) {
        this.cacheWrapper = wrapper;

        if(slaveIdx == 0) {
            try {
                wrapper.put(DEFAULT_BUCKET_PREFIX, COUNTER_KEY, new Integer(0));
            } catch (Exception e) {
                log.warn("error initializing the counter");
            }
        }

        log.info("Executing: " + this.toString());

        List<Stresser> stressers;
        try {
            stressers = executeOperations();
        } catch (Exception e) {
            log.warn("exception when stressing the cache wrapper", e);
            throw new RuntimeException(e);
        }
        return processResults(stressers);
    }



    @Override
    public void destroy() throws Exception {
        cacheWrapper.empty();
        cacheWrapper = null;
    }

    public void setSlaveIdx(int slaveIdx) {
        this.slaveIdx = slaveIdx;
    }

    public void setSimulationTime(long simulationTime) {
        this.simulationTime = simulationTime;
    }

    public void setNumOfThreads(int numOfThreads) {
        this.numOfThreads = numOfThreads;
    }

    private class Stresser extends Thread {
        private long delta = 0;
        private TreeSet<Integer> increments;
        private boolean executionSuccessful = true;

        public Stresser(int threadIndex) {
            super("Stresser-" + threadIndex + ":" + slaveIdx);
            this.increments = new TreeSet<Integer>();
        }

        @Override
        public void run() {
            try {
                startPoint.await();
                log.info("Starting thread: " + getName());
            } catch (InterruptedException e) {
                log.warn(e);
            }
            long init_time = System.nanoTime();
            int i = 0;
            Integer counterValue = 0;
            boolean successful;

            while(delta < simulationTime){
                successful = true;

                cacheWrapper.startTransaction();
                log.info("*** [" + getName() + "] new transaction: " + i + "***");


                try {
                    counterValue = (Integer) cacheWrapper.get(DEFAULT_BUCKET_PREFIX, COUNTER_KEY);
                    if(counterValue == null) {
                        counterValue = 0;
                    }
                } catch (Throwable e) {
                    log.warn("[" + getName() + "] error in get operation", e);
                    successful = false;
                    counterValue = null;
                }

                if(successful) {
                    counterValue++;
                    try {
                        cacheWrapper.put(DEFAULT_BUCKET_PREFIX, COUNTER_KEY, counterValue);
                    } catch (Throwable e) {
                        counterValue = null;
                        successful = false;
                        log.warn("[" + getName() + "] error in put operation", e);
                    }
                }

                try{
                    cacheWrapper.endTransaction(successful);
                } catch(Throwable e){
                    counterValue = null;
                    successful = false;
                    log.warn("[" + getName() + "] error committing transaction", e);
                }
                log.info("*** [" + getName() + "] end transaction: " + i++ + "; value=" + counterValue + "***");
                if(successful && counterValue != null) {
                    if(!increments.add(counterValue)) {
                        executionSuccessful = false;
                        log.fatal("Duplicated Counter Value found!! Aborting " + getName());
                        return ;
                    }
                }
                this.delta = System.nanoTime() - init_time;
            }
        }
    }

    private List<Stresser> executeOperations() throws Exception {
        List<Stresser> stressers = new ArrayList<Stresser>();
        startPoint = new CountDownLatch(1);

        for (int threadIndex = 0; threadIndex < numOfThreads; threadIndex++) {
            Stresser stresser = new Stresser(threadIndex);
            stressers.add(stresser);

            try{
                stresser.start();
            }
            catch (Throwable t){
                log.warn("Error starting all the stressers", t);
            }
        }

        log.info("Cache private class Stresser extends Thread { wrapper info is: " + cacheWrapper.getInfo());
        startPoint.countDown();
        for (Stresser stresser : stressers) {
            stresser.join();
            log.info("stresser[" + stresser.getName() + "] finsihed");
        }
        log.info("All stressers have finished their execution");

        return stressers;
    }

    private Map<String, String> processResults(List<Stresser> stressers) {
        HashMap<String, String> results = new HashMap<String, String>();
        TreeSet<Integer> allIncrements = new TreeSet<Integer>();

        for(Stresser s : stressers) {
            if(!s.executionSuccessful) {
                results.put(STRESSOR_RESULT, String.valueOf(false));
                return results;
            }
            for (Integer i : s.increments) {
                if(!allIncrements.add(i)) {
                    results.put(STRESSOR_RESULT, String.valueOf(false));
                    return results;
                }
            }
        }

        results.put(STRESSOR_RESULT, String.valueOf(true));
        results.put(STRESSOR_INCREMENTS, convertIntegerSetToString(allIncrements));
        return results;
    }

    public static String convertIntegerSetToString(Set<Integer> set) {
        String s = "";
        for (Integer i : set) {
            s += i + "!";
        }
        return s;
    }

    public static SortedSet<Integer> convertStringToSet(String string) {
        String[] ints = string.split("!");
        if(ints.length == 0) {
            return new TreeSet<Integer>();
        }
        TreeSet<Integer> result = new TreeSet<Integer>();
        for (String s : ints) {
            result.add(Integer.valueOf(s));
        }
        return result;
    }

}
