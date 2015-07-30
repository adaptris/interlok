package com.adaptris.core.services.dynamic;

import com.adaptris.core.CoreException;
import com.adaptris.core.TradingRelationship;

/**
 * <p> 
 * Returns a logical name for a passed <code>TradingRelationship</code> or
 * <code>TradingRelationship[]</code>.
 * </p>
 */
public interface ServiceNameProvider {

  /**
   * <p>
   * Returns the logical name of the <code>Service</code> to use for a passed
   * <code>TradingRelationship</code>.
   * </p>
   * @param t the <code>TradingRelationship</code>, may not be null
   * @return the logical name of the <code>Service</code> to obtain or null if
   * no name exists
   * @throws CoreException wrapping any underlying Exceptions that occur
   */
  String obtain(TradingRelationship t) throws CoreException;
  
  /**
   * <p>
   * Returns the logical name of the <code>Service</code> to use for the passed
   * <code>TradingRelationship[]</code>.  <code>TradingRelationship</code>s
   * in the array are attempted in turn and are thus expected to become
   * increasingly generic.
   * </p>
   * @param t the <code>TradingRelationship</code>, may not be null
   * @return the logical name of the <code>Service</code> to obtain or null if
   * no name exists
   * @throws CoreException wrapping any underlying Exceptions that occur
   */
  String obtain(TradingRelationship[] t) throws CoreException;
}
