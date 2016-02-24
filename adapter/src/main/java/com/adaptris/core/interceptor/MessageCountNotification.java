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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.runtime.WorkflowManager;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Interceptor that emits a {@link javax.management.Notification} under based on message count being higher or lower
 * than the given message count.
 * <p>
 * A Notification will be emitted when the message count threshold is first exceeded in the last
 * complete timeslice. Notifications will continue being emitted for as long as the message count is
 * greater than the threshold and {@link #getMaxNotifications()} has not been exceeded. When the
 * message count dips below the threshold a notification is emitted with a different
 * {@link javax.management.Notification#getMessage()}.
 * </p>
 * <p>
 * The {@link javax.management.Notification#setUserData(Object)} part of the notification is a {@link java.util.Properties}
 * object containing information about the slice which was the source of the notification.
 * Notifications are only generated based on the last complete timeslice that was recorded when a
 * message enters the workflow. Note that a workflow which does not process any messages within a
 * given timeslice will never rollover the current timeslice so this means that notifications are
 * not generated until the current timeslice rolls over or a message is next processed by the
 * workflow. Any delay in notifications will be based on the spikiness of the traffic and the
 * granularity of your timeslice.
 * </p>
 * <p>
 * Additionally, this interceptor will notify a consecutive maximum of
 * {@link #getMaxNotifications()} (if not specified, then notifications continue forever) whenever a
 * boundary is breached. For instance, if you have {@link #getMaxNotifications()} set to 5, then a
 * maximum of 5 notifications will be emitted for an upper boundary breach provided no lower
 * boundary breaches happen in-between. If the configuration is
 * {@code max-notifications=3, message-count=10} then our notifications happen like this:
 * </p>
 * 
 * <pre>
 * {@code
 * |--Timeslice--|--MsgCount--|--Upper Boundary Notification--|--Lower Boundary Notification--|
 * |      01     |    11      | No (no previous timeslice)    |             No                |
 * |      02     |    12      | Yes (count = 11)              |             No                |
 * |      03     |    13      | Yes (count = 12)              |             No                |
 * |      04     |    00      |            No                 |             No                |
 * |      05     |    11      | Yes, (13) timeslice changed   |             No                |
 * |      06     |    01      |            No                 |             No                |
 * |      07     |    11      |            No                 |   Yes (count=1)               |
 * |      08     |    12      | Yes (count = 11)              |             No                |
 * |      09     |    13      | Yes (count = 12)              |             No                |
 * |      10     |    14      | Yes (count = 13)              |             No                |
 * |      11     |    01      | No, as max exceeded           |             No                |
 * }
 * </pre>
 * 
 * @config message-count-notification
 * 
 * @since 3.0.4
 */
@XStreamAlias("message-count-notification")
@AdapterComponent
@ComponentProfile(summary = "Interceptor that issues a JMX notification based on a message count boundary",
 tag = "interceptor,jmx")
@DisplayOrder(order = {"messageCount", "maxNotifications"})
public class MessageCountNotification extends NotifyingInterceptorByCount {

  /**
   * The {@link javax.management.Notification#getMessage()} when the message count is below the configured threshold.
   * 
   */
  public static final String NOTIF_MESSAGE_BELOW_THRESHOLD = "Message Count Below Boundary";
  /**
   * The {@link javax.management.Notification#getMessage()} when the message count is above the configured threshold.
   * 
   */
  public static final String NOTIF_MESSAGE_ABOVE_THRESHOLD = "Message Count Above Boundary";

  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }

  private enum Threshold {
    Count {
      @Override
      int higher(MessageStatistic stat, MessageCountNotification threshold) {
        if (threshold.getMessageCount() != null) {
          int upper = threshold.getMessageCount().intValue();
          if (stat.getTotalMessageCount() > upper) {
            return 1;
          }
        }
        return 0;
      }

      @Override
      int lower(MessageStatistic stat, MessageCountNotification threshold) {
        if (threshold.getMessageCount() != null) {
          int lower = threshold.getMessageCount().intValue();
          if (stat.getTotalMessageCount() <= lower) {
            return 1;
          }
        }
        return 0;
      }

    };
    abstract int higher(MessageStatistic stat, MessageCountNotification interceptor);

    abstract int lower(MessageStatistic stat, MessageCountNotification interceptor);
  }

  private enum NotificationType {
    Above() {

      @Override
      void mark(NotificationCount counter) {
        counter.above++;
        counter.below = 0;
      }

      @Override
      boolean shouldNotify(MessageCountNotification interceptor, NotificationCount count) {
        if (interceptor.getMaxNotifications() != null) {
          return interceptor.getMaxNotifications() > count.above;
        }
        return true;
      }
      
    },
    Below() {
      @Override
      void mark(NotificationCount counter) {
        counter.below++;
        counter.above = 0;
      }

      @Override
      boolean shouldNotify(MessageCountNotification interceptor, NotificationCount count) {
        if (interceptor.getMaxNotifications() != null) {
          return interceptor.getMaxNotifications() > count.below;
        }
        return true;
      }
    };
    
    abstract void mark(NotificationCount counter);
    abstract boolean shouldNotify(MessageCountNotification interceptor, NotificationCount count);
    
  }

  private Integer messageCount;
  private Integer maxNotifications;


  private transient MessageStatistic previousTimeSlice;
  private transient NotificationCount notificationCount;
  private transient boolean onceTooHigh;

  public MessageCountNotification() {
    super();
  }

  public MessageCountNotification(String uid) {
    this();
    setUniqueId(uid);
  }

  public MessageCountNotification(String uid, TimeInterval duration) {
    this(uid);
    setTimesliceDuration(duration);
  }

  @Override
  public synchronized void workflowStart(AdaptrisMessage inputMsg) {
    if (previousTimeSlice == null) {
      previousTimeSlice = getCurrentTimeSlice();
    }
    handleNotification(getCurrentTimeSlice());
  }


  @Override
  public synchronized void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    MessageStatistic currentTimeSlice = getAndIncrementStatistic(inputMsg, outputMsg);
  }

  @Override
  public void init() throws CoreException {
    notificationCount = new NotificationCount();
  }

  @Override
  public void start() throws CoreException {}

  @Override
  public void stop() {}

  @Override
  public void close() {}

  private void handleNotification(MessageStatistic current) {
    if (previousTimeSlice != current) {
      if (messageCountAbove(previousTimeSlice)) {
        if (NotificationType.Above.shouldNotify(this, notificationCount)) {
          sendNotification(NOTIF_MESSAGE_ABOVE_THRESHOLD, asProperties(previousTimeSlice));
          NotificationType.Above.mark(notificationCount);
        }
        onceTooHigh = true;
      } else if (messageCountBelow(previousTimeSlice) && onceTooHigh) {
        // Now if the count is < messageCount and we previously notified then
        // we might have to do something.
        if (NotificationType.Below.shouldNotify(this, notificationCount)) {
          sendNotification(NOTIF_MESSAGE_BELOW_THRESHOLD, asProperties(previousTimeSlice));
          NotificationType.Below.mark(notificationCount);
        }
      } else {
        // Well otherwise, it's situation normal.
        onceTooHigh = false;
      }
      previousTimeSlice = current;
    }
  }

  private boolean messageCountBelow(MessageStatistic stat) {
    int rc = 0;
    for (Threshold t : Threshold.values()) {
      rc += t.lower(stat, this);
    }
    return rc > 0;
  }

  private boolean messageCountAbove(MessageStatistic stat) {
    int rc = 0;
    for (Threshold t : Threshold.values()) {
      rc += t.higher(stat, this);
    }
    return rc > 0;
  }

  public Integer getMessageCount() {
    return messageCount;
  }

  /**
   * Set the upper boundary over which notifications will be emitted.
   * 
   * @param l the threshold, defaults to null which means no notification on this boundary
   */
  public void setMessageCount(Integer l) {
    this.messageCount = l;
  }


  public Integer getMaxNotifications() {
    return maxNotifications;
  }

  /**
   * Set the maximum number of consecutive notifications to emit.
   * 
   * @param i the max number of consecutive notifications.
   */
  public void setMaxNotifications(Integer i) {
    this.maxNotifications = i;
  }

  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof MessageCountNotification) {
        return !isEmpty(((MessageCountNotification) e).getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e)
        throws MalformedObjectNameException {
      return new InterceptorNotification((WorkflowManager) parent, (MessageCountNotification) e);
    }
  }

  private static class NotificationCount {
    private int above, below;
  }
}
