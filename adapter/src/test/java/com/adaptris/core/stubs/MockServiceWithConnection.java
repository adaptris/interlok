package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;

/**
 * Service that does nothing, but does have a connection.
 * 
 * @author lchan
 * 
 */
public class MockServiceWithConnection extends ServiceImp {

  private AdaptrisConnection connection;

  public MockServiceWithConnection() {

  }

  public MockServiceWithConnection(AdaptrisConnection c) {
    this();
    setConnection(c);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  @Override
  public void init() throws CoreException {
    getConnection().addExceptionListener(this);
    LifecycleHelper.init(getConnection());
  }

  @Override
  public void start() throws CoreException {
    getConnection().addExceptionListener(this);
    LifecycleHelper.init(getConnection());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getConnection());
  }

  @Override
  public void close() {
    LifecycleHelper.close(getConnection());

  }

  public AdaptrisConnection getConnection() {
    return connection;
  }

  public void setConnection(AdaptrisConnection connection) {
    this.connection = connection;
  }

}
