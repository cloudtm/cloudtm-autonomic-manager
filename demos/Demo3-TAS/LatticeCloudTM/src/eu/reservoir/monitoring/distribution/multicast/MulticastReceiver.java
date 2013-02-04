// MulticastReceiver.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.distribution.multicast;

import eu.reservoir.monitoring.distribution.*;
import eu.reservoir.monitoring.core.TypeException;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * This is a multicast receiver for monitoring messages.
 */
public class MulticastReceiver implements Runnable {
    /*
     * The receiver that interactes messages.
     */
    Receiving receiver = null;

    /*
     * The multicast socket being received from
     */
    MulticastSocket msocket;

    /*
     * A packet to receive
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
     * The MulticastAddress, which combines InetAddress and port
     */
    MulticastAddress multicastAddress;

    /*
     * My thread.
     */
    Thread myThread;

    boolean threadRunning = false;

    /*
     * A default packet size.
     */
    static int PACKET_SIZE = 1500;

    /*
     * The packet contents as a ByteArrayInputStream
     */
    ByteArrayInputStream byteStream;

    /*
     * The InetAddress of the last packet received
     */
    InetAddress ipAddr;

    /*
     * The length of the last packet received
     */
    int length;

    /*
     * The last exception received.
     */
    Exception lastException;


    /**
     * Construct a receiver for a particular multicast address
     */
    public MulticastReceiver(Receiving receiver, MulticastAddress mcastAddr) throws IOException {
	multicastAddress = mcastAddr;

	this.receiver = receiver;
	address = mcastAddr.getAddress();
	this.port = mcastAddr.getPort();

	setUpSocket();
    }

    /**
     * Set up the socket for the given addr/port,
     * and also a pre-prepared DatagramPacket.
     */
    void setUpSocket() throws IOException {
	// TODO: check if port should be there
	msocket = new MulticastSocket(port);

	// allocate an emtpy packet for use later
	mpacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
    }

    /**
     * Join the address now
     * and start listening
     */
    public void join()  throws IOException {
	// join the multicast group
	msocket.joinGroup(address);

	// start the thread
	myThread = new Thread(this);

	myThread.start();
    }

    /**
     * Leave the address now
     * and stop listening
     */
    public void leave()  throws IOException {
	// stop the thread
	threadRunning = false;

	// leave the multicast group
	msocket.leaveGroup(address);
    }

    /**
     * Receive a  message from the multicast address.
     */
    protected boolean receive() {
	try {
	    // clear lastException
	    lastException = null;

	    // receive from socket
	    msocket.receive(mpacket);

	    /* System.out.println("Received " + mpacket.getLength() +
			   " bytes from "+ mpacket.getAddress() + 
			   "/" + mpacket.getPort()); 
	    */


	     /* System.out.print("Interface: " + msocket.getInterface() + "/" + msocket.getNetworkInterface()); */

	    // filter out packets to wrong address but right port
	    // you cant in Java :-0
	    // if (! mpacket.getAddress().equals(address)) {
	    // }

	    // get an input stream over the data bytes of the packet
	    ByteArrayInputStream theBytes = new ByteArrayInputStream(mpacket.getData(), 0, mpacket.getLength());

	    byteStream = theBytes;
	    ipAddr = mpacket.getAddress();
	    length = mpacket.getLength();

	    // Currently we reuse the packet.
	    // This could be dangerous.

	    // Maybe we should do this
	    // allocate an emtpy packet for use later
	    // mpacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

	    // Reset the packet size for next time
	    mpacket.setLength(PACKET_SIZE);


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
		MulticastTransmissionMetaData metaData = new MulticastTransmissionMetaData(length, ipAddr, multicastAddress);


		// now notify the receiver with the message
		// and the multicast address it came in on
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
