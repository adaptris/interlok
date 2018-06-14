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

package com.adaptris.core.interceptor;

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.apache.log4j.MDC;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.services.AddLoggingContext;
import com.adaptris.core.services.RemoveLoggingContext;
import com.adaptris.util.GuidGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * WorkflowInterceptor implementation that adds a mapped diagnotic context via {@link MDC#put(String, String)}.
 * <p>
 * Because the diagnostic logging context is thread based; this will only be useful when used as part of a single threaded workflow
 * such as {@link StandardWorkflow}; in {@link PoolingWorkfow} the context will be lost as the message enters the threadpool for
 * processing. A better alternative might be {@link AddLoggingContext} and {@link RemoveLoggingContext} as part of the service
 * execution chain.
 * </p>
 * 
 * @config logging-context-workflow-interceptor
 * @see AddLoggingContext
 * @see RemoveLoggingContext
 * 
 */
@XStreamAlias("logging-context-workflow-interceptor")
@AdapterComponent
@ComponentProfile(summary = "Interceptor that adds Logging Context at the start of a workflow, removes it at the end",
    tag = "interceptor")
public class LoggingContextWorkflowInterceptor extends WorkflowInterceptorImpl {

  private static final GuidGenerator GUID = new GuidGenerator();

  private String key;
  private String value;

  private transient String keyToUse;
  private transient String valueToUse;

  public LoggingContextWorkflowInterceptor() {
    super();
  }

  public LoggingContextWorkflowInterceptor(String uid) {
    this();
    setUniqueId(uid);
  }

  @Override
  public synchronized void workflowStart(AdaptrisMessage inputMsg) {
    MDC.put(keyToUse, valueToUse);
  }

  @Override
  public synchronized void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    MDC.remove(keyToUse);
  }

  @Override
  public void init() throws CoreException {
    keyToUse = resolve(getKey());
    valueToUse = resolve(getValue());
  }

  @Override
  public void start() throws CoreException {}

  @Override
  public void stop() {}

  @Override
  public void close() {}

  public String getKey() {
    return key;
  }

  /**
   * Set the context key.
   * <p>
   * If not specified then we will try to use one of the following values in order of preference provided they are not null/blank
   * </p>
   * <ul>
   * <li>The interceptors unique id</li>
   * <li>The parent workflow unique id</li>
   * <li>The parent channel unique id</li>
   * <li>A generated unique id</li>
   * </ul>
   * 
   * @param key the contextKey to set.
   */
  public void setKey(String key) {
    this.key = key;
  }

  private String resolve(String s) {
    if (!isEmpty(s))
      return s;
    if (!isEmpty(getUniqueId())) {
      return getUniqueId();
    }
    if (!isEmpty(parentWorkflow().getUniqueId())) {
      return parentWorkflow().getUniqueId();
    }
    if (!isEmpty(parentChannel().getUniqueId())) {
      return parentChannel().getUniqueId();
    }
    return GUID.safeUUID();
  }

  public String getValue() {
    return value;
  }

  /**
   * Set the context value.
   * <p>
   * If not specified then we will try to use one of the following values in order of preference provided they are not null/blank
   * </p>
   * <ul>
   * <li>The interceptors unique id</li>
   * <li>The parent workflow unique id</li>
   * <li>The parent channel unique id</li>
   * <li>A Generated unique id</li>
   * </ul>
   * 
   * @param val the contextValue to set
   */
  public void setValue(String val) {
    this.value = val;
  }

}
