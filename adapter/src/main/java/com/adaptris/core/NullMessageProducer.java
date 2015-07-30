package com.adaptris.core;

import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Null implementation of <code>AdaptrisMessageProducer</code>.
 * </p>
 * 
 * @config null-message-producer
 */
@XStreamAlias("null-message-producer")
public class NullMessageProducer extends AdaptrisMessageProducerImp {

  public NullMessageProducer() {
	  setMessageFactory(null);
  }

  public NullMessageProducer(ProduceDestination p) {
    this();
    setDestination(p);
  }

  /** @see AdaptrisMessageProducer */
  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    // do nothing...
  }

  /** @see AdaptrisMessageProducer */
  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination overload)
    throws ProduceException {

    // do nothing...
  }

  /** @see AdaptrisMessageProducer */
  @Override
  public void init() throws CoreException {
    // do nothing...
  }

  /** @see AdaptrisMessageProducer */
  @Override
  public void start() throws CoreException {
    // do nothing...
  }

  /** @see AdaptrisMessageProducer */
  @Override
  public void stop() {
    // do nothing...
  }

  /** @see AdaptrisMessageProducer */
  @Override
  public void close() {
    // do nothing...
  }

  /** @see AdaptrisMessageProducer */
  @Override
  public AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException {
    return null; // do nothing...
  }

  /** @see AdaptrisMessageProducer */
  @Override
  public AdaptrisMessage request(AdaptrisMessage msg, long timeout)
    throws ProduceException {

    return null; // do nothing...
  }

  /** @see AdaptrisMessageProducer */
  @Override
  public AdaptrisMessage request(
    AdaptrisMessage msg,
    ProduceDestination destination)
    throws ProduceException {

    return null; // do nothing...
  }

  /** @see AdaptrisMessageProducer */
  @Override
  public AdaptrisMessage request(
    AdaptrisMessage msg,
    ProduceDestination destination,
    long timeout)
    throws ProduceException {

    return null; // do nothing...
  }

  /** @see MessageEventGenerator */
  @Override
  public String createName() {
    return this.getClass().getName();
  }
  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

}
