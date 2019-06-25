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

package com.adaptris.core.services.exception;

import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service that takes an exception in object metadata and serializes that into the AdaptrisMessage payload.
 * 
 * <p>
 * In some scenarios (e.g. handling request reply scenarios), rather than directly handling the message exception in the adapter, it
 * may be required to report back to the back-end application that an exception has occured along with the problem document.
 * </p>
 * 
 * @config exception-report-service
 * 
 * @see ExceptionSerializer
 * @author lchan
 * @see com.adaptris.core.CoreConstants#OBJ_METADATA_EXCEPTION
 */
@XStreamAlias("exception-report-service")
@AdapterComponent
@ComponentProfile(summary = "Generate a report based on the current exception", tag = "service,error-handling")
@DisplayOrder(order = {"exceptionSerializer"})
public class ExceptionReportService extends ServiceImp {

  @Valid
  @NotNull
  private ExceptionSerializer exceptionSerializer;

  public ExceptionReportService() {
  }

  public ExceptionReportService(ExceptionSerializer e) {
    this();
    setExceptionSerializer(e);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      if (msg.getObjectHeaders().containsKey(OBJ_METADATA_EXCEPTION)) {
        Exception e = (Exception) msg.getObjectHeaders().get(OBJ_METADATA_EXCEPTION);
        getExceptionSerializer().serialize(e, msg);
      }
      else {
        log.debug("No Exception in object metadata, nothing to do.");
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {
    try {
      Args.notNull(getExceptionSerializer(), "exceptionSerializer");
    }
    catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  public ExceptionSerializer getExceptionSerializer() {
    return exceptionSerializer;
  }

  public void setExceptionSerializer(ExceptionSerializer exceptionSerializer) {
    this.exceptionSerializer = Args.notNull(exceptionSerializer, "exceptionSerializer");
  }

}
