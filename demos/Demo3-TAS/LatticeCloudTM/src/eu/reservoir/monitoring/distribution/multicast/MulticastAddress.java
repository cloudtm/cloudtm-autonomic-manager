// MulticastAddress.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.distribution.multicast;

import java.net.*;
import java.io.Serializable;

/**
 * The full encapsulation of a multicast address in a single object.
 * It contains the InetAddress and the port
 */
public class MulticastAddress implements Serializable {
    /*
     * The InetAddress
     */
    InetAddress addr;

    /*
     * The port
     */
    int port;

    public MulticastAddress(InetAddress addr, int port) {
	this.addr = addr;
	this.port = port;
    }

    public MulticastAddress(String addrStr, int port) {
	try {
	    InetAddress addr = InetAddress.getByName(addrStr);
	    this.addr = addr;
	    this.port = port;
	} catch (UnknownHostException uhe) {
	    throw new Error(uhe.getMessage());
	}
    }

    /**
     * Return the InetAddress
     */
    public InetAddress getAddress() {
	return addr;
    }

    /**
     * Return the port
     */
    public int getPort() {
	return port;
    }


    /**
     * Two MulticastAddresses are equal if
     * Their InetAddresses are equal() and their ports are ==
     */
    public boolean equals(Object other) {
	if (other instanceof MulticastAddress) {
	    MulticastAddress mOther = (MulticastAddress)other;

	    // if the addresses are equal() and the ports are ==
	    // it's the same MulticastAddress.
	    if (this.getAddress().equals(mOther.getAddress()) &&
		this.getPort() == mOther.getPort()) {
		return true;
	    } else {
		return false;
	    }
	} else {  // wrong class
	    return false;
	}
    }

    /**
     * Return a hashCode
     */
    public int hashCode() {
	return addr.hashCode();
    }


    /**
     * To string
     */
    public String toString() {
	return getAddress() + "/" + getPort();
    }
}
