package eu.cloudtm.reporter.manager;

/**
 * The resource interface
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public interface Resource {

   public static final double UNKNOWN_DOUBLE_ATTR = -1;
   public static final long UNKNOWN_LONG_ATTR = -1;
   public static final int UNKNOWN_INT_ATTR = -1;
   public static final String UNKNOWN_STRING_ATTR = "#";

   double getCpuUsage();

   long getMemoryUsage();

   long getMemoryFree();

   double getDoubleIspnAttribute(String name);

   long getLongIspnAttribute(String name);

   int getIntIspnAttribute(String name);

   String getStringIspnAttribute(String name);

}
