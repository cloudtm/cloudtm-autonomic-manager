package eu.cloudtm.reporter.customattributes;

import eu.cloudtm.reporter.manager.Resource;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 4.0
 */
public class RadargunWorkload implements CustomAttribute {

   private final Parameter[] parameters = new Parameter[] {
         new DoubleParameter("PaymentPercentage", "getPaymentWeight"),
         new DoubleParameter("OrderStatusPercentage", "getOrderStatusWeight"),
         new StringParameter("WriteOpsPerWriteTx", "getWrtOpsPerWriteTx"),
         new StringParameter("ReadOpsPerWriteTx", "getRdOpsPerWriteTx"),
         new StringParameter("ReadOpsPerReadTx", "getRdOpsPerReadTx"),
         new IntegerParameter("NumberOfKeys", "getNumberOfKeys"),
         new IntegerParameter("Threads", "getNumberOfActiveThreads"),
         new DoubleParameter("ExpectedWritePercentage", "getExpectedWritePercentage"),
         new StringParameter("Protocol", "currentProtocolId")
   };

   @Override
   public String[] getHeaders() {
      String[] headers = new String[parameters.length];
      for (int i = 0; i < parameters.length; ++i) {
         headers[i] = parameters[i].headerName;
      }
      return headers;
   }

   @Override
   public String[] getValues() {
      String[] value = new String[parameters.length];
      for (int i = 0; i < parameters.length; ++i) {
         value[i] = parameters[i].get();
         parameters[i].reset();
      }
      return value;
   }

   @Override
   public String[] getAttributes() {
      String[] attributes = new String[parameters.length];
      for (int i = 0; i < parameters.length; ++i) {
         attributes[i] = parameters[i].attributeName;
      }
      return attributes;
   }

   @Override
   public void update(Resource resource) {
      for (Parameter parameter : parameters) {
         parameter.update(resource);
      }
   }

   private abstract class Parameter {
      final String headerName;
      final String attributeName;

      protected Parameter(String headerName, String attributeName) {
         this.headerName = headerName;
         this.attributeName = attributeName;
         reset();
      }

      abstract void reset();
      abstract void update(Resource resource);
      abstract String get();
   }

   private class DoubleParameter extends Parameter {

      private double value;

      private DoubleParameter(String headerName, String attributeName) {
         super(headerName, attributeName);
      }

      @Override
      void reset() {
         value = Resource.UNKNOWN_DOUBLE_ATTR;
      }

      @Override
      void update(Resource resource) {
         if (value == Resource.UNKNOWN_DOUBLE_ATTR) {
            value = resource.getDoubleIspnAttribute(attributeName);
         }
      }

      @Override
      String get() {
         return value == Resource.UNKNOWN_DOUBLE_ATTR ? "" : Double.toString(value);
      }
   }

   private class IntegerParameter extends Parameter {

      private int value;

      private IntegerParameter(String headerName, String attributeName) {
         super(headerName, attributeName);
      }

      @Override
      void reset() {
         value = Resource.UNKNOWN_INT_ATTR;
      }

      @Override
      void update(Resource resource) {
         if (value == Resource.UNKNOWN_INT_ATTR) {
            value = resource.getIntIspnAttribute(attributeName);
         }
      }

      @Override
      String get() {
         return value == Resource.UNKNOWN_INT_ATTR ? "" : Integer.toString(value);
      }
   }

   private class StringParameter extends Parameter {

      private String value;

      private StringParameter(String headerName, String attributeName) {
         super(headerName, attributeName);
      }

      @Override
      void reset() {
         value = Resource.UNKNOWN_STRING_ATTR;
      }

      @Override
      void update(Resource resource) {
         if (value.equals(Resource.UNKNOWN_STRING_ATTR)) {
            value = resource.getStringIspnAttribute(attributeName);
         }
      }

      @Override
      String get() {
         return value.equals(Resource.UNKNOWN_STRING_ATTR) ? "" : value;
      }
   }
}
