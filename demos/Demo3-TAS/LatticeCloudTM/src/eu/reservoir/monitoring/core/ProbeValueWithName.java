// ProbeValue.java
// Author: Fabrizio Pastore
// Date: Feb 2010

package eu.reservoir.monitoring.core;

import eu.reservoir.monitoring.core.DefaultProbeValue;
import eu.reservoir.monitoring.core.TypeException;

/**
 * Constructs a ProbeValue that has its associated name
 * 
 * @author Fabrizio Pastore
 *
 */
public class ProbeValueWithName extends DefaultProbeValue {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;

        /**
         * Construct a ProbeValueWithName
         */
	public ProbeValueWithName(String name, int field, Object value) throws TypeException {
		super(field, value);
		this.name = name;
	}
	
        /**
         * Get the name for an attribute.
         */
	public String getName() {
		return name;
	}

        /**
         * Set the name for an attribute.
         */
	public void setName(String name) {
		this.name = name;
	}

    /**
     * To string
     */
    public String toString() {
	return field + ": " + name + " " + type + " " + value;
    }


}
