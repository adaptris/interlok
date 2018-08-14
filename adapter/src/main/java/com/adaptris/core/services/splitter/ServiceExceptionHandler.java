/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.services.splitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;

public class ServiceExceptionHandler implements Thread.UncaughtExceptionHandler {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());
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
   */
  public void throwFirstException() throws ServiceException {
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
