package com.adaptris.core;



/**
 * <p> 
 * Create a <code>TradingRelationship</code> from an 
 * <code>AdaptrisMessage</code>.
 * </p>
 */
public interface TradingRelationshipCreator {

  /**
   * <p>
   * Create a <code>TradingRelationship</code> from an 
   * <code>AdaptrisMessage</code>.
   * </p>
   * @param msg the <code>AdaptrisMessage</code> to process
   * @return a <code>TradingRelationship</code>
   * @throws CoreException wrapping any <code>Exceptions</code> which occur
   */
  TradingRelationship create(AdaptrisMessage msg) throws CoreException;
}
