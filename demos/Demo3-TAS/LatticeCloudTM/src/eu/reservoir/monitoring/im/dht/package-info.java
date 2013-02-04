/**
 * This package provides classes that are 
 * the implementation of the Info Plane
 * that utilizes a Distributed Hash Table (DHT)
 * for storing the Info on the Probes and the Data Sources.
 * <p>
 * For a DHT, there needs to be a <em>root</em> node,
 * which the other DHT nodes connect to.
 * This is defined in DHTInfoPlaneRoot.
 * <p>
 * Any other nodes which add data to the DHT are defined
 * using a DHTInfoPlane object, which points to the <em>root</em> node.
 * <p>
 * For a node which only wishes to read data from the DHT, but not
 * add new data, the DHTInfoPlaneConsumer class is used.
 */
package eu.reservoir.monitoring.im.dht;