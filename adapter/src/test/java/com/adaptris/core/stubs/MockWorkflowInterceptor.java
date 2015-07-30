package com.adaptris.core.stubs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.interceptor.WorkflowInterceptorImpl;

public class MockWorkflowInterceptor extends WorkflowInterceptorImpl implements MessageCounter {

  private Set<AdaptrisMessage> messages;

  public MockWorkflowInterceptor() {
    messages = new HashSet<AdaptrisMessage>();
  }

  /**
   * <p>
   * Returns the internal store of produced messages.
   * </p>
   *
   * @return the internal store of produced messages
   */
  @Override
  public List<AdaptrisMessage> getMessages() {
    return new ArrayList<AdaptrisMessage>(messages);
  }

  @Override
  public int messageCount() {
    return messages.size();
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  public void start() throws CoreException {
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  public void stop() {
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
  }

  public void workflowStart(AdaptrisMessage inputMsg) {
    messages.add(inputMsg);
  }

  @Override
  public void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    // This'll probably return false because it already exists in the set.
    messages.add(inputMsg);
  }

}
