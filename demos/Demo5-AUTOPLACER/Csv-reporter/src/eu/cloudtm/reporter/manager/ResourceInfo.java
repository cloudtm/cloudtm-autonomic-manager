package eu.cloudtm.reporter.manager;

/**
 * Contains the information about a the Infinispan instance, i.e, the listening address and the port
 *
 * @author Pedro ruivo
 * @since 1.1
 */
public class ResourceInfo {
   
   private final String address;
   private final int port;

   public ResourceInfo(String address, int port) {
      this.address = address;
      this.port = port;
   }

   public String getAddress() {
      return address;
   }

   public int getPort() {
      return port;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ResourceInfo that = (ResourceInfo) o;

      return port == that.port && !(address != null ? !address.equals(that.address) : that.address != null);

   }

   @Override
   public int hashCode() {
      int result = address != null ? address.hashCode() : 0;
      result = 31 * result + port;
      return result;
   }

   @Override
   public String toString() {
      return "ResourceInfo{" +
            "address='" + address + '\'' +
            ", port=" + port +
            '}';
   }
}
