package com.adaptris.core.util;

import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;

/**
 * Helper class that assists converting exceptions.
 * 
 * @author lchan
 * 
 */
public class ExceptionHelper {

  public static void rethrowCoreException(Throwable e) throws CoreException {
    rethrowCoreException(e.getMessage(), e);
  }

  public static void rethrowServiceException(Throwable e) throws ServiceException {
    rethrowServiceException(e.getMessage(), e);
  }

  public static void rethrowProduceException(Throwable e) throws ProduceException {
    rethrowProduceException(e.getMessage(), e);
  }

  public static void rethrowCoreException(String msg, Throwable e) throws CoreException {
    if (e instanceof CoreException) {
      throw (CoreException) e;
    }
    throw new CoreException(msg, e);
  }

  public static void rethrowServiceException(String msg, Throwable e) throws ServiceException {
    if (e instanceof ServiceException) {
      throw (ServiceException) e;
    }
    throw new ServiceException(msg, e);
  }


  public static void rethrowProduceException(String msg, Throwable e) throws ProduceException {
    if (e instanceof ProduceException) {
      throw (ProduceException) e;
    }
    throw new ProduceException(msg, e);
  }
}
