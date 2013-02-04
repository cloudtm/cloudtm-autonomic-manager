package eu.cloudtm.reporter;

/**
 * Memory unity converter
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public enum MemoryUnit {
   BYTE("b",1),
   KILO_BYTE("kb",1024),
   MEGA_BYTE("mb",1024*1024),
   GIGA_BYTE("gb",1024*1024*1024);
   
   final long conversion;
   final String name;

   private MemoryUnit(String name, long conversion) {
      this.name = name;
      this.conversion = conversion;
   }
   
   public double convert(double bytes) {
      if (bytes <= 0) {
         return bytes;               
      }
      return bytes / conversion;
   }
   
   static MemoryUnit fromString(String unit) {
      for (MemoryUnit mu : values()) {
         if (mu.name.equalsIgnoreCase(unit)) {
            return mu;
         }
      }
      return BYTE;
   }
}
