package org.radargun.fwk;

import org.radargun.utils.TransactionWorkload;
import org.testng.annotations.Test;

import java.util.Random;

import static org.radargun.utils.TransactionWorkload.Operation;

/**
 * Simple transaction workload parsers test
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
@Test
public class TransactionWorkloadTest {

   public void testWriteTxParserWrong() {
      //Pattern: min read,max read;min write,max write
      TransactionWorkload workload = new TransactionWorkload(100);
      workload.writeTx("10;10;10");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 100);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 100);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 100);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 100);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 100);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 100);
   }

   public void testWriteTxParserNaN() {
      //Pattern: min read,max read;min write,max write
      TransactionWorkload workload = new TransactionWorkload(100);
      workload.writeTx("10;abc,def");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 100);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 100);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 100);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 100);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 100);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 100);
   }
   
   public void testWriteTxParser() {
      //Pattern: min read,max read;min write,max write
      TransactionWorkload workload = new TransactionWorkload(100);
      workload.writeTx("3,4;20,10");      
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 3);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 4);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 10);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 20);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 100);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 100);
   }

   public void testWriteTxParser2() {
      //Pattern: read;min write,max write
      TransactionWorkload workload = new TransactionWorkload(15);
      workload.writeTx("4;10,20");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 4);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 4);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 10);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 20);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 15);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 15);
   }

   public void testWriteTxParser3() {
      //Pattern: min read,max read;write
      TransactionWorkload workload = new TransactionWorkload(1);
      workload.writeTx("3,4;10");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 3);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 4);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 10);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 10);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 1);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 1);
   }

   public void testWriteTxParser4() {
      //Pattern: read;write
      TransactionWorkload workload = new TransactionWorkload(3);
      workload.writeTx("5;10");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 5);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 5);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 10);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 10);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 3);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 3);
   }

   public void testWriteTxParser5() {
      //Pattern: min read,max read;
      TransactionWorkload workload = new TransactionWorkload(5);
      workload.writeTx("3,4;");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 3);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 4);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 5);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 5);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 5);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 5);            
   }

   public void testWriteTxParser6() {
      //Pattern: ;min write,max write
      TransactionWorkload workload = new TransactionWorkload(5);
      workload.writeTx(";7,6");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 5);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 5);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 6);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 7);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 5);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 5);
   }

   public void testWriteTxParser7() {
      //Pattern: read;
      TransactionWorkload workload = new TransactionWorkload(5);
      workload.writeTx("10;");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 10);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 10);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 5);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 5);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 5);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 5);
   }

   public void testWriteTxParser8() {
      //Pattern: ;write
      TransactionWorkload workload = new TransactionWorkload(5);
      workload.writeTx(";30");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 5);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 5);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 30);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 30);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 5);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 5);
   }

   public void testReadTxParserWrong() {
      //Pattern: min read,max read;min write,max write
      TransactionWorkload workload = new TransactionWorkload(100);
      workload.readTx("10;10;10");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 100);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 100);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 100);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 100);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 100);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 100);
   }

   public void testReadTxParserNaN() {
      //Pattern: min read,max read;min write,max write
      TransactionWorkload workload = new TransactionWorkload(100);
      workload.readTx("10;abc,def");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 100);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 100);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 100);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 100);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 100);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 100);
   }

   public void testReadTxParser() {
      //Pattern: min read,max read
      TransactionWorkload workload = new TransactionWorkload(100);
      workload.readTx("3,4");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 100);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 100);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 100);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 100);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 3);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 4);
   }

   public void testReadTxParser2() {
      //Pattern: read
      TransactionWorkload workload = new TransactionWorkload(10);
      workload.readTx("5");
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 10);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 10);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 10);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 10);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 5);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 5);
   }
   
   public void testOperationBoundaries() {
      TransactionWorkload workload = new TransactionWorkload(1);
      workload.writeTx("3,4;10");

      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 3);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 4);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 10);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 10);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 1);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 1);

      Random random = new Random();
      for (int i = 0; i < 10; i++) {
         assertBoundaries(workload.writeTxWrites(random), 10, 10);
         assertBoundaries(workload.writeTxReads(random), 3, 4);
         assertBoundaries(workload.readTxReads(random), 1, 1);
      }
   }

   public void testOperationBoundaries2() {
      TransactionWorkload workload = new TransactionWorkload(1);
      workload.writeTx("3;10,20");
      workload.readTx("5,7");

      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_READ, 3);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_READ, 3);
      assertValue(workload, Operation.WRITE_TX_LOWER_BOUND_WRITE, 10);
      assertValue(workload, Operation.WRITE_TX_UPPER_BOUND_WRITE, 20);
      assertValue(workload, Operation.READ_TX_LOWER_BOUND, 5);
      assertValue(workload, Operation.READ_TX_UPPER_BOUND, 7);

      Random random = new Random();
      for (int i = 0; i < 10; i++) {
         assertBoundaries(workload.writeTxWrites(random), 10, 20);
         assertBoundaries(workload.writeTxReads(random), 3, 3);
         assertBoundaries(workload.readTxReads(random), 5, 7);
      }
   }
   
   private void assertBoundaries(int op, int lower, int upper) {      
      assert lower <= op && op <= upper : String.format("Wrong Boundary: %s <= %s <= %s", lower, op, upper);
   }

   private void assertValue(TransactionWorkload workload, Operation operation, int expectedValue) {
      int value = workload.getOperationBounds().get(operation);
      assert value == expectedValue : String.format("mismatch values for %s. %s != %s", operation, expectedValue, value);
   }
   
}
