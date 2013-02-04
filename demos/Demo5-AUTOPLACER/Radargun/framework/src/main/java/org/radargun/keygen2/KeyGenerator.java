package org.radargun.keygen2;

/**
 * the interface
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public interface KeyGenerator {

   /**
    * returns a random key
    * @return  a random key
    */
   Object getRandomKey();

   /**
    * returns an array of {@code size} unique keys or less than {@code size} if there are not enough keys depending
    * of the configuration used.
    *
    * @param size the number of keys pretended
    * @return     an array of {@code size} unique keys
    */
   Object[] getUniqueRandomKeys(int size);

   /**
    * returns the bucket
    * @return  the bucket
    */
   String getBucket();

   /**
    * creates a random value to set the key. the size of the value is the size set in the configuration
    *
    * @return  a random value to set the key
    */
   Object getRandomValue();

}
