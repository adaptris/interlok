package com.adaptris.core.lms;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.AdaptrisMessageFactoryImplCase;
import com.adaptris.core.lms.ZipFileBackedMessageFactory.CompressionMode;

public class ZipFileBackedMessageFactoryTest extends
    AdaptrisMessageFactoryImplCase {

  /**
   * @see com.adaptris.core.AdaptrisMessageFactoryImplCase#getMessageFactory()
   */
  @Override
  protected AdaptrisMessageFactory getMessageFactory() {
    ZipFileBackedMessageFactory mf = new ZipFileBackedMessageFactory();
    mf.setCompressionMode(CompressionMode.Both);
    return mf;
  }

}
