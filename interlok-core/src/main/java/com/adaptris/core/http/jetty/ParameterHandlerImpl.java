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

package com.adaptris.core.http.jetty;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.http.server.ParameterHandler;

/**
 * Abstract {@link ParameterHandler} implementation that provides a prefix.
 * 
 * @author lchan
 *
 */
public abstract class ParameterHandlerImpl implements ParameterHandler<HttpServletRequest> {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  private String parameterPrefix;

  public ParameterHandlerImpl() {

  }

  public String getParameterPrefix() {
    return parameterPrefix;
  }

  public void setParameterPrefix(String headerPrefix) {
    this.parameterPrefix = headerPrefix;
  }

  /**
   * Return the parameter prefix with null protection.
   * 
   * @return the prefix
   */
  protected String parameterPrefix() {
    return defaultIfEmpty(getParameterPrefix(), "");
  }
}
