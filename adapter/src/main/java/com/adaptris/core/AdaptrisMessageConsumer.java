package com.adaptris.core;


/**
 * <p>
 * Implementations of <code>AdaptrisMessageConsumer</code>
 * obtain data, convert it into an <code>AdaptrisMessage</code> and pass
 * the message to the registered <code>AdaptrisMessageListener</code>.
 * </p>
 */
public interface AdaptrisMessageConsumer extends AdaptrisMessageWorker {

  /**
   * <p>
   * Sets the <code>AdaptrisMessageListener</code> to use.
   * </p>
   * @param listener the <code>AdaptrisMessageListener</code> to use
   */
  void registerAdaptrisMessageListener(AdaptrisMessageListener listener);

  /**
   * <p>
   * Returns the <code>ConsumeDestination</code> to use.
   * </p>
   * @return this <code>Object</code>'s <code>ConsumeDestination</code>
   */
  ConsumeDestination getDestination();

  /**
   * <p>
   * Sets the <code>ConsumeDestination</code> to use.
   * </p>
   * @param destination the name of the destination
   */
  void setDestination(ConsumeDestination destination);
}
