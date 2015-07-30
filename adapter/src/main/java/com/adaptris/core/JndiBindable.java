package com.adaptris.core;

/**
 * Interface for objects that can be found to the internal JNDI context.
 * 
 * @author amcgrath
 * 
 */
public interface JndiBindable {

  /**
   * Returns the exact name to bind this object to our {@link javax.naming.Context}.
   * 
   * <p>
   * Specifying a lookupName will not be modified at all when binding to jndi. Therefore you may want to prepend your own chosen
   * subcontexts in this name e.g. "comp/env/"
   * </p>
   * 
   * @return the lookup name.
   */
  String getLookupName();
  
}
