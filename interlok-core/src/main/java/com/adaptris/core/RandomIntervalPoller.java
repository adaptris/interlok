/*
 * Copyright 2020 Adaptris Ltd.
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

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@linkplain Poller} which polls at a random interval with a delay between each execution of up-to the
 * configured poll interval (in ms).
 * 
 * <p>
 * This implementation is of marginal use, and is best used to generate messages at pseudo-random intervals.
 * </p>
 * 
 * @config random-interval-poller
 * 
 */
@XStreamAlias("random-interval-poller")
public class RandomIntervalPoller extends FixedIntervalPoller {

  public RandomIntervalPoller() {
    super();
  }

  public RandomIntervalPoller(TimeInterval interval) {
    super(interval);
  }

  private long millToMill(long milliseconds) {
    return TimeUnit.MILLISECONDS.toMillis(milliseconds);
  }

  private long millToSec(long milliseconds) {
    return TimeUnit.MILLISECONDS.toSeconds(milliseconds);
  }

  private long millToMin(long milliseconds) {
    return TimeUnit.MILLISECONDS.toMinutes(milliseconds);
  }

  private long millToHour(long milliseconds) {
    return TimeUnit.MILLISECONDS.toHours(milliseconds);
  }

  private long residualTime(String measure, long millis) {
    if(measure == "minutes") {
      return millToMin(millis) - (millToHour(millis) * 60);
    }
    else if(measure == "seconds"){
      return millToSec(millis) - (millToMin(millis) * 60);
    }
    else {
      return millToMill(millis) - (millToSec(millis) * 1000);
    }
  }  

  @Override
  protected void scheduleTask() {
    if (executor != null && !executor.isShutdown()) {
      long delay = ThreadLocalRandom.current().nextLong(pollInterval());
      pollerTask = executor.schedule(new MyPollerTask(), delay, TimeUnit.MILLISECONDS);

      if(delay < 5000L) {
        log.trace("Next Execution scheduled in {}ms", millToMill(delay));
      }
      else if((delay >= 5000L) && (delay <= 120000L)) {
        log.trace("Next Execution scheduled in: {}s {}ms", millToSec(delay), residualTime("milliseconds", delay));
      }
      else if ((delay > 120000L) && (delay <= 7200000L)) {
        log.trace("Next Execution scheduled in: {}m {}s",millToMin(delay), residualTime("seconds", delay));
      }
      else {
        log.trace("Next Execution scheduled in: {}h {}m", millToHour(delay), residualTime("minutes", delay));
      }
    }
  }

  private class MyPollerTask implements Runnable {
    @Override
    public void run() {
      processMessages();
      scheduleTask();
    }
  }
}
