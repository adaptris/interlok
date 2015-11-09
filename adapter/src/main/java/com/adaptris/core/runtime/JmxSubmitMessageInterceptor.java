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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.Workflow;
import com.adaptris.core.WorkflowInterceptor;
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
 * @license BASIC
 * @since 3.0.4
 */
@XStreamAlias("jmx-submit-message-interceptor")
public class JmxSubmitMessageInterceptor implements WorkflowInterceptor {
  
  private String uniqueId;

  private MessageCache messageCache;
  
  public JmxSubmitMessageInterceptor() {
    this.setMessageCache(new LruBoundedMessageCache());
  }
  
  @Override
  public void workflowStart(AdaptrisMessage inputMsg) {
  }

  @Override
  public void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    CacheableAdaptrisMessageWrapper wrapper = new CacheableAdaptrisMessageWrapper();
    wrapper.setMessageId(outputMsg.getUniqueId());
    wrapper.setMessage(outputMsg);
    this.getMessageCache().put(wrapper);
  }
  
  @Override
  public void prepare() throws CoreException {
  }

  @Override
  public void init() throws CoreException {
    this.getMessageCache().init();
  }

  @Override
  public void start() throws CoreException {
    this.getMessageCache().start();
  }

  @Override
  public void stop() {
    this.getMessageCache().stop();
  }

  @Override
  public void close() {
    this.getMessageCache().close();
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
