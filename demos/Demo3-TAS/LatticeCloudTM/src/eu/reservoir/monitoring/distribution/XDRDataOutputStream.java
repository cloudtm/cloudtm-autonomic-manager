// XDRDataOutputStream.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.distribution;

import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * An OutputStream that has XDR encoded data in it.
 */
public class XDRDataOutputStream extends FilterOutputStream implements DataOutput {
    // the no of bytes written
    int size = 0;

    /**
     * Construct an XDRDataOutputStream.
     */
    public XDRDataOutputStream(OutputStream out) {
	super(out);
    }

    /**
     * Writes the specified byte to this output stream.
     */
    public void write(int b) throws IOException {
	super.write(b);
	size++;
    }

    public int size() {
	return size;
    }

    /**
      * Put a boolean onto the stream.
      * A boolean is padded out to 4 bytes.
      */
    public void writeBoolean(boolean b) throws IOException {
	if(b) {
	    writeInt(1);
	} else {
	    writeInt(0);
	}
    }

    /**
      * Put a single byte onto the stream.
      * 4 bytes will be output.
      */
    public void writeByte(byte b) throws IOException {
	writeInt((int)b);
    }

    /**
      * Put a single byte onto the stream.
      * 4 bytes will be output.
      * For DataOutput.
      */
    public void writeByte(int i) throws IOException {
	writeInt(i);
    }

    /**
     * Put a char onto the stream.
     * 4 bytes will be output.
     */
    public void writeChar(char i) throws IOException {
	write((byte)((i >> 8) & 0xff));
	write((byte)(i & 0xff));
	pad(2);
   }

    /**
     * Put a char onto the stream.
     * 4 bytes will be output.
     * For DataOutput.
     */
    public void writeChar(int i) throws IOException {
	writeInt(i);
   }

    /**
     * Put a short onto the stream.
     * 4 bytes will be output.
     */
    public void writeShort(short i) throws IOException {
	write((byte)((i >> 8) & 0xff));
	write((byte)(i & 0xff));
	pad(2);
   }

    /**
     * Put a short onto the stream.
     * 4 bytes will be output.
     * For DataOutput.
     */
    public void writeShort(int i) throws IOException {
	writeInt(i);
   }

    /**
     * Put an integer onto the stream.
     * 4 bytes will be output.
     */
    public void writeInt(int i) throws IOException {
 	write((byte)((i >> 24) & 0xff));
	write((byte)((i >> 16) & 0xff));
	write((byte)((i >> 8) & 0xff));
	write((byte)(i & 0xff));
   }

    /**
      * Put a long onto the stream.
      * 8 bytes will be consumed.
      */
    public void writeLong(long l) throws IOException {
 	write((byte)((l >> 56) & 0xff));
 	write((byte)((l >> 48) & 0xff));
	write((byte)((l >> 40) & 0xff));
	write((byte)((l >> 32) & 0xff));
 	write((byte)((l >> 24) & 0xff));
	write((byte)((l >> 16) & 0xff));
	write((byte)((l >> 8) & 0xff));
	write((byte)(l & 0xff));
    }

    /**
      * Put a floating point number (as 32 bits) onto the stream.
      * It is written in IEEE 754 floating-point "single format" bit layout.
      * 4 bytes will be output.
      */
    public void writeFloat(float f) throws IOException {
	writeInt(Float.floatToIntBits(f));
    }

    /**
      * Put a double precision floating point number (as 64 bits) onto the stream.
      * It is written in the IEEE 754 floating-point "double format" bit layout.
      * 8 bytes will be output.
      * @param d The double precision floating point number to add to the
      * stream
      */
    public void writeDouble(double d) throws IOException {
	writeLong(Double.doubleToLongBits(d));
    }


    /**
      * Put a String represented as a variable length opaque data item.
      * The number of bytes consumed will be 4 (for the length), plus the 
      * number of bytes in the String, with padding rounded up
      * to the next multiple of 4.
      */
    public void writeString(String str) throws IOException {
	byte [] data = str.getBytes();
	writeBytes(data, 0, data.length);
    }

