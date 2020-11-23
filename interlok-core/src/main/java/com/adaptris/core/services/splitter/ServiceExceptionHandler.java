/*
 * Copyright 2018 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.adaptris.core.services.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.extern.slf4j.Slf4j;

/**
 * Default {@link ServiceErrorHandler} that captures exceptions thrown and rethrows the first
 * exception when requested.
 *
 * @config default-service-exception-handler
 *
 */
@XStreamAlias("default-service-exception-handler")
@Slf4j
@ComponentProfile(
    summary = "Default behaviour for handling exceptions during pooled-split-join-service execution",
    since = "3.11.1",
    tag = "service,splitjoin")
public class ServiceExceptionHandler implements ServiceErrorHandler {

  private List<Throwable> exceptionList = Collections.synchronizedList(new ArrayList<Throwable>());

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    log.error("uncaughtException from {}", t.getName(), e);
    exceptionList.add(e);
  }

  public Throwable getFirstThrowableException() {
    Throwable result = null;
    if (exceptionList.size() > 0) {
      result = exceptionList.get(0);
    }
    return result;
  }

  public void clearExceptions() {
    exceptionList.clear();
  }

  /**
   * Note that this also clears any exceptions.
   *
   * @throws ServiceException if there was an exception.
   * @see #getFirstThrowableException()
   * @deprecated use {@link #throwExceptionAsRequired()} instead.
   */
  @Deprecated
  public void throwFirstException() throws ServiceException {
    throwExceptionAsRequired();
  }

  @Override
  public void throwExceptionAsRequired() throws ServiceException {
    Throwable e = getFirstThrowableException();
    if (e != null) {
      log.error("One or more services failed: {}", e.getMessage());
      ServiceException wrappedException = ExceptionHelper.wrapServiceException(e);
      for (Throwable t : exceptionList) {
        if (t != e) {
          wrappedException.addSuppressed(t);
        }
      }
      clearExceptions();
      throw wrappedException;
    }
  }
}
