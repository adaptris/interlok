package com.adaptris.core.services.dynamic;

import com.adaptris.core.CoreException;
import com.adaptris.core.TradingRelationship;

/**
 * <p>
 * Implementations of <code>MatchingStrategy</code> provide alternative 
 * <code>TradingRelationship</code>'s for <code>ServiceNameProvider</code>'s 
 * to look for if there is no exact match.
 * </p>
 */
public interface MatchingStrategy {

  /**
   * <p>
   * Returns a <code>TradingRelationship[]</code> containing a list
   * of <code>TradingRelationship</code>s to attempt to match in a 
   * <code>ServiceNameProvider</code>.  <code>ServiceNameProvider</code>s
   * will attempt each element in turn.  Elements should be ordered from
   * more to less specific. 
   * </p>
   * @param t the <code>TradingRelationship</code> to create an array of
   * matches for
   * @return an array of matches
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  TradingRelationship[] create(TradingRelationship t) throws CoreException;
}
