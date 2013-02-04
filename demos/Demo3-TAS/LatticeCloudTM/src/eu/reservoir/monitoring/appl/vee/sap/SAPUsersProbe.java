package eu.reservoir.monitoring.appl.vee.sap;

import java.util.ArrayList;

import eu.reservoir.monitoring.appl.datarate.EveryNSeconds;
import eu.reservoir.monitoring.core.*;

public class SAPUsersProbe extends AbstractProbe implements Probe
{
	public SAPUsersProbe(String name)
	{
		// set name
        setName(name);

        // set service ID
        setServiceID(new ID(21)); 
       	// set service Group ID
       	setGroupID(new ID(24));

       	// set data rate
        setDataRate(new EveryNSeconds(10));

        // define probe Attribute
        addProbeAttribute(new DefaultProbeAttribute(0, "Value", ProbeAttributeType.INTEGER, "n"));
        addProbeAttribute(new DefaultProbeAttribute(1, "FQN", ProbeAttributeType.STRING, "name"));

        // activate probe
        activateProbe();
	}
	

	public ProbeMeasurement collect()
	{
		try
		{
		    ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(1);

		    //extract KPI from SAP System
		    int totalUsers = (int)(Math.random()*100);

		    // add values to list
		    list.add(new DefaultProbeValue(0, totalUsers));
		    list.add(new DefaultProbeValue(1, "com.sap.ci.totalUsers"));

		    // create a measurement based on the values
		    return new ProducerMeasurement(this, list, "com.sap.ci");
		} catch (Exception e) {
		    return null;
		}
	}

}
