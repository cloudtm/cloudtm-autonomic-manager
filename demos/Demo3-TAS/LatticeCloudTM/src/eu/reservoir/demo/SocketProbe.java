// SocketProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

package eu.reservoir.demo;

import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.appl.datarate.*;
import java.util.ArrayList;
import java.nio.channels.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.IOException;

/**
 * A probe that listens on a socket for a string
 * and returns that as a measurement.
 */
public class SocketProbe extends AbstractProbe implements Probe  {
    // a server socket
    ServerSocketChannel channel = null;
    ServerSocket socket = null;
    SocketListener listener = null;
    int port;

    String data;

    /*
     * Construct a probe
     */
    public SocketProbe(String name, int port) {
        this.port = port;
        setName(name);
        addProbeAttribute(new DefaultProbeAttribute(0, "value", ProbeAttributeType.STRING, ""));
    }

    /**
     * At the start of the  thread we open a ServerSocketChannel
     * and create an object that listens on that socket.
     */
    public void beginThreadBody() {
        try {
            // Create a non-blocking server socket channel on port
            channel = ServerSocketChannel.open();
            socket = channel.socket();
            //channel.configureBlocking(false);
            socket.bind(new InetSocketAddress(port));

            System.err.println("SocketProbe: listening on port " + port);
            
            listener = new SocketListener(socket, this);
        } catch (IOException ioe) {
            // could not get socket, so there is nothing to do
            stopProbeThread();
        }
    }

    /**
     * At the end of the  thread we terminate the listener
     * and close the ServerSocketChannel.
     */
    public void endThreadBody() {
        System.err.println("SocketProbe: ending");
        listener.terminate();
    }

    /**
     * The listener passes in some data using this method.
     */
    public void passData(String s) {
        data = s;
        // we inform ourself that something has to happen
        inform(s);
    }

    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	try {
	    ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(1);

	    list.add(new DefaultProbeValue(0, data));

	    return new ProducerMeasurement(this, list);
	} catch (Exception e) {
	    System.err.println(e);
	    e.printStackTrace();
	    return null;
	}
    }


}