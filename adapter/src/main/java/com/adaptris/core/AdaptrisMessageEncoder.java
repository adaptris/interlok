package com.adaptris.core;


/**
 * <p>
 * Implementations of <code>AdaptrisMessageEncoder</code> create representations
 * of whole <code>AdaptrisMessage</code>s, including metadata and unique ID.
 * They also allow <code>Objects</code>s to be decoded back to
 * <code>AdaptrisMessage</code>s. Implementations can be configured such that
 * decoded messages may retain the original unique ID or get a new one.
 * </p>
 */
public interface AdaptrisMessageEncoder extends AdaptrisMessageTranslator {

  /**
   * Encode the <code>AdaptrisMessage</code> object and write it to the supplied
   * target
   * 
   * @param msg the <code>AdaptrisMessage</code> to be encoded
   * @param target the destination to write to.
   * @throws CoreException wrapping any underlying Exceptions that may occur
   */
  void writeMessage(AdaptrisMessage msg, Object target) throws CoreException;

  /**
   * Decode the supplied Object into an <code>AdaptrisMessage</code> object.
   * 
   * @param source the object to be decoded.
   * @return an <code>AdaptrisMessage</code> created from the object.
   * @throws CoreException wrapping any underlying Exceptions that may occur
   */
  AdaptrisMessage readMessage(Object source) throws CoreException;

  
}
