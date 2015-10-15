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

import java.util.Properties;

import javax.management.MalformedObjectNameException;
import javax.management.Notification;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.runtime.WorkflowManager;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Interceptor that emits a {@link Notification} if the number of messages has exceeded the
 * specified threshold in the current timeslice.
 * <p>
 * The {@link Notification#setUserData(Object)} part of the notification is a {@link Properties}
 * object containing information about the various counts that exceeded the interceptors threshold.
 * Note that notifications are emitted whenever a message is deemed to have exceeded the threshold;
 * so you will get multiple notifications whenever a message causes the threshold to be exceeded
 * until the next time-slice is activated.
 * </p>
 * 
 * @config message-threshold-notification
 * @license STANDARD
 * @since 3.0.4
 */
@XStreamAlias("message-threshold-notification")
public class MessageThresholdNotification extends NotifyingInterceptorByCount{

  private Long countThreshold;
  private Long errorThreshold;
  private Long sizeThreshold;

  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  private enum Threshold {
    Size {
      @Override
      int check(MessageStatistic stat, MessageThresholdNotification threshold) {
        if (threshold.getSizeThreshold() != null && stat.getTotalMessageSize() > threshold.getSizeThreshold().longValue()) {
          return 1;
        }
        return 0;
      }
    },
    Error {
      @Override
      int check(MessageStatistic stat, MessageThresholdNotification threshold) {
        if (threshold.getErrorThreshold() != null && stat.getTotalMessageErrorCount() > threshold.getErrorThreshold().longValue()) {
          return 1;
        }
        return 0;
      }
    },
    Count {
      @Override
      int check(MessageStatistic stat, MessageThresholdNotification threshold) {
        if (threshold.getCountThreshold() != null && stat.getTotalMessageCount() > threshold.getCountThreshold().longValue()) {
          return 1;
        }
        return 0;
      }

    };
    abstract int check(MessageStatistic stat, MessageThresholdNotification threshold);
  }


  public MessageThresholdNotification() {
    super();
  }

  public MessageThresholdNotification(String uid) {
    this();
    setUniqueId(uid);
  }

  @Override
  public void workflowStart(AdaptrisMessage inputMsg) {
  }

  @Override
  public synchronized void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    MessageStatistic currentTimeSlice = getAndIncrementStatistic(inputMsg, outputMsg);
    if (shouldNotify(currentTimeSlice)) {
      sendNotification("Message Threshold Exceeded", asProperties(currentTimeSlice));
    }
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  private boolean shouldNotify(MessageStatistic stat) {
    int rc = 0;
    for (Threshold c : Threshold.values()) {
      rc += c.check(stat, this);
    }
    return rc > 0;
  }

  public Long getCountThreshold() {
    return countThreshold;
  }

  /**
   * Set the message count threshold on which notifications will be emitted.
   * 
   * @param l the threshold, defaults to null which means no notification on this metric
   */
  public void setCountThreshold(Long l) {
    this.countThreshold = l;
  }

  public Long getErrorThreshold() {
    return errorThreshold;
  }

  /**
   * Set the message error count threshold on which notifications will be emitted.
   * 
   * @param l the threshold, defaults to null which means no notifications on metric.
   */
  public void setErrorThreshold(Long l) {
    this.errorThreshold = l;
  }

  public Long getSizeThreshold() {
    return sizeThreshold;
  }

  /**
   * Set the total message size threshold (in bytes) on which notifications will be emitted.
   * 
   * @param l the threshold, defaults to null which means no notifications on metric.
   */
  public void setSizeThreshold(Long l) {
    this.sizeThreshold = l;
  }

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof MessageThresholdNotification) {
        return !isEmpty(((MessageThresholdNotification) e).getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent,
        AdaptrisComponent e) throws MalformedObjectNameException {
      return new InterceptorNotification((WorkflowManager) parent, (MessageThresholdNotification) e);
    }

  }
}
