package com.adaptris.core.runtime;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.Workflow;
import com.adaptris.core.WorkflowInterceptor;
import com.adaptris.util.license.License;
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
  public boolean isEnabled(License license) throws CoreException {
    return true;
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
