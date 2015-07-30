package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;

public class MockSkipProducerService extends ServiceImp {

  public void doService(AdaptrisMessage msg) throws ServiceException {
    msg.addMetadata(CoreConstants.KEY_WORKFLOW_SKIP_PRODUCER, CoreConstants.STOP_PROCESSING_VALUE);
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void close() {
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

}
