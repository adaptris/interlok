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

package com.adaptris.core.runtime;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.Workflow;
import com.adaptris.core.WorkflowInterceptor;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A {@link WorkflowInterceptor} that simply caches all messages that have completed running through
 * the workflow.
 * </p>
 * <p>
 * This implementation uses a {@link MessageCache} which is defaulted to
 * {@link LruBoundedMessageCache}.
 * </p>
 * 
 * @config jmx-submit-message-interceptor
 * 
 * @since 3.0.4
 */
@XStreamAlias("jmx-submit-message-interceptor")
@AdapterComponent
@ComponentProfile(summary = "Interceptor for ensuring JMX submissions are handled correctly", tag = "interceptor,jmx")
public class JmxSubmitMessageInterceptor implements WorkflowInterceptor {
  
  private String uniqueId;

  @Valid
  @NotNull
  @AutoPopulated
  private MessageCache messageCache;
  
  public JmxSubmitMessageInterceptor() {
    this.setMessageCache(new LruBoundedMessageCache());
  }
  
  @Override
  public void workflowStart(AdaptrisMessage inputMsg) {
  }

  @Override
  public synchronized void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    this.getMessageCache().put(new CacheableAdaptrisMessageWrapper(outputMsg.getUniqueId(), outputMsg));
  }
  
  @Override
  public void prepare() throws CoreException {
  }

  @Override
  public void init() throws CoreException {
    LifecycleHelper.init(getMessageCache());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getMessageCache());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getMessageCache());
  }

  @Override
  public void close() {
    LifecycleHelper.close(getMessageCache());
  }

  @Override
  public void registerParentChannel(Channel c) {
  }

  @Override
  public void registerParentWorkflow(Workflow w) {
  }

  @Override
  public String getUniqueId() {
    return this.uniqueId;
  }
  
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  public MessageCache getMessageCache() {
    return messageCache;
  }

  public void setMessageCache(MessageCache messageCache) {
    this.messageCache = messageCache;
  }

}
