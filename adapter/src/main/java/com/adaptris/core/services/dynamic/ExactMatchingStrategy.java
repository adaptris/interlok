package com.adaptris.core.services.dynamic;

import com.adaptris.core.CoreException;
import com.adaptris.core.TradingRelationship;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * <code>ExactMatchingStrategy</code> returns a <code>TradingRelationship[]</code> containing the passed
 * <code>TradingRelationship</code> only, or an empty array if the parameter is null.
 * </p>
 * 
 * @config exact-matching-strategy
 */
@XStreamAlias("exact-matching-strategy")
public class ExactMatchingStrategy implements MatchingStrategy {

  /**
   * <p>
   * Returns a <code>TradingRelationship[]</code> containing the passed
   * <code>TradingRelationship</code> only, or an empty array if the parameter
   * is null.
   * </p>
   * @see com.adaptris.core.services.dynamic.MatchingStrategy
   *   #create(com.adaptris.core.TradingRelationship)
   */
  @Override
  public TradingRelationship[] create(TradingRelationship t)
    throws CoreException {
    
    TradingRelationship[] result = null;
    
    if (t == null) {
      result = new TradingRelationship[0];
    }
    else {
      result = new TradingRelationship[1];
      result[0] = t;
    }
    
    return result;
  }
}
