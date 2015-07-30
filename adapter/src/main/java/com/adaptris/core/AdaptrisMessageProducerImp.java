package com.adaptris.core;



/**
 * <p>
 * Implementation of behaviour common to <code>AdaptrisMessageProducer</code>s.
 * </p>
 */
public abstract class AdaptrisMessageProducerImp
  extends AdaptrisMessageWorkerImp
  implements AdaptrisMessageProducer {

  private ProduceDestination destination;
  
  public AdaptrisMessageProducerImp() {    
  }
  // gets and sets

  /** 
   * @see com.adaptris.core.AdaptrisMessageProducer
   *   #setDestination(com.adaptris.core.ProduceDestination) 
   */
  @Override
  public void setDestination(ProduceDestination dest) { // may be null...
    destination = dest;
  }

  /** @see com.adaptris.core.AdaptrisMessageProducer#getDestination() */
  @Override
  public ProduceDestination getDestination() {
    return destination;
  }
}
