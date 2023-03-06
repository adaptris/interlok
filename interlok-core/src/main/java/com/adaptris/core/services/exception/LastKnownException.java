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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.ServiceException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ExceptionGenerator} implementation that generates the exception from the last known exception.
 * 
 * <p>
 * Use this class with {@link ThrowExceptionService} to throw an exception as part of a workflow. Whenever an exception is
 * encountered in a service, exception is stored as part of object metadata. In some cases, the exception may have been
 * <i>ignored</i> due to continue-on-fail settings. This implementation uses any Exception stored in object metadata as the base of
 * the exception that is generated. Note that if this {@link ExceptionGenerator} is used, then it is possible for
 * {@link ThrowExceptionService} to not throw an exception as there may have not been an exception generated previously.
 * </p>
 * 
 * @config last-known-exception
 * @author lchan
 * 
 */
@JacksonXmlRootElement(localName = "last-known-exception")
@XStreamAlias("last-known-exception")
public class LastKnownException implements ExceptionGenerator {

  public ServiceException create(AdaptrisMessage msg) {
    ServiceException result = null;
    if (msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION)) {
      Exception exc = (Exception) msg.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION);
      if (exc != null) {
        if (exc instanceof ServiceException) {
          result = (ServiceException) exc;
        }
        else {
          result = new ServiceException(exc);
        }
      }
    }
    return result;
  }

}
