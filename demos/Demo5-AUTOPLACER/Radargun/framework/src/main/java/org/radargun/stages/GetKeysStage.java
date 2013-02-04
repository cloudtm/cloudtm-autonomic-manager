package org.radargun.stages;

import org.radargun.CacheWrapper;
import org.radargun.DistStageAck;
import org.radargun.keygen2.RadargunKey;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

/**
 * @author Pedro Ruivo
 * @since 1.1
 */
public class GetKeysStage extends AbstractDistStage {

   private static final String NEW_LINE = System.getProperty("line.separator");
   private String filePath = "keys.txt";
   private boolean saveInStringFormat = true;
   private boolean saveValues = true;

   @Override
   public DistStageAck executeOnSlave() {
      DefaultDistStageAck result = new DefaultDistStageAck(slaveIndex, slaveState.getLocalAddress());
      CacheWrapper cacheWrapper = slaveState.getCacheWrapper();

      if (cacheWrapper == null) {
         log.info("Not running test on this slave as the wrapper hasn't been configured.");
         return result;
      }

      Collection<? extends RadargunKey> localKeys = cacheWrapper.getLocalKeys(RadargunKey.class);

      log.info("Starting GetKeysStage:" + this.toString());
      log.info("Save keys to file in " + filePath);

      try {
         ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(filePath));
         if (saveValues) {
            for (RadargunKey key : localKeys) {
               writeKeyAndValue(key, objectOutputStream, cacheWrapper);
            }
         } else {
            for (RadargunKey key : localKeys) {
               writeKey(key, objectOutputStream);
            }
         }
      } catch (Exception e) {
         result.setRemoteException(e);
         return result;
      }

      return result;
   }

   public void setFilePath(String filePath) {
      this.filePath = filePath;
   }

   public void setSaveInStringFormat(boolean saveInStringFormat) {
      this.saveInStringFormat = saveInStringFormat;
   }

   public void setSaveValues(boolean saveValues) {
      this.saveValues = saveValues;
   }

   private void writeKeyAndValue(RadargunKey key, ObjectOutputStream objectOutputStream, CacheWrapper cacheWrapper)
         throws Exception {
      Object value = cacheWrapper.get(null, key);
      if (saveInStringFormat) {
         objectOutputStream.writeUTF(key == null ? "null" : key.toString());
         objectOutputStream.writeUTF("=>");
         objectOutputStream.writeUTF(value == null ? "null" : value.toString());
         objectOutputStream.writeUTF(NEW_LINE);
      } else {
         objectOutputStream.writeObject(key);
         objectOutputStream.writeObject(value);
      }
      objectOutputStream.flush();
   }

   private void writeKey(RadargunKey key, ObjectOutputStream objectOutputStream) throws Exception {
      if (saveInStringFormat) {
         objectOutputStream.writeUTF(key == null ? "null" : key.toString());
      } else {
         objectOutputStream.writeObject(key);
      }
   }

   @Override
   public String toString() {
      return "GetKeysStage{" +
            "filePath='" + filePath + '\'' +
            ", saveInStringFormat=" + saveInStringFormat +
            ", saveValues=" + saveValues +
            ", " + super.toString();
   }
}

