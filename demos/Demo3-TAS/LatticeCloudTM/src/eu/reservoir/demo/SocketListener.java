// SocketListener.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

package eu.reservoir.demo;

import java.util.Set;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.nio.channels.*;
import java.nio.*;
import java.nio.charset.Charset;

/**
 * This listens on a Socket for new connections
 * and reads data from them.
 */
public class SocketListener implements Runnable {
    SocketProbe probe;
    ServerSocket socket;
    Selector selector;
    Thread thread;
    boolean running = false;

    // read data from channel into the buffer
    ByteBuffer buffer = ByteBuffer.allocate(4096);

    Charset charset = Charset.forName("UTF-8");

    /**
     * Listen on a Socket.
     */
    public SocketListener(ServerSocket s, SocketProbe p) {
        socket = s;
        probe = p;

        thread = new Thread(this);
        running = true;
        thread.start();

    }

    /**
     * Set up.
     */
    void setUp() {
        try {
            selector = Selector.open();

            // get channel for main socket
            ServerSocketChannel ssc = socket.getChannel();

            // check channel
            if (ssc == null) {
                System.err.println("SocketListener: no channel for " + socket);
                probe.deactivateProbe();
            } else {
                // set channel to be non-blocking
                ssc.configureBlocking(false);

                System.err.println("Registering " + ssc);

                // register this channel with the selector
                ssc.register( selector, SelectionKey.OP_ACCEPT );

                System.err.println("SocketListener: ready to accept on " + socket);
            }
        } catch (IOException ioe) {
            // cant process socket, so
            // tell the probe to end
            probe.deactivateProbe();
        }
    }

    /**
     * shutDown
     */
    void shutDown() {
        System.err.println("SocketListener: shutdown");

        try {
            // close main socket
            socket.close();
        } catch (Exception e) {
        }
    }

    /**
     * terminate
     */
    void terminate() {
        running = false;
    }

    /**
     * Start reading from a new connection.
     */
    void newConnection(Socket s) {
        try {
            // Make sure channel for new connection is nonblocking
            SocketChannel sc = s.getChannel();
            sc.configureBlocking( false );
            System.err.println("Registering " + sc);
            // Register it with the Selector, for reading.
            sc.register( selector, SelectionKey.OP_READ );
        } catch (IOException ioe) {
            System.err.println("Error on Socket " + s);
            try {
                s.close();
            } catch (Exception e) {
            }
        }

    }

    /**
     * End a connection
     */
    void endConnection(SocketChannel c) {
        // close a remote connection
        try {
            c.close();
        } catch (IOException ioe) {
        }
    }

    /**
     * Process some input.
     * This reads everything that is available, and sends it immediately.
     * This does not deal with end of line or end of input delineator.
     */
    void processInput(SocketChannel sc) {
        int read = -1;

        // read some data
        try {
            buffer.clear();
            read = sc.read(buffer);
        } catch (IOException ioe) {
        }

        // check what was read
        if (read != -1) {
            buffer.flip();

            // convert buffer to string
            String value = charset.decode(buffer).toString().trim();

            // inform the probe
            probe.passData(value);
        } else {
            // EOF for the connection
            System.err.println("channel EOF: " + sc);
            endConnection(sc);
        }
    }

    /**
     * The main thread loop.
     */
    public void run() {
        setUp();

        while (running) {
            try {
                // select() on all channels
                int num = selector.select();

                // did select() return with no values ?
                if (num == 0) {
                    // go again
                    continue;
                } else {
                    // check what is ready

                    Set<SelectionKey> keys = selector.selectedKeys();

                    for (SelectionKey key : keys) {
                        if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                            // do we have an accept

                            System.err.println("About to accept on: " + socket);
                            // Accept the incoming connection.
                            Socket local = socket.accept();

                            // Deal with incoming connection
                            newConnection(local);

                        } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                            // do we have a read
                            // get the channel
                            SocketChannel sc = (SocketChannel)key.channel();

                            // and process some input from it
                            processInput( sc );
                        } else {
                            System.err.println("Unexpected behaviour on " + key);
                        }

                    }

                    // Remove the selected keys because you've dealt
                    // with them.
                    keys.clear();

                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        
        shutDown();
    }

}