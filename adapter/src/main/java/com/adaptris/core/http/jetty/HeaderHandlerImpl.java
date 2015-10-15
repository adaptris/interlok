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

import com.adaptris.core.http.server.HeaderHandler;

/**
 * Abstract {@link HeaderHandler} implementation that provides a prefix.
 * 
 * @author lchan
 *
 */
public abstract class HeaderHandlerImpl implements HeaderHandler<HttpServletRequest> {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  private String headerPrefix;

  public HeaderHandlerImpl() {

  }

  public String getHeaderPrefix() {
    return headerPrefix;
  }

  public void setHeaderPrefix(String headerPrefix) {
    this.headerPrefix = headerPrefix;
  }

  /**
   * Return the header prefix with null protection.
   * 
   * @return the prefix
   */
  protected String headerPrefix() {
    return defaultIfEmpty(getHeaderPrefix(), "");
  }
}
