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
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ExceptionGenerator} implementation that generates the exception from static configuration.
 * 
 * <p>
 * Use this class with {@link ThrowExceptionService} to throw an exception as part of a workflow. The configured message forms the
 * exception message (i.e. {@link Exception#getMessage()}).
 * </p>
 * 
 * @config configured-exception
 * 
 * @author lchan
 * 
 */
@XStreamAlias("configured-exception")
public class ConfiguredException implements ExceptionGenerator {

  private String message;

  public ConfiguredException() {
  }

  public ConfiguredException(String msg) {
    this();
    setMessage(msg);
  }

  /**
   * Returns the configured exception message to use.
   *
   * @return the configured exception message to use
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the configured exception message to use.
   *
   * @param s the configured exception message to use.
   */
  public void setMessage(String s) {
    message = s;
  }

  public ServiceException create(AdaptrisMessage msg) {
    return new ServiceException(getMessage());
  }

}
