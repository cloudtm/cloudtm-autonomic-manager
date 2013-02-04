// ProbeAttributeType.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

import eu.reservoir.monitoring.core.table.Table;
import eu.reservoir.monitoring.core.list.MList;
import java.util.HashMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * The type that an Attribute of a Probe can be.
 */
public enum ProbeAttributeType {
        BOOLEAN('Z'),
		BYTE('B'),
		CHAR('C'),
		SHORT('S'),
		INTEGER('I'),
		LONG('J'),
		FLOAT('F'),
		DOUBLE('D'),
		STRING('"'),
		BYTES(']'),
		TABLE('T'),
		LIST('L'),
		MAP('M');

    /*
     * The code for each ProbeAttributeType
     */
    byte code = 0;

    /*
     * A nasty hack required to do reverse lookup / from code
     */
    private static final HashMap<Byte, ProbeAttributeType> fromCodeMap = new HashMap<Byte, ProbeAttributeType>();

    // called at class initialization time
    static {
	// skip through every enum in ProbeAttributeType
	for (ProbeAttributeType type : EnumSet.allOf(ProbeAttributeType.class)) {
	    fromCodeMap.put(type.code, type);
	}
    }

    /*
     * Define the attribute type code
     */
    private ProbeAttributeType(char code) {
	this.code = (byte)code;
    }

    /**
     * Get the code for a ProbeAttributeType
     */
    public byte getCode() {
	return code;
    }

    /**
     * Get the correct ProbeAttributeType for a given code.
     */
    public static ProbeAttributeType fromCode(byte code) throws TypeException {
	ProbeAttributeType type = fromCodeMap.get(code);

	if (type != null) {
	    return type;
	} else {
	    throw new TypeException("Invalid ProbeAttributeType code: " + code);
	}
    }

    /**
     * A lookup function
     */
    public static ProbeAttributeType lookup(Object obj) throws TypeException {
    	if(obj == null){
    		int a = 0;
    		throw new TypeException("obj NULL");
    	}
    		
	if (String.class.isInstance(obj)) {
	    return STRING;
	} else if (Integer.class.isInstance(obj)) {
	    return INTEGER;
	} else if (Long.class.isInstance(obj)) {
	    return LONG;
	} else if (Float.class.isInstance(obj)) {
	    return FLOAT;
	} else if (Double.class.isInstance(obj)) {
	    return DOUBLE;
	} else if (Boolean.class.isInstance(obj)) {
	    return BOOLEAN;
	} else if (Byte.class.isInstance(obj)) {
	    return BYTE;
	} else if (Character.class.isInstance(obj)) {
	    return CHAR;
	} else if (Short.class.isInstance(obj)) {
	    return SHORT;
	} else if (Table.class.isInstance(obj)) {
	    return TABLE;
	} else if (MList.class.isInstance(obj)) {
	    return LIST;
	} else if (Map.class.isInstance(obj)) {
	    return STRING;
	} else {
	    throw new TypeException("Unsupported type " + obj.getClass().getName());
	}
    }
}
