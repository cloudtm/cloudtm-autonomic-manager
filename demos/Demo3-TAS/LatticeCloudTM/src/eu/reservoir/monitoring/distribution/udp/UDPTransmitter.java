// UDPTransmitter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.distribution.udp;

import eu.reservoir.monitoring.distribution.*;
import java.net.*;
import java.io.*;

/**
 * This is a UDP transmitter for monitoring messages
 */
public class UDPTransmitter {
    /*
     * The transmitting that interacts with a DataSourceDelegate.
     */
    Transmitting transmitting = null;

    /*
     * The IP address being transmitted to
     */
    InetSocketAddress udpAddr;

    /*
     * The socket being transmitted to
     */
    DatagramSocket socket;

    /*
     * A packet being transmitted 
     */
    DatagramPacket packet;

    /*
     * The IP address
     */
    InetAddress address;

    /*
     * The port
     */
    int port;

    /**
     * Construct a transmitter for a particular IP address
     */
    public UDPTransmitter(Transmitting transmitting, InetSocketAddress dstAddr) throws IOException {
	udpAddr = dstAddr;

	this.transmitting = transmitting;
	this.address = dstAddr.getAddress();
	this.port = dstAddr.getPort();

	setUpSocket();
    }

    /**
     * Set up the socket for the given addr/port,
     * and also a pre-prepared Datagrapacket.
     */
    void setUpSocket() throws IOException {
	socket = new DatagramSocket();

	// allocate an emtpy packet for use later
	packet = new DatagramPacket(new byte[1], 1);
	packet.setAddress(address);
	packet.setPort(port);
    }

    /**
     * Connect to the remote address now
     */
    public void connect()  throws IOException {
	// connect to the remote UDP socket
	socket.connect(udpAddr);

    }

    /**
     * End the connection to the remote address now
     */
    public void end()  throws IOException {
	// disconnect now
	socket.disconnect();
    }

    /**
     * Send a message to UDP address,  with a given id.
     */
    public int transmit(ByteArrayOutputStream byteStream, int id) throws IOException {
	// set up the packet
	packet.setData(byteStream.toByteArray());
	packet.setLength(byteStream.size());

	System.out.println(byteStream.size());
	// now send it
	socket.send(packet);

	//System.err.println("trans: " + id + " = " + byteStream.size());

	// notify the transmitting object
	if (transmitting != null) {
	    transmitting.transmitted(id);
	}

	return byteStream.size();
    }
}
