/*
 * $RCSfile: DefaultMessageTest.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/03/20 10:44:34 $
 * $Author: lchan $
 */
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