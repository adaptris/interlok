package com.adaptris.core;


/**
 * New interface so that we can re-use producer style functionality without all the additional requirements surrounds
 * AdaptrisMessageWorker.
 *
 * @author lchan
 *
 */
public interface AdaptrisMessageSender extends ComponentLifecycle {
  /**
   * Produces the {@link AdaptrisMessage} to the default destination.
   * 
   * @param msg the @link AdaptrisMessage} to produce
   * @throws ProduceException wrapping any underlying Exceptions
   */
  void produce(AdaptrisMessage msg) throws ProduceException;

  /**
   * Produce the {@link AdaptrisMessage} to the supplied {@link ProduceDestination} , over-riding any configured destinations.
   *
   * @param msg the {@link AdaptrisMessage} to produce
   * @param destination the {@link ProduceDestination} to produce to
   * @throws ProduceException wrapping any underlying Exceptions
   */
  void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException;

}
