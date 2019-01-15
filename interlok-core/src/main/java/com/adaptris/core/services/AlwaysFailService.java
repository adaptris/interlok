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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Always fail.
 * 
 * @config always-fail-service
 * 
 * 
 * @author lchan
 * @deprecated since 3.0.0 consider using {@link ThrowExceptionService} instead which wils give you a better exception message.
 */
@Deprecated
@XStreamAlias("always-fail-service")
@AdapterComponent
@ComponentProfile(summary = "Deprecated: use ThrowExceptionService instead", tag = "service")
public class AlwaysFailService extends ServiceImp {

  private static transient boolean warningLogged;

  public AlwaysFailService() {
    super();
    LoggingHelper.logDeprecation(warningLogged, ()-> { warningLogged=true;}, this.getClass().getSimpleName(), ThrowExceptionService.class.getName());      
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    throw new ServiceException(this.getClass().getName());
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }
  @Override
  public void prepare() throws CoreException {
  }
}
