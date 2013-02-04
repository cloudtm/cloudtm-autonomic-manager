// MulticastTransmitter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.distribution.multicast;

import eu.reservoir.monitoring.distribution.*;
import java.net.*;
import java.io.*;

/**
 * This is a multicast transmitter for monitoring messages
 */
public class MulticastTransmitter {
    /*
     * The transmitting that interacts with a DataSourceDelegate.
     */
    Transmitting transmitting = null;

    /*
     * The multicast addres being transmitted to
     */
    MulticastAddress maddr;

    /*
     * The multicast socket being transmitted to
     */
    MulticastSocket msocket;

    /*
     * A packet being transmitted 
     */
    DatagramPacket mpacket;

    /*
     * The IP address
     */
    InetAddress address;

    /*
     * The port
     */
    int port;

    /*
     * The ttl
     */
    int ttl;

    /**
     * Construct a transmitter for a particular multicast address
     */
    public MulticastTransmitter(Transmitting transmitting, MulticastAddress mcastAddr, int ttl) throws IOException {
	maddr = mcastAddr;

	this.transmitting = transmitting;
	address = mcastAddr.getAddress();
	this.port = mcastAddr.getPort();
	this.ttl = ttl;

	setUpSocket();
    }

    /**
     * Set up the socket for the given addr/port/ttl,
     * and also a pre-prepared DatagramPacket.
     */
    void setUpSocket() throws IOException {
	msocket = new MulticastSocket(port);
	msocket.setTimeToLive(ttl);

	// allocate an emtpy packet for use later
	mpacket = new DatagramPacket(new byte[1], 1);
	mpacket.setAddress(address);
	mpacket.setPort(port);
    }

    /**
     * Join the address now
     * and start listening
     */
    public void join()  throws IOException {
	// join the multicast group
	msocket.joinGroup(address);

    }

    /**
     * Leave the address now
     * and stop listening
     */
    public void leave()  throws IOException {
	// leave the multicast group
	msocket.leaveGroup(address);
    }

    /**
     * Send a message onto the multicast address,  with a given id.
     */
    public int transmit(ByteArrayOutputStream byteStream, int id) throws IOException {
	// set up the packet
	mpacket.setData(byteStream.toByteArray());
	mpacket.setLength(byteStream.size());

	// now send it
	msocket.send(mpacket);

	//System.err.println("trans: " + id + " = " + byteStream.size());

	// notify the transmitting object
	if (transmitting != null) {
	    transmitting.transmitted(id);
	}

	return byteStream.size();
    }
}
