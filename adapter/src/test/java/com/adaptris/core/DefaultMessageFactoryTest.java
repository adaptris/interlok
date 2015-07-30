/*
 * $RCSfile: DefaultMessageFactoryTest.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/03/20 10:44:34 $
 * $Author: lchan $
 */
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
