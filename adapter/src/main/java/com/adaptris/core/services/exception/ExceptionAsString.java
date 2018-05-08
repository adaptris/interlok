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
package com.adaptris.core.services.exception;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataOutputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Serializes the exception as a String.
 * <p>
 * Note that this uses {@link ExceptionUtils#getFullStackTrace(Throwable)} which means that the root cause is first
 * </p>
 * 
 * @config exception-as-string
 */
@XStreamAlias("exception-as-string")
public class ExceptionAsString implements ExceptionSerializer {

  @NotNull
  @AutoPopulated
  @Valid
  private DataOutputParameter<String> target;

  @InputFieldDefault(value = "true")
  private Boolean includeStackTrace;

  public ExceptionAsString() {
    setTarget(new StringPayloadDataOutputParameter());
  }

  @Override
  public void serialize(Exception exc, AdaptrisMessage msg) throws CoreException {
    try {
      if (includeStackTrace()) {
        target.insert(ExceptionUtils.getFullStackTrace(exc), msg);
      } else {
        target.insert(ExceptionUtils.getRootCauseMessage(exc), msg);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  public DataOutputParameter<String> getTarget() {
    return target;
  }

  public void setTarget(DataOutputParameter<String> target) {
    this.target = Args.notNull(target, "target");
  }

  public ExceptionAsString withTarget(DataOutputParameter<String> target) {
    setTarget(target);
    return this;
  }

  public Boolean getIncludeStackTrace() {
    return includeStackTrace;
  }

  public void setIncludeStackTrace(Boolean includeStackTrace) {
    this.includeStackTrace = includeStackTrace;
  }

  public ExceptionAsString withIncludeStackTrace(Boolean b) {
    setIncludeStackTrace(b);
    return this;
  }

  boolean includeStackTrace() {
    return BooleanUtils.toBooleanDefaultIfNull(getIncludeStackTrace(), true);
  }

}
