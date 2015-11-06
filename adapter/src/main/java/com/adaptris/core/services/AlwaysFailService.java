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

package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Always fail.
 * 
 * @config always-fail-service
 * 
 * @license BASIC
 * @author lchan
 * @deprecated consider using {@link ThrowExceptionService} instead (since 2.6.2) which wil give you a better exception message.
 */
@Deprecated
@XStreamAlias("always-fail-service")
public class AlwaysFailService extends ServiceImp {

  private static transient boolean warningLogged;

  public AlwaysFailService() {
    super();
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(), ThrowExceptionService.class.getName());
      warningLogged = true;
    }
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    throw new ServiceException(this.getClass().getName());
  }

  @Override
  public void close() {
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void prepare() throws CoreException {
  }
}
