package com.adaptris.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Partial implementation of behaviour commom to all {@link AdaptrisMessageEncoder} instances.
 * </p>
 */
public abstract class AdaptrisMessageEncoderImp implements AdaptrisMessageEncoder {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private transient AdaptrisMessageFactory messageFactoryToUse;

  public AdaptrisMessageEncoderImp() {
    registerMessageFactory(new DefaultMessageFactory());
  }

  /**
   * 
   * @see AdaptrisMessageTranslator#registerMessageFactory(AdaptrisMessageFactory)
   */
  @Override
  public void registerMessageFactory(AdaptrisMessageFactory f) {
    messageFactoryToUse = f;
  }

  /**
   * 
   * @see AdaptrisMessageTranslator#currentMessageFactory()
   */
  @Override
  public AdaptrisMessageFactory currentMessageFactory() {
    return messageFactoryToUse;
  }
}
