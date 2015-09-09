package com.adaptris.core;

public class DefaultMessageTest extends AdaptrisMessageCase {

  private DefaultMessageFactory msgFactory = new DefaultMessageFactory();

  /**
   * @see com.adaptris.core.AdaptrisMessageCase#getMessageFactory()
   */
  @Override
  protected AdaptrisMessageFactory getMessageFactory() {
    return msgFactory;
  }

}