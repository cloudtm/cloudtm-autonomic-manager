// QueueLengthProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.appl.vee.sge;

import eu.reservoir.monitoring.core.*;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;


/**
* A probe for gettting the queue length.
* <p>
* It needs to emulate a REST agent that sends:
* <pre>
* <MonitoringInformation> 
*   <EventType>Agent</EventType> 
*   <EpochTimestamp>25547674</EpochTimestamp> 
*   <TimeDelta>0</TimeDelta> 
*   <FQN>sun.services.sge.kpis.queueSize</FQN> 
*   <Value>45</Value> 
* </MonitoringInformation>
* </pre>
* <p>
* A measurement already has:  a timestamp, a time delta, and a type.
* For this probe we will send the queue length, and the FQN.
* 
*/
public class QueueLengthProbe extends AbstractProbe implements Probe  {
    File qlengthfile = new File("/opt/test/qlength"); ////VG: the queue length is stored in this file pericodically by the monitoring script

   /*
    * Construct a probe
    */
   public QueueLengthProbe(String name) {
	// set name
       setName(name);
       // set service ID
       setServiceID(new ID(0)); // should set real one

       // data rate is 360 measurements per hour, 
       // equivalent to once every 10 seconds
       setDataRate(new Rational(360, 1));

       // define attributes
       addProbeAttribute(new DefaultProbeAttribute(0, "Value", ProbeAttributeType.INTEGER, "n"));
       addProbeAttribute(new DefaultProbeAttribute(1, "FQN", ProbeAttributeType.STRING, "name"));

       // activate the Probe
       activateProbe();
   }

   /**
    * Collect a measurement.
    */
   public ProbeMeasurement collect() { //VG: reimplemented to read the SGE queue length value from a file.
       try {
	   ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(3);

	   // make up a queue length
	   int queueLength = readQlength( qlengthfile );

	   // add values to list
	   list.add(new DefaultProbeValue(0, queueLength));
	   list.add(new DefaultProbeValue(1, getName()));

	   // create a measurement based on the values
	   return new ProducerMeasurement(this, list, "Agent");
       } catch (Exception e) {
	   // on error, return a null
	   return null;
       }
   }

   private int readQlength(File qlengthf) {
       int qL;

       try {
	   BufferedReader reader = new BufferedReader(new FileReader(qlengthf));
	   String s =reader.readLine();
	   qL=Integer.parseInt(s);
	   reader.close();

	   return qL;
       } catch (Exception e) {
	   // something went wrong
	   System.err.println("readQlength: " + e);
	   return 0;
       }
   }
}