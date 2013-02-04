package eu.cloudtm.reporter.customattributes;

import eu.cloudtm.reporter.logging.Log;
import eu.cloudtm.reporter.logging.LogFactory;
import eu.cloudtm.reporter.manager.Resource;

/**
 * Calculates the commit latency based on the transaction execution time and the total transaction execution time
 * (execution + commit)
 *
 * @author Pedro Ruivo
 * @since 1.0
 */
public class CommitLatency implements CustomAttribute {

   private static final Log log = LogFactory.getLog(CommitLatency.class);

   private double txDuration;
   private double txExecutionDuration;
   private int counter;

   @Override
   public String[] getHeaders() {
      return new String[] {"CommitLatency"};
   }

   @Override
   public String[] getValues() {
      try {
         double commitLatency = txDuration - txExecutionDuration;
         if (counter <= 0 || commitLatency <= 0) {
            return new String[] {""};
         }
         double avgCommitLatency = commitLatency / counter;
         log.trace("Commit latency is %s", avgCommitLatency);
         return new String[] {Double.toString(avgCommitLatency)};
      } finally {
         reset();
      }
   }

   @Override
   public String[] getAttributes() {
      return new String[] {"avgWriteTxDuration", "avgWriteTxLocalExecution"};
   }

   @Override
   public void update(Resource resource) {
      double tmpTxDuration = resource.getDoubleIspnAttribute("avgWriteTxDuration");
      double tmpTxExecutionDuration = resource.getDoubleIspnAttribute("avgWriteTxLocalExecution");

      log.trace("Updating commit latency for resource %s. exec=%s, total=%s", resource, tmpTxExecutionDuration, tmpTxDuration);

      if (tmpTxDuration == Resource.UNKNOWN_DOUBLE_ATTR || tmpTxExecutionDuration == Resource.UNKNOWN_DOUBLE_ATTR ||
            tmpTxDuration == 0 || tmpTxExecutionDuration == 0) {
         return;
      }

      txDuration += tmpTxDuration;
      txExecutionDuration += tmpTxExecutionDuration;
      counter++;
   }

   private void reset() {
      log.trace("Resetting commit latency intermediate values");
      txDuration = 0;
      txExecutionDuration = 0;
      counter = 0;
   }
}
