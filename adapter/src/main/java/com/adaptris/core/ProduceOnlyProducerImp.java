package com.adaptris.core;


/**
 * Convenience class that only supports produce rather than request.
 * 
 * <p>
 * All the request methods throw an UnsupportedOperationException
 * </p>
 * 
 * @author lchan
 * 
 */
public abstract class ProduceOnlyProducerImp extends AdaptrisMessageProducerImp {

  /**
   * @see AdaptrisMessageProducerImp #produce(AdaptrisMessage, ProduceDestination)
   */
  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    produce(msg, getDestination());
  }

  /**
   * UnsupportedOperationException is thrown
   * 
   * @see AdaptrisMessageProducerImp#request(AdaptrisMessage)
   */
  @Override
  public final AdaptrisMessage request(AdaptrisMessage msg)
      throws ProduceException {
    throw new UnsupportedOperationException("Request Reply is not supported");
  }

  /**
   * UnsupportedOperationException is thrown
   * 
   * @see AdaptrisMessageProducerImp#request(AdaptrisMessage, long)
   */
  @Override
  public final AdaptrisMessage request(AdaptrisMessage msg, long timeout)
      throws ProduceException {
    throw new UnsupportedOperationException("Request Reply is not supported");
  }

  /**
   * UnsupportedOperationException is thrown
   * 
   * @see AdaptrisMessageProducerImp
   *      #request(AdaptrisMessage,ProduceDestination)
   */
  @Override
  public final AdaptrisMessage request(AdaptrisMessage msg,
                                       ProduceDestination destination)
      throws ProduceException {
    throw new UnsupportedOperationException("Request Reply is not supported");
  }

  /**
   * UnsupportedOperationException is thrown
   * 
   * @see AdaptrisMessageProducerImp #request(AdaptrisMessage,
   *      ProduceDestination, long)
   */
  @Override
  public final AdaptrisMessage request(AdaptrisMessage msg,
                                       ProduceDestination destination,
                                       long timeout) throws ProduceException {
    throw new UnsupportedOperationException("Request Reply is not supported");
  }

}
