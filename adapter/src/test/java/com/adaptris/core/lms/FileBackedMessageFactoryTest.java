/*
 * $RCSfile: FileBackedMessageFactoryTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/06/05 16:44:32 $
 * $Author: lchan $
 */
package com.adaptris.core.lms;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.AdaptrisMessageFactoryImplCase;

public class FileBackedMessageFactoryTest extends
    AdaptrisMessageFactoryImplCase {

  /**
   * @see com.adaptris.core.AdaptrisMessageFactoryImplCase#getMessageFactory()
   */
  @Override
  protected AdaptrisMessageFactory getMessageFactory() {
    return new FileBackedMessageFactory();
  }

}
