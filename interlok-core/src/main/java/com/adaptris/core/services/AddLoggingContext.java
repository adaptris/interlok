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

import javax.validation.constraints.NotBlank;
import org.slf4j.MDC;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.services.splitter.PoolingMessageSplitterService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Add a mapped diagnostic context via {@link MDC#put(String, String)}.
 * <p>
 * It can be useful to use a mapped diagnostic context to provide additional information into your logfile if the underlying logging
 * system supports it (e.g. logback or log4j2)
 * </p>
 * <p>
 * As the diagnostic logging context is thread based; bear in mind that you will lose the context if part of the service execution
 * chain contains a something like {@link PoolingMessageSplitterService} or similar (i.e. something with an underlying thread pool
 * that acts on the message).
 * </p>
 * 
 * @config add-logging-context-service
 * 
 */
@XStreamAlias("add-logging-context-service")
@AdapterComponent
@ComponentProfile(summary = "Add a mapped diagnostic context for logging; useful for filtering", tag = "service,logging,debug")
@DisplayOrder(order = { "key", "value"})
public class AddLoggingContext extends ServiceImp {
  private static final String UNIQUE_ID_MNENOMIC = "$UNIQUE_ID$";

  @NotBlank
  @InputFieldHint(expression = true)
  private String key;
  @NotBlank
  @InputFieldHint(expression = true)
  private String value;

  public AddLoggingContext() {
    super();
  }

  public AddLoggingContext(String key, String value) {
    this();
    setKey(key);
    setValue(value);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      MDC.put(msg.resolve(getKey()), resolveValue(msg));
    }
    catch (IllegalArgumentException | IllegalStateException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private String resolveValue(AdaptrisMessage msg) {
    String value = msg.resolve(getValue());
    if (UNIQUE_ID_MNENOMIC.equals(value)) {
      return msg.getUniqueId();
    }
    return value;
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

  public String getKey() {
    return key;
  }

  /**
   * Set the key for the mapped diagnostic context.
   * 
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = Args.notBlank(key, "key");
  }

  public String getValue() {
    return value;
  }

  /**
   * Set the value for the mapped diagnostic context.
   * <p>
   * Supports metadata resolution via {@link AdaptrisMessage#resolve(String)}; and also the magic value {@code $UNIQUE_ID$} which
   * places the unique-id as the value.
   * </p>
   * 
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = Args.notBlank(value, "value");
  }

}
