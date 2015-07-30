package com.adaptris.core;


/**
 * <p>
 * Implementations of this interface return a <code>String</code> destination
 * (e.g. queue name, URL) to be used by <code>AdaptrisMessageProducer</code>.
 * </p>
 */
public interface ProduceDestination {

  /**
   * <p>
   * Returns a <code>String</code> destination name.  Implementations
   * may or may not use the <code>AdaptrisMessage</code> to dynamically
   * generate the name. 
   * </p>
   * @param msg the <code>AdaptrisMessage</code> for which the name
   * is being generated
   * @return the destination name to use
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  String getDestination(AdaptrisMessage msg) throws CoreException;
}
