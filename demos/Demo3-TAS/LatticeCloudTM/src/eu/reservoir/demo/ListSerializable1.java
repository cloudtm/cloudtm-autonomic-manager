// ListSerializable1.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.core.list.*;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.TypeException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * A test of the MList type.
 */
public class ListSerializable1 {
    public static void main(String[] args) throws TypeException {
	// allocate a list
	MList mList = new DefaultMList(ProbeAttributeType.INTEGER);

	mList.add(1).add(2).add(3);

	System.out.println(mList);

	System.out.println("serialize: ");
	byte[] bytes = construct(mList);
	System.out.println((bytes.length+"") + ": " + new String(bytes));

    }

    private static byte[] construct(Serializable object) {
	// Create a ByteArrayOutputStream to store the serialized 
	// version of the objects
	ByteArrayOutputStream bos = new ByteArrayOutputStream();

	try {
	    // Wrap the ByteArrayOutputStream in an ObjectOutputStream
	    ObjectOutputStream oos = new ObjectOutputStream(bos);
	    // and write the objects to the stream
	    oos.writeObject(object);
	    oos.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new Error("Failed to write serializable data for " + object);
	}

	// the bytes are now held in the ByteArrayOutputStream
	// so we get the bytes of the ByteArrayOutputStream
	byte[] rawBytes = bos.toByteArray();

	return rawBytes;

    }

}

