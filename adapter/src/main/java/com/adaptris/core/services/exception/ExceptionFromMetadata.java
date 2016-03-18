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

import static org.apache.commons.lang.StringUtils.isBlank;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ExceptionGenerator} implementation that generates the exception from metadata.
 * 
 * <p>
 * Use this class with {@link ThrowExceptionService} to throw an exception as part of a workflow. The exception message (i.e.
 * {@link Exception#getMessage()}) is derived from the configured metadata key. If the metadata key does not exist (or returns no
 * value) then the default exception message is used to generate the exception. Note that this <strong>always</strong> throws an
 * exception.
 * 
 * @config exception-from-metadata
 * 
 * @author lchan
 * 
 */
@XStreamAlias("exception-from-metadata")
@DisplayOrder(order = {"exceptionMessageMetadataKey", "defaultExceptionMessage"})
public class ExceptionFromMetadata extends ExceptionFromMetadataImpl {

  private String defaultExceptionMessage;

  public ExceptionFromMetadata() {
  }

  public ExceptionFromMetadata(String defaultMessage) {
    this();
    setDefaultExceptionMessage(defaultMessage);
  }

  public ExceptionFromMetadata(String defaultMessage, String metadataKey) {
    this();
    setDefaultExceptionMessage(defaultMessage);
    setExceptionMessageMetadataKey(metadataKey);
  }

  /**
   * Returns the configured exception message to use.
   *
   * @return the configured exception message to use
   */
  public String getDefaultExceptionMessage() {
    return defaultExceptionMessage;
  }

  /**
   * Sets the configured exception message to use.
   *
   * @param s the configured exception message to use.
   */
  public void setDefaultExceptionMessage(String s) {
    defaultExceptionMessage = s;
  }

  public ServiceException create(AdaptrisMessage msg) {
    String message = getDefaultExceptionMessage();
    if (getExceptionMessageMetadataKey() != null && msg.containsKey(getExceptionMessageMetadataKey())) {
      message = isBlank(msg.getMetadataValue(getExceptionMessageMetadataKey())) ? message : msg
          .getMetadataValue(getExceptionMessageMetadataKey());
    }
    return new ServiceException(message);
  }
}
