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

package com.adaptris.core;

import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link Poller} which polls at a configurable fixed interval.
 * </p>
 * 
 * @config fixed-interval-poller
 * 
 */
@XStreamAlias("fixed-interval-poller")
public class FixedIntervalPoller extends ScheduledTaskPoller {

  private static final TimeInterval DEFAULT_POLL_INTERVAL = new TimeInterval(20L, TimeUnit.SECONDS);

  @Valid
  @AutoPopulated
  private TimeInterval pollInterval;

  public FixedIntervalPoller() {
  }

  public FixedIntervalPoller(TimeInterval interval) {
    setPollInterval(interval);
  }

  protected void scheduleTask() {
    pollerTask = executor.scheduleWithFixedDelay(new PollerTask(), 100L, pollInterval(), TimeUnit.MILLISECONDS);
    log.trace("Scheduled {}", pollerTask);
  }

  private class PollerTask implements Runnable {

    @Override
    public void run() {
      processMessages();
    }
  }

  long pollInterval() {
    return getPollInterval() != null ? getPollInterval().toMilliseconds() : DEFAULT_POLL_INTERVAL.toMilliseconds();
  }

  public TimeInterval getPollInterval() {
    return pollInterval;
  }

  /**
   * Set the poll interval.
   *
   * @param pollInterval the interval; default is 20 seconds.
   */
  public void setPollInterval(TimeInterval pollInterval) {
    this.pollInterval = pollInterval;
  }
}
