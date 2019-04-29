/*
 * Copyright 2019 Adaptris Ltd.
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
package com.adaptris.core.services.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.util.NumberUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @config jdbc-truncated-parameter-logging
 *
 */
@XStreamAlias("jdbc-truncated-parameter-logging")
@ComponentProfile(summary="Log parameters, but truncate them", since="3.8.4")
public class TruncatedParameterLogger implements ParameterLogger {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());
  
  @InputFieldDefault(value = "50")
  private Integer truncateLength;
  
  public TruncatedParameterLogger() {
    
  }
  
  public TruncatedParameterLogger(Integer length) {
    this();
    setTruncateLength(length);
  }
  
  @Override
  public void log(int paramterIndex, Object o) {
    String s = o == null ? "(null)" : o.toString();
    log.trace("Setting argument {} to [{}]", paramterIndex, StringUtils.abbreviate(s, truncateLength()));
  }

  public Integer getTruncateLength() {
    return truncateLength;
  }

  public void setTruncateLength(Integer truncateLength) {
    this.truncateLength = truncateLength;
  }
  

  int truncateLength() {
    return NumberUtils.toIntDefaultIfNull(getTruncateLength(), 50);
  }
}
