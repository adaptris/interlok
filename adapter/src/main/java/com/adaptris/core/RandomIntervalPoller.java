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
 * @license BASIC
 */
@XStreamAlias("random-interval-poller")
public class RandomIntervalPoller extends FixedIntervalPoller {

  public RandomIntervalPoller() {
    super();
  }

  public RandomIntervalPoller(TimeInterval interval) {
    super(interval);
  }

  protected void scheduleTask() {
    long delay = ThreadLocalRandom.current().nextLong(pollInterval());
    pollerTask = executor.schedule(new MyPollerTask(), delay, TimeUnit.MILLISECONDS);
    log.trace("Next Execution scheduled in {}ms", delay);
  }

  private class MyPollerTask implements Runnable {
    @Override
    public void run() {
      processMessages();
      scheduleTask();
    }
  }
}