    /**
      * Put a String represented as a variable length opaque data item.
      * The number of bytes consumed will be 4 (for the length), plus the 
      * number of bytes in the String, with padding rounded up
      * to the next multiple of 4.
      * For DataOutput.
      */
    public void writeUTF(String str) throws IOException {
	byte [] data = str.getBytes();
	writeBytes(data, 0, data.length);
    }

    /**
      * Put a String represented as a variable length opaque data item.
      * The number of bytes consumed will be 4 (for the length), plus the 
      * number of bytes in the String, with padding rounded up
      * to the next multiple of 4.
      * For DataOutput.
      */
    public void writeChars(String str) throws IOException {
	byte [] data = str.getBytes();
	writeBytes(data, 0, data.length);
    }

    /**
      * Put a String represented as a variable length opaque data item.
      * The number of bytes consumed will be 4 (for the length), plus the 
      * number of bytes in the String, with padding rounded up
      * to the next multiple of 4.
      * For DataOutput.
      */
    public void writeBytes(String str) throws IOException {
	byte [] data = str.getBytes();
	writeBytes(data, 0, data.length);
    }

    /**
      * Put a array of bytes onto the stream as a variable length opaque data item.
      * The number of bytes consumed will be 4 (for the length), plus the 
      * number of bytes in the String, with padding rounded up
      * to the next multiple of 4.
      */
    public void writeBytes(byte[] data) throws IOException {
	writeBytes(data, 0, data.length);
    }

   /**
      * Put a array of bytes onto the stream as a variable length opaque data item.
      * The number of bytes consumed will be 4 (for the length), plus the 
      * number of bytes in the String, with padding rounded up
      * to the next multiple of 4.
      * @param data The array which the data is taken from
      * @param bufferOffset The position in the array where copying starts
      * @param len The number of bytes to copy to the buffer
      */
    public void writeBytes(byte[] data, int bufferOffset, int len) throws IOException {
	writeInt(len);
	write(data, bufferOffset, len);
	int pad = pad(len);
	//System.err.println("writeBytes: pad = " + pad + " total = " + (4 + len + pad));
    }

    /**
      * Put a array of bytes onto the stream as a variable length opaque data item.
      * The number of bytes consumed will be 4 (for the length), plus the 
      * number of bytes in the String, with padding rounded up
      * to the next multiple of 4.
      * @param data The array which the data is taken from
      * @param len The number of bytes to put onto the stream
      */
    public void writeBytes(byte[] data, int len) throws IOException {
	writeBytes(data, 0, len);
    }

    /**
      * Put a array of bytes onto the stream as a fixed length opaque data item.
      * The number of bytes consumed will be the 
      * number of bytes in the array,  with padding rounded up
      * to the next multiple of 4.
      */
    public void writeFixed(byte[] data) throws IOException {
	writeFixed(data, 0, data.length);
    }

   /**
      * Put a array of bytes onto the stream as a fixed length opaque data item.
      * The number of bytes consumed will be the 
      * number of bytes in the array,  with padding rounded up
      * to the next multiple of 4.
      * @param data The array which the data is taken from
      * @param bufferOffset The position in the array where copying starts
      * @param len The number of bytes to copy to the buffer
      */
    public void writeFixed(byte[] data, int bufferOffset, int len) throws IOException {
	write(data, bufferOffset, len);
	int pad = pad(len);
	//System.err.println("writeFixed: pad = " + pad + " total = " + (pad + len));
    }

    /**
      * Put a array of bytes onto the stream as a fixed length opaque data item.
      * The number of bytes consumed will be the 
      * number of bytes in the array,  with padding rounded up
      * to the next multiple of 4.
      * @param data The array which the data is taken from
      * @param len The number of bytes to put onto the stream
      */
    public void writeFixed(byte[] data, int len) throws IOException {
	writeFixed(data, 0, len);
    }

    /* 
     * Work out padding.
     * Return no of pad bytes added to stream.
     */
    private int pad(int len) throws IOException {
	int lenMod = len %4;

	if(lenMod > 0) {
	    int pad =  4 - lenMod;

	    for (int p=0; p < pad; p++) {
		write((byte)0);
	    }

	    return pad;
	} else {
	    return 0;
	}
    }

    /**
     * To string.
     */
    public String toString() {
	return "XDRDataOutputStream@" + hashCode() + "/" + size();
    }
}