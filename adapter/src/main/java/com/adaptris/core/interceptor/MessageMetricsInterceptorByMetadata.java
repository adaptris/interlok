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

import javax.management.MalformedObjectNameException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.runtime.WorkflowManager;
import com.adaptris.util.KeyValuePair;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * WorkflowInterceptor implementation that captures metrics about messages that match a given metadata criteria.
 * 
 * <p>
 * This captures information about a message where that message contains the specified key and and value combination, the value
 * portion may be a regular expression. If the message metadata matches that configured, then it captures the total number of
 * messages that passed through this workflow, and captures the size of messages entering the workflow (but not the total size of
 * messages exiting the workflow); and also the number of messages that had an error condition at the end of the workflow.
 * </p>
 * 
 * @config message-metrics-interceptor-by-metadata
 * 
 */
@XStreamAlias("message-metrics-interceptor-by-metadata")
@AdapterComponent
@ComponentProfile(summary = "Interceptor that collates message counts based on metadata",
    tag = "interceptor")

public class MessageMetricsInterceptorByMetadata extends MessageMetricsInterceptorImpl {

  @NotNull
  @Valid
  private KeyValuePair metadataElement;

  private transient Object chubb = new Object();
  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  public MessageMetricsInterceptorByMetadata() {
    super();
  }

  public MessageMetricsInterceptorByMetadata(MetadataElement element) {
    this();
    setMetadataElement(element);
  }

  public void init() throws CoreException {
    if (metadataElement == null) {
      throw new CoreException("metadata element may not be null");
    }
    super.init();
  }

  public KeyValuePair getMetadataElement() {
    return metadataElement;
  }

  /**
   * Set the metadata element that this interceptor will filter on.
   * 
   * <p>
   * Note that the value part of this metadata may be a regular expression.
   * </p>
   * 
   * @param m the metadata element.
   */
  public void setMetadataElement(KeyValuePair m) {
    if (m == null) {
      throw new IllegalArgumentException("metadata-element may not be null");
    }
    this.metadataElement = m;
  }

  @Override
  public synchronized void workflowStart(AdaptrisMessage inputMsg) {
  }

  @Override
  public synchronized void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    if (captureMetric(inputMsg) || captureMetric(outputMsg)) {
      final int errors = wasSuccessful(inputMsg, outputMsg) ? 0 : 1;
      final int msgs = 1;
      final long size = inputMsg.getSize();
      update(new StatisticsDelta<MessageStatistic>() {
        @Override
        public MessageStatistic apply(MessageStatistic currentStat) {
          currentStat.setTotalMessageCount(currentStat.getTotalMessageCount() + msgs);
          currentStat.setTotalMessageSize(currentStat.getTotalMessageSize() + size);
          currentStat.setTotalMessageErrorCount(currentStat.getTotalMessageErrorCount() + errors);
          return currentStat;
        }
      });
    }
  }

  private boolean captureMetric(AdaptrisMessage msg) {
    boolean rc = false;
    String key = getMetadataElement().getKey();
    String valueRegexp = getMetadataElement().getValue();
    if (msg.containsKey(getMetadataElement().getKey())) {
      String value = msg.getMetadataValue(key);
      if (value.matches(valueRegexp)) {
        rc = true;
      }
    }
    return rc;
  }

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof MessageMetricsInterceptorByMetadata) {
        return !isEmpty(((MessageMetricsInterceptorByMetadata) e).getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new MessageMetricsStatistics((WorkflowManager) parent, (MessageMetricsInterceptorByMetadata) e);
    }

  }


}
