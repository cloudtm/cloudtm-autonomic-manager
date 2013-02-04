// ListTest2.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.core.list.*;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.TypeException;

/**
 * A test of the MList type.
 */
public class ListTest2 {
    public static void main(String[] args) throws TypeException {
	// allocate a list
	MList mList = new DefaultMList(ProbeAttributeType.INTEGER);

	mList.add(1).add(2).add(3);

	System.out.println(mList);


	System.out.println("Adding the wrong type should cause an Exception");

	mList.add("hello");

	// never get here
	System.out.println(mList);
    }
}

