package eu.reservoir.monitoring.appl.vee.sap;

import java.util.Scanner;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.distribution.multicast.*;

public class SAPUsersMonitor 
{
	   // The DataSource
	   DataSource ds;

	   	public SAPUsersMonitor(String addr, int port, String fqn)
	   	{
			// set up data source
			ds = new BasicDataSource(fqn);
	
			// set up multicast address
			MulticastAddress dataGroup = new MulticastAddress(addr, port); 
	
			// set up data plane
			ds.setDataPlane(new MulticastDataPlaneProducer(dataGroup));
	
			ds.connect();
	   }

	   private void turnOnProbe(Probe p)
	   {
	       ds.addProbe(p);
	       ds.turnOnProbe(p);
	   }

	   private void turnOffProbe(Probe p) 
	   {
	       ds.turnOffProbe(p);
	       ds.removeProbe(p);
	   }
	   
	   public static void main(String [] args)
	   {
			if (args.length == 3)
			{
			    String fqn = args[0];
	
			    String addr = args[1];
	
			    Scanner sc = new Scanner(args[2]);
			    int port = sc.nextInt();
	
			    SAPUsersMonitor sapUsersMon = new SAPUsersMonitor(addr, port, fqn);
			    Probe usersProbe = new SAPUsersProbe(fqn);
			    sapUsersMon.turnOnProbe(usersProbe);
			} 
			else 
			{
			    System.err.println("SAPUsersMonitor [multicast-address, port] fullyQualifiedName");
			    System.exit(1);
			}
	   }
}
