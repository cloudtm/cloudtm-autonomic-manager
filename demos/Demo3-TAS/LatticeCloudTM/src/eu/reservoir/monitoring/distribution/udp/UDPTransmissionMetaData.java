// UDPTransmissionMetaData.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2009

package eu.reservoir.monitoring.distribution.udp;

import eu.reservoir.monitoring.distribution.MetaData;
import java.net.InetAddress;
import java.io.Serializable;

/**
 * Information about a transmission.
 * Includes: packet length, src ip address, dst ip address
 */
public class UDPTransmissionMetaData implements MetaData, Serializable {
    public final int length;
    public final InetAddress srcIPAddr;
    public final InetAddress dstIPAddr;

    /**
     * Construct a UDPTransmissionMetaData object.
     */
    public UDPTransmissionMetaData(int l, InetAddress sia, InetAddress dia) {
	length = l;
	srcIPAddr = sia;
	dstIPAddr = dia;
    }

    /**
     * UDPTransmissionMetaData to string.
     */
    public String toString() {
	return dstIPAddr + ": "  + srcIPAddr + " => " + length;
    }
}