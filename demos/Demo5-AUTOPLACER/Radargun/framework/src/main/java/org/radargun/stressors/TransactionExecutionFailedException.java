package org.radargun.stressors;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class TransactionExecutionFailedException extends Exception {   

   private Object lastValueRead; 
   
   public TransactionExecutionFailedException() {
   }

   public TransactionExecutionFailedException(String s) {
      super(s);
   }

   public TransactionExecutionFailedException(String s, Throwable throwable) {
      super(s, throwable);
   }

   public TransactionExecutionFailedException(Throwable throwable) {
      super(throwable);
   }
   
   public Object getLastValueRead() {
      return lastValueRead;
   }

   public void setLastValueRead(Object lastValueRead) {
      this.lastValueRead = lastValueRead;
   }
}
