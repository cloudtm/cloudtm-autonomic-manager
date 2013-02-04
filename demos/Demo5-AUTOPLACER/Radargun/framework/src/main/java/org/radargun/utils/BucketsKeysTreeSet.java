package org.radargun.utils;

import java.util.*;

/**
 * Date: 1/19/12
 * Time: 10:27 AM
 *
 * @author pruivo
 */
public class BucketsKeysTreeSet {
    SortedMap<String, SortedSet<String>> buckesAndKeys;

    public BucketsKeysTreeSet() {
        buckesAndKeys = new TreeMap<String, SortedSet<String>>();
    }

    public void addKeySet(String bucket, Collection<String> keys) {
        SortedSet<String> keysSorted = buckesAndKeys.get(bucket);
        if(keysSorted == null) {
            keysSorted = new TreeSet<String>(keys != null ? keys : Collections.<String>emptySet());
            buckesAndKeys.put(bucket, keysSorted);
        } else {
            keysSorted.addAll(keys);
        }
    }

    public Set<Map.Entry<String, SortedSet<String>>> getEntrySet() {
        return buckesAndKeys.entrySet();
    }
}
