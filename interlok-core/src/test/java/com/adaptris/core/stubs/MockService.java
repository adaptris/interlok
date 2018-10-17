package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;

public class MockService extends ServiceImp {
  
  public int callCount = 0;

  public MockService() {
  }

  public MockService(String uuid) {
    this();
    setUniqueId(uuid);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    callCount++;
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
    callCount = 0;
  }

  @Override
  protected void closeService() {
    callCount = 0;
  }

}
