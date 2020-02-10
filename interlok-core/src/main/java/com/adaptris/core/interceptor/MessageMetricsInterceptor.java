/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.interceptor;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.management.MalformedObjectNameException;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.runtime.WorkflowManager;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * WorkflowInterceptor implementation that exposes metrics via JMX.
 * 
 * <p>
 * This workflow interceptor captures the total number of messages that passed through this workflow, and captures the size of
 * messages entering the workflow (but not the total size of messages exiting the workflow); and also the number of messages that
 * had an error condition at the end of the workflow.
 * </p>
 * 
 * @config message-metrics-interceptor
 * 
 */
@XStreamAlias("message-metrics-interceptor")
@AdapterComponent
@ComponentProfile(summary = "Interceptor that captures the total number of messages passing through the workflow",
    tag = "interceptor")
public class MessageMetricsInterceptor extends MessageMetricsInterceptorImpl {
  
  public static final String UID_SUFFIX = "-MessageMetrics";

  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  public MessageMetricsInterceptor() {
    super();
  }

  public MessageMetricsInterceptor(String uid, TimeInterval timesliceDuration) {
    this(uid, timesliceDuration, null);
  }

  public MessageMetricsInterceptor(String uid, TimeInterval timesliceDuration, Integer historyCount) {
    this();
    setUniqueId(uid);
    setTimesliceDuration(timesliceDuration);
    setTimesliceHistoryCount(historyCount);
  }

  @Override
  public synchronized void workflowStart(AdaptrisMessage inputMsg) {

  }

  @Override
  public synchronized void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
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

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof MessageMetricsInterceptor) {
        return !isEmpty(((MessageMetricsInterceptor) e).getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new MessageMetricsStatistics((WorkflowManager) parent, (MessageMetricsInterceptor) e);
    }

  }
}
