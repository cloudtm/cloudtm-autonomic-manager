package eu.cloudtm.reporter.customattributes;

import eu.cloudtm.reporter.manager.Resource;

/**
 * Interface to implement when custom attributes are necessary to be reported. These custom attributes are obtained
 * based on multiple Infinispan attributes
 *
 * @author Pedro Ruivo
 * @since 1.1
 */
public interface CustomAttribute {

   String[] getHeaders();
   
   String[] getValues();
   
   String[] getAttributes();
   
   void update(Resource resource);
}
