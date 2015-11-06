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

package com.adaptris.core.jms;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.validation.Valid;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ProducerSessionFactory} that creates a new session/producer based an inactivity timer.
 * 
 * <p>
 * This implementaton refreshes the session based on some on the specified interval between the last message and the current message
 * </p>
 * 
 * @config jms-timed-inactivity-producer-session
 * @license STANDARD
 * @author lchan
 * 
 */
@XStreamAlias("jms-timed-inactivity-producer-session")
public class TimedInactivityProducerSessionFactory extends ProducerSessionFactoryImpl {

  private static final TimeInterval DEFAULT_INACVITY_INTERVAL = new TimeInterval(2L, TimeUnit.HOURS);
  private transient Date lastMsgTimestamp = new Date();

  @AutoPopulated
  @Valid
  private TimeInterval inactivityInterval;

  public TimedInactivityProducerSessionFactory() {
    super();
  }

  public TimedInactivityProducerSessionFactory(TimeInterval interval) {
    super();
    setInactivityInterval(interval);
  }

  @Override
  public ProducerSession createProducerSession(JmsProducerImpl producer, AdaptrisMessage msg)
      throws JMSException {
    if (newSessionRequired() || session == null) {
      if (newSessionRequired()) log.trace("Last message was {} new session required from {}", new Date(), refreshDate());
      closeQuietly(session);
      session = createProducerSession(producer);
      // While we won't be accurate to the millisecond; this is acceptable.
      lastMsgTimestamp = new Date();
    }
    return session;
  }

  boolean newSessionRequired() {
    Date now = new Date();
    return now.after(refreshDate());
  }

  private Date refreshDate() {
    Calendar next = Calendar.getInstance();
    next.setTime(lastMsgTimestamp);
    next.add(Calendar.MILLISECOND, Long.valueOf(inactivityIntervalMs()).intValue());
    return next.getTime();
  }

  public TimeInterval getInactivityInterval() {
    return inactivityInterval;
  }

  /**
   * Set the inactivity interval before a new session is created.
   * 
   * @param inactivityInterval the interval, if not specified, the default is 2 hours.
   */
  public void setInactivityInterval(TimeInterval inactivityInterval) {
    this.inactivityInterval = inactivityInterval;
  }

  private long inactivityIntervalMs() {
    return getInactivityInterval() != null ? getInactivityInterval().toMilliseconds() : DEFAULT_INACVITY_INTERVAL.toMilliseconds();
  }
}
