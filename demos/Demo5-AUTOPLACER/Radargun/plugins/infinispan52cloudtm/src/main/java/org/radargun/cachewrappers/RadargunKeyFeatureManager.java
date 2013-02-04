package org.radargun.cachewrappers;

import org.infinispan.dataplacement.c50.keyfeature.Feature;
import org.infinispan.dataplacement.c50.keyfeature.FeatureValue;
import org.infinispan.dataplacement.c50.keyfeature.KeyFeatureManager;
import org.infinispan.dataplacement.c50.keyfeature.NumericFeature;
import org.radargun.keygen2.RadargunKey;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Key Features manager for Radargun. Used by the data placement algorithm
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public class RadargunKeyFeatureManager implements KeyFeatureManager {

   private static enum RadargunFeature {
      NODE_INDEX("node_index"),
      THREAD_INDEX("thread_index"),
      KEY_INDEX("key_index");

      final Feature feature;

      private RadargunFeature(String featureName) {
         feature = new NumericFeature(featureName);
      }
   }

   private final Feature[] features;

   @SuppressWarnings("UnusedDeclaration") //loaded dynamically
   public RadargunKeyFeatureManager() {
      features = new Feature[RadargunFeature.values().length];
      for (RadargunFeature feature : RadargunFeature.values()) {
         features[feature.ordinal()] = feature.feature;
      }
   }

   @Override
   public Feature[] getAllKeyFeatures() {
      return features;
   }

   @Override
   public Map<Feature, FeatureValue> getFeatures(Object key) {
      if (key instanceof RadargunKey) {
          RadargunKey radargunKey = (RadargunKey) key;
          Map<Feature, FeatureValue> featureValueMap = new HashMap<Feature, FeatureValue>();
          for (RadargunFeature radargunFeature : RadargunFeature.values()) {
              featureValueMap.put(radargunFeature.feature, valueOf(radargunFeature, radargunKey));
          }
          return featureValueMap;
      }

      return Collections.emptyMap();
   }

    private FeatureValue valueOf(RadargunFeature feature, RadargunKey key) {
        int value = 0;
        switch (feature) {
            case KEY_INDEX:
                value = key.getKeyIdx();
                break;
            case NODE_INDEX:
                value = key.getNodeIdx();
                break;
            case THREAD_INDEX:
                value = key.getThreadIdx();
                break;
        }
        return feature.feature.createFeatureValue(value);
    }
}
