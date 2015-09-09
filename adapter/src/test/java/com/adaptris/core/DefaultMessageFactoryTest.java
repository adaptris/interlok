package com.adaptris.core;


public class DefaultMessageFactoryTest extends AdaptrisMessageFactoryImplCase {

  /**
   * @see com.adaptris.core.AdaptrisMessageFactoryImplCase#getMessageFactory()
   */
  @Override
  protected AdaptrisMessageFactory getMessageFactory() {
    return new DefaultMessageFactory();
  }

}
