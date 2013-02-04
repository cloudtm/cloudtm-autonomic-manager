// XDRInputStream.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.distribution;

import java.io.FilterInputStream;
import java.io.DataInput;
import java.io.InputStream;
import java.io.IOException;

/**
 * An InputStream that has XDR encoded data in it.
 */
public class XDRDataInputStream extends FilterInputStream implements DataInput {
    /**
     * Construct an XDRDataInputStream.
     */
    public XDRDataInputStream(InputStream ins) {
	super(ins);
    }

    /**
      * Get a boolean from the stream.
      * 4 bytes will be input.
      */
    public boolean readBoolean() throws IOException {
	return (readInt() == 1);
    }

    /**
      * Get a byte from the stream.
      * 4 bytes will be input.
      */
    public byte readByte() throws IOException {
	read();
	read();
	read();
	return (byte)(read() & 0xff);
    }

    /**
      * Get a byte from the stream.
      * 4 bytes will be input.
      */
    public int readUnsignedByte() throws IOException {
	return readInt();
    }

    /**
      * Get a char from the stream.
      * 4 bytes will be input.
      */
    public char readChar() throws IOException {
	read();
	read();
	return (char)(((read() & 0xff) << 8) | (read() & 0xff));
    }


    /**
      * Get a short from the stream.
      * 4 bytes will be input.
      */
    public short readShort() throws IOException {
	read();
	read();
	return (short)(((read() & 0xff) << 8) | (read() & 0xff));
    }

    /**
      * Get a short from the stream.
      * 4 bytes will be input.
      */
    public int readUnsignedShort() throws IOException {
	return readInt();
    }

    /**
      * Get an integer from the stream.
      * 4 bytes will be input.
      */
    public int readInt() throws IOException {
	int value = 
	    ((read() & 0xff) << 24) |
	    ((read() & 0xff) << 16) |
	    ((read() & 0xff) << 8) |
	    ((read() & 0xff));
	return value;
    }

    /**
      * Get a long from the stream.
      * 8 bytes will be input.
      */
    public long readLong() throws IOException {
	long value =
	    ((long)(read() & 0xff) << 56) |
	    ((long)(read() & 0xff) << 48) |
	    ((long)(read() & 0xff) << 40) |
	    ((long)(read() & 0xff) << 32) |
	    ((long)(read() & 0xff) << 24) |
	    ((long)(read() & 0xff) << 16) |
	    ((long)(read() & 0xff) << 8) |
	    ((long)(read() & 0xff));
	return value;
    }

    /**
      * Get a float from the stream.
      * 4 bytes will be input.
      */
    public float readFloat() throws IOException {
	int value = readInt();
	return Float.intBitsToFloat(value);
   }


    /**
      * Get a double from the stream.
      * 4 bytes will be input.
      */
    public double readDouble() throws IOException {
	long value = readLong();
	return Double.longBitsToDouble(value);
   }

   /**
    * Get a counted (ie variable length) array of bytes from the stream.
    * The number of bytes consumed will be 4 (for the length) plus the 
    * number of bytes in the array, padded to the next multiple of 4.
    */
    public byte[] readBytes() throws IOException {
	// read the size of the array
	int len = readInt();
	// allocate a new byte[]
	byte[] data = new byte[len];
	// read it into data
	read(data, 0, len);
	// Read the padding
	pad(len);
	// return the byte[]
	return data;
    }

    /**
     * Reads some bytes from an input stream and stores them into the
     * buffer array b. The number of bytes read is equal to the length
     * of b.
     */
    public void readFully(byte[] b) throws IOException {
	if (b == null) {
	    // If b is null, a NullPointerException is thrown. 

	    throw new NullPointerException();

	} else {
	    if (b.length == 0) {
		// if b.length is zero, then no bytes are read.
		// read it into b

		return;
	    } else {
		// try and read soem bytes
		read(b, 0, b.length);
	    }
	}
    }

    /**
     * Reads len bytes from an input stream.
     */
    public void readFully(byte[] b, int off, int len) throws IOException {
	// If b is null, a NullPointerException is thrown. 
	if (b == null) {
	    throw new NullPointerException();
	} else {
	    if (b.length == 0 || len == 0) {
		// if b.length is zero, then no bytes are read.
		// read it into b
		// If len is zero, then no bytes are read. 

		return;

	    } else if (off < 0 || len < 0 || (off+len) > b.length) {
		// If off is negative, or len is negative,
		// or off+len is greater than the length of the array b,
		// then an IndexOutOfBoundsException is thrown. 

		throw new IndexOutOfBoundsException();

	    } else {
		// try and read soem bytes
		read(b, off, len);
	    }
	}
    }


    /**
     * Read a string from the stream.
     */
    public String readStream() throws IOException {
	byte[] data = readBytes();
	return new String(data);
    }

    /**
     * Read a string from the stream.
     * For DataInput.
     */
    public String readUTF() throws IOException {
	byte[] data = readBytes();
	return new String(data);
    }

    /**
     * Read a line from the input.
     */
    public String readLine() throws IOException {
	throw new UnsupportedOperationException("XDRDataInputStream: readLine() is deprecated and not supported");
    }

   /**
    * Get a fixed array of bytes from the stream.
    * The number of bytes consumed 
    * number of bytes in the array, padded to the next multiple of 4.
    */
    public byte[] readFixed(int len) throws IOException {
	// allocate a new byte[]
	byte[] data = new byte[len];
	// read it into data
	read(data, 0, len);
	// read the padding
	pad(len);
	// return the byte[]
	return data;
    }

    /**
     * Make an attempt to skip some bytes in the input.
     */
    public int skipBytes(int n) throws IOException {
	int i=0;

	for (i=0; i<n; i++) {
	    read();
	}

	return i+1;
    }

    /* 
     * Work out padding.
     * Return no of pad bytes read from stream.
     */
    private int pad(int len) throws IOException {
	int lenMod = len %4;

	if(lenMod > 0) {
	    int pad =  4 - lenMod;

	    for (int p=0; p < pad; p++) {
		read();
	    }

	    return pad;
	} else {
	    return 0;
	}
    }


}