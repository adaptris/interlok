package com.adaptris.core;


/**
 * <p>
 * Creates a file name for an <code>AdaptrisMessage</code>.  Implementations
 * may create this name dynamically based on message contents or metadata, or 
 * may allow the name to be configured, etc. 
 * </p>
 */
public interface FileNameCreator {

  /**
   * <p>
   * Returns a file name for the passed <code>AdaptrisMessage</code>.
   * </p>
   * @param msg the <code>AdaptrisMessage</code> to create a file name for
   * @return a file name for the message
   * @throws CoreException wrapping any <code>Exception</code> that may occur
   */
  String createName(AdaptrisMessage msg) throws CoreException;
}
