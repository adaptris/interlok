package com.adaptris.core;


/**
 * <p>
 * Applies arbitrary processing to <code>AdaptrisMessage</code>s.  Applicable
 * where the calling code does not want to be informed
 * of any <code>Exception</code>s that may occur.  <code>Service</code>
 * is applicable where the container requires to be informed of 
 * <code>Exception</code>s.
 * </p>
 * @see Service
 */
public interface AdaptrisMessageListener {

  /**
   * <p>
   * It is the responsibility of implementations of this interface to ensure 
   * that all <code>Exception</code>s, including <code>RuntimeException</code>s,
   * are handled.  Failure to handle any <code>Exception</code> will result in 
   * undefined behaviour.  Throwing a <code>RuntimeException</code> to this 
   * method is considered a bug.  
   * </p><p>
   * Although most clients of implementations of this interface are likely to 
   * be single-threaded, if implementations are not guaranteed to be thread 
   * safe, they should be <code>synchronized</code> or use some other locking
   * mechanism. 
   * </p>
   * @param msg the <code>AdaptrisMessage</code> to process
   */
  void onAdaptrisMessage(AdaptrisMessage msg);
}
