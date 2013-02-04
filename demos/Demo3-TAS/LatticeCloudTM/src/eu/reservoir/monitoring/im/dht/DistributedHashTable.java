// DistributedHashTable.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.im.dht;

import java.io.Serializable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.math.BigInteger;
import org.planx.xmlstore.routing.messaging.*;
import org.planx.xmlstore.routing.operation.*;
import org.planx.xmlstore.routing.*;

/**
 * A Distributed Hash Table implementation.
 * Values can be accessed and added from any one
 * of the distributed nodes.
 */
public class DistributedHashTable {
    Kademlia kademlia = null;

    Configuration conf = null;

    Identifier[] ids;


    int port = 0;

    // allow identifier keys to have 1024 bits
    static { Identifier.IDSIZE=300; }

    /**
     * Constructor a Distributed Hash Table on a specified port
     */
    public DistributedHashTable(int port) throws IOException {
	conf = new Configuration();
	//conf.B = 10; // log base exponent
	//conf.K = 2000;  //  Bucket size.          
	//conf.CONCURRENCY = 10; // Maximum number of concurrent messages in transit.

	this.port = port;

	// allocate a Kademlia object
	kademlia = new Kademlia(Identifier.randomIdentifier(), port, conf);
    }


    /**
     * Set up kademlia to connect to a known and existing Kademlia
     */
    public void connect(String remAddress, int remPort) throws IOException {
	InetSocketAddress connAddr;

	connAddr = new InetSocketAddress(InetAddress.getByName(remAddress), remPort);

	kademlia.connect(connAddr);
    }

    /**
     * Close the kademlia connection.
     */
    public void close() throws IOException {
	kademlia.close();
    }

    /**
     * Get an object out of the DHT.
     */
    public Object get(String aKey) throws IOException {
	Identifier key = new Identifier(new String(aKey).getBytes());

	return kademlia.get(key);
    }

    /**
     * Get an object out of the DHT.
     */
    public Object get(BigInteger aKey) throws IOException {
	Identifier key = new Identifier(aKey);

	return kademlia.get(key);
    }

    /**
     * Put an object into the DHT.
     */
    public DistributedHashTable put(String aKey, Serializable aValue) throws IOException {
	Identifier key = new Identifier(aKey.getBytes());

	kademlia.put(key, aValue);

	return this;
    }

    /**
     * Put an object into the DHT.
     */
    public DistributedHashTable put(BigInteger aKey, Serializable aValue) throws IOException {
	Identifier key = new Identifier(aKey);

	kademlia.put(key, aValue);

	return this;
    }

    /**
     * Does the DHT contain a particular Identifier.
     * Returns true if the map contains the specified key and false otherwise.
     */
    public boolean contains(String aKey) throws IOException {
	Identifier key = new Identifier(new String(aKey).getBytes());

	return kademlia.contains(key);
    }

    /**
     * Removes the mapping with the specified key.
     */
    public DistributedHashTable remove(String aKey) throws IOException {
	Identifier key = new Identifier(new String(aKey).getBytes());

	kademlia.remove(key);

	return this;
    }

    /**
     * Removes the mapping with the specified key.
     */
    public DistributedHashTable remove(BigInteger aKey) throws IOException {
	Identifier key = new Identifier(aKey);

	kademlia.remove(key);

	return this;
    }



}
