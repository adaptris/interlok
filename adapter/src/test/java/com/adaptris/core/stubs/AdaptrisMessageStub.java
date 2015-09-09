package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultAdaptrisMessageImp;
import com.adaptris.util.IdGenerator;

/**
 * Stub of AdaptrisMessage for testing custom message factory.
 *
 * @author hfraser
 * @author $Author: lchan $
 */
public class AdaptrisMessageStub extends DefaultAdaptrisMessageImp {

  protected AdaptrisMessageStub(IdGenerator guid, AdaptrisMessageFactory f) throws RuntimeException {
    super(guid, f);
    setPayload(new byte[0]);
  }
}
