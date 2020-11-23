/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.util;

import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;
import com.adaptris.interlok.InterlokException;

/**
 * Helper class that assists converting exceptions.
 *
 * @author lchan
 *
 */
public abstract class ExceptionHelper {

  public static void rethrowCoreException(Throwable e) throws CoreException {
    rethrowCoreException(e.getMessage(), e);
  }

  public static CoreException wrapCoreException(Throwable e) {
    return wrapCoreException(e.getMessage(), e);
  }

  public static void rethrowServiceException(Throwable e) throws ServiceException {
    rethrowServiceException(e.getMessage(), e);
  }

  public static ServiceException wrapServiceException(Throwable e) {
    return wrapServiceException(e.getMessage(), e);
  }

  public static void rethrowProduceException(Throwable e) throws ProduceException {
    rethrowProduceException(e.getMessage(), e);
  }

  public static ProduceException wrapProduceException(Throwable e) {
    return wrapProduceException(e.getMessage(), e);
  }

  public static void rethrowCoreException(String msg, Throwable e) throws CoreException {
    if (e instanceof CoreException) {
      throw (CoreException) e;
    }
    throw new CoreException(msg, e);
  }

  public static CoreException wrapCoreException(String msg, Throwable e) {
    if (e instanceof CoreException) {
      return (CoreException) e;
    }
    return new CoreException(msg, e);
  }

  public static void rethrowServiceException(String msg, Throwable e) throws ServiceException {
    if (e instanceof ServiceException) {
      throw (ServiceException) e;
    }
    throw new ServiceException(msg, e);
  }

  public static ServiceException wrapServiceException(String msg, Throwable e) {
    if (e instanceof ServiceException) {
      return (ServiceException) e;
    }
    return new ServiceException(msg, e);
  }

  public static void rethrowProduceException(String msg, Throwable e) throws ProduceException {
    if (e instanceof ProduceException) {
      throw (ProduceException) e;
    }
    throw new ProduceException(msg, e);
  }

  public static ProduceException wrapProduceException(String msg, Throwable e) {
    if (e instanceof ProduceException) {
      return (ProduceException) e;
    }
    return new ProduceException(msg, e);
  }

  public static InterlokException wrapInterlokException(Throwable e) {
    return wrapInterlokException(e.getMessage(), e);
  }

  public static InterlokException wrapInterlokException(String msg, Throwable e) {
    if (e instanceof InterlokException) {
      return (InterlokException) e;
    }
    return new InterlokException(msg, e);
  }
}
