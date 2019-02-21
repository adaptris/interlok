package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.core.util.ExceptionHelper;

public class MockService extends ServiceImp {

  public transient int callCount = 0;

  private FailureCondition failureCondition;

  public static enum FailureCondition {
    // Arguably All is a redundant condition, as you'd never manage to init the service.
    Prepare, Lifecycle, Service, All, Never
  };

  private static enum CurrentOperation {
    Prepare {

      @Override
      public boolean isBroken(FailureCondition c) {
        return (c == FailureCondition.Prepare) || (c == FailureCondition.All);
      }

    },    
    Lifecycle {

      @Override
      public boolean isBroken(FailureCondition c) {
        return (c == FailureCondition.Lifecycle) || (c == FailureCondition.All);
      }

    },
    Service {
      @Override
      public boolean isBroken(FailureCondition c) {
        return (c == FailureCondition.Service) || (c == FailureCondition.All);
      }

    };

    public abstract boolean isBroken(FailureCondition c);
  }

  public MockService() {
    setFailureCondition(FailureCondition.Never);
  }

  public MockService(String uuid) {
    this();
    setUniqueId(uuid);
  }

  public MockService(FailureCondition b) {
    this();
    setFailureCondition(b);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    checkFail(CurrentOperation.Service);
    callCount++;
  }

  @Override
  public void prepare() throws CoreException {
    checkFail(CurrentOperation.Prepare);
  }

  @Override
  protected void initService() throws CoreException {
    checkFail(CurrentOperation.Lifecycle);
    callCount = 0;
  }

  @Override
  public void start() throws CoreException {
    checkFail(CurrentOperation.Lifecycle);
  }
  
  @Override
  protected void closeService() {
    callCount = 0;
  }

  private void checkFail(CurrentOperation op) throws ServiceException {
    if (op.isBroken(getFailureCondition())) {
      throw new ServiceException();
    }
  }

  public FailureCondition getFailureCondition() {
    return failureCondition;
  }

  public void setFailureCondition(FailureCondition c) {
    this.failureCondition = c;
  }
}
