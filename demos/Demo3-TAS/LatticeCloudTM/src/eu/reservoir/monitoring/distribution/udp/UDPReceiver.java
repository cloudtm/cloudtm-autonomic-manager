// UDPReceiver.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2009

package eu.reservoir.monitoring.distribution.udp;

import eu.reservoir.monitoring.distribution.*;
import eu.reservoir.monitoring.core.TypeException;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * This is a UDP receiver for monitoring messages.
 */
public class UDPReceiver implements Runnable {
    /*
     * The receiver that interactes messages.
     */
    Receiving receiver = null;

    /*
     * The socket doing the listening
     */
    DatagramSocket socket;

    /*
     * A packet to receive
     */
    DatagramPacket packet;

    /*
     * The IP address
     */
    InetSocketAddress address;

    InetAddress dstAddr;

    /*
     * The port
     */
    int port;

    /*
     * My thread.
     */
    Thread myThread;

    boolean threadRunning = false;

    /*
     * A default packet size.@@ROB
     */
    static int PACKET_SIZE = 65000;

    /*
     * The packet contents as a ByteArrayInputStream
     */
    ByteArrayInputStream byteStream;

    /*
     * The InetSocketAddress of the last packet received
     */
    InetAddress srcAddr;

    /*
     * The length of the last packet received
     */
    int length;

    /*
     * The last exception received.
     */
    Exception lastException;


    /**
     * Construct a receiver for a particular IP address
     */
    public UDPReceiver(Receiving receiver, InetSocketAddress ipAddr) throws IOException {
	address = ipAddr;

	this.receiver = receiver;
	this.dstAddr = ipAddr.getAddress();
	this.port = ipAddr.getPort();

	setUpSocket();
    }

    /**
     * Set up the socket for the given addr/port,
     * and also a pre-prepared DatagramPacket.
     */
    void setUpSocket() throws IOException {
	socket = new DatagramSocket(port);

	// allocate an emtpy packet for use later
	packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
    }

    /**
     * Join the address now
     * and start listening
     */
    public void listen()  throws IOException {
	// already bind to the address
	//socket.bind(address);

	// start the thread
	myThread = new Thread(this);

	myThread.start();
    }

    /**
     * Leave the address now
     * and stop listening
     */
    public void end()  throws IOException {
	// stop the thread
	threadRunning = false;

	// disconnect
	socket.disconnect();
    }

    /**
     * Receive a  message from the multicast address.
     */
    protected boolean receive() {
	try {
	    // clear lastException
	    lastException = null;

	    // receive from socket
	    socket.receive(packet);

	    /* System.out.println("UDPReceiver Received " + packet.getLength() +
			   " bytes from "+ packet.getAddress() + 
			   "/" + packet.getPort()); 
	    */

	    // get an input stream over the data bytes of the packet
	    ByteArrayInputStream theBytes = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());

	    byteStream = theBytes;
	    srcAddr = packet.getAddress();
	    length = packet.getLength();

	    // Currently we reuse the packet.
	    // This could be dangerous.

	    // Maybe we should do this
	    // allocate an emtpy packet for use later
	    // packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

	    // Reset the packet size for next time
	    packet.setLength(PACKET_SIZE);


	    return true;
	} catch (Exception e) {
	    // something went wrong
	    lastException = e;
	    return false;
	}
    }

    /**
     * The Runnable body
     */
    public void run() {
	// if we get here the thread must be running
	threadRunning = true;

	while (threadRunning) {

	    if (receive()) {
		// construct the transmission meta data
		UDPTransmissionMetaData metaData = new UDPTransmissionMetaData(length, srcAddr, dstAddr);


		// now notify the receiver with the message
		// and the address it came in on
		try {
		    receiver.received(byteStream, metaData);
		} catch (IOException ioe) {
		    receiver.error(ioe);
		} catch (TypeException te) {
		    receiver.error(te);
		}
	    } else {
		// the receive() failed
		// we find the exception in lastException
		receiver.error(lastException);
	    }
	}
    }
	    

}
