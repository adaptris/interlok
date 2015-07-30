package com.adaptris.core.services.dynamic;

import com.adaptris.core.CoreException;
import com.adaptris.core.Service;

/**
 * <p> 
 * Implementations provide a store of <code>Service</code>s which may be 
 * retrieved by name.  
 * </p>
 */
public interface ServiceStore {
  
  /**
   * <p>
   * Perform any validation that may be requireed on the store.
   * </p>
   * @throws CoreException if the store is invalid
   */
  void validate() throws CoreException;
  
  /**
   * <p>
   * Returns the <code>Service</code> stored against the passed logical 
   * <code>name</code> if one exists in the store, otherwise null.
   * </p>
   * @param name the name of the <code>Service</code> to obtain
   * @return the named <code>Service</code> or null if it does not exist
   * @throws CoreException wrapping any underlying Exception
   */
  Service obtain(String name) throws CoreException;
}
