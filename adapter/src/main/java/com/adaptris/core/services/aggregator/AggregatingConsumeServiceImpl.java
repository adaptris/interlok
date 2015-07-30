package com.adaptris.core.services.aggregator;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.LifecycleHelper;


/**
 * Base class for {@link AggregatingConsumeService} implementations providing common functionality.
 * 
 * @author lchan
 * 
 */
public abstract class AggregatingConsumeServiceImpl<E extends AdaptrisConnection> extends ServiceImp implements
    AggregatingConsumeService<E> {

  public AggregatingConsumeServiceImpl() {
  }

  protected void start(AdaptrisComponent ac) throws ServiceException {
    try {
      LifecycleHelper.init(ac);
      LifecycleHelper.start(ac);
    }
    catch (CoreException e) {
      throw new ServiceException(e);
    }
  }

  protected void stop(AdaptrisComponent ac) {
    LifecycleHelper.stop(ac);
    LifecycleHelper.close(ac);
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
  }

  @Override
  public void close() {
  }
}
