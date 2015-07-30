package com.adaptris.core;


/**
 * <p>
 * Implementations of <code>AdaptrisMessageProducer</code> produce messages to a destination (such as a JMS Queue or file system
 * directory).
 * </p>
 *
 * @see AdaptrisMessageSender
 */
public interface AdaptrisMessageProducer extends AdaptrisMessageWorker, AdaptrisMessageSender {

  /**
   * Produce the {@link AdaptrisMessage} to the configured destination and blocks indefinitely for a reply.
   *
   * @param msg the {@link AdaptrisMessage} to produce
   * @return a reply {@link AdaptrisMessage} which is generally the same as supplied parameter
   * @throws ProduceException wrapping any underlying Exceptions
   */
  AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException;

  /**
   * Produces the {@link AdaptrisMessage} to the passed supplied {@link ProduceDestination} and blocks indefinitely for a reply.
   *
   * @param msg the {@link AdaptrisMessage} to produce
   * @param destination the {@link ProduceDestination} to produce to
   * @return a reply {@link AdaptrisMessage} which is generally the same as supplied parameter
   * @throws ProduceException wrapping any underlying Exceptions
   */
  AdaptrisMessage request(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException;

  /**
   * Produces the {@link AdaptrisMessage} to the passed supplied {@link ProduceDestination} and blocks for the specified timeout for
   * a reply.
   *
   *
   * @param msg the {@link AdaptrisMessage} to produce
   * @param destination the {@link ProduceDestination} to produce to
   * @param timeoutMs the time to wait for a reply in milliseconds
   * @return a reply {@link AdaptrisMessage} which is generally the same as supplied parameter
   * @throws ProduceException wrapping any underlying Exceptions
   */
  AdaptrisMessage request(AdaptrisMessage msg, ProduceDestination destination, long timeoutMs) throws ProduceException;

  /**
   * Produces the {@link AdaptrisMessage} to the configured {@link ProduceDestination} and blocks for the specified timeout for a
   * reply.
   * 
   * @param msg the {@link AdaptrisMessage} to produce
   * @param timeout the time to wait for a reply in milliseconds
   * @return a reply {@link AdaptrisMessage} which is generally the same as supplied parameter
   * @throws ProduceException wrapping any underlying Exceptions
   */
  AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException;

  /**
   * Returns the default {@link ProduceDestination} for this instance.
   *
   * @return the {@link ProduceDestination} used in the absence of any other information.
   */
  ProduceDestination getDestination();

  /**
   * Set the default {@link ProduceDestination}.
   *
   * @param destination the {@link ProduceDestination} to use
   */
  void setDestination(ProduceDestination destination);
}
