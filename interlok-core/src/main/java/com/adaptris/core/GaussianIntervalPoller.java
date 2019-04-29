package com.adaptris.core;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 *   Implementation of {@linkplain Poller} which polls at a random interval with a normal distribution.
 * </p>
 *
 * <p>
 *   Note: Logic is as follows:
 *   <br/>
 *   <br/>
 *   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@linkplain java.util.Random#nextGaussian()} * {@linkplain #getMeanInterval()} + {@linkplain #getStandardDeviationInterval()}.
 *
 * </p>
 *
 * @author mcwarman
 * @config gaussian-interval-poller
 * 
 */
@XStreamAlias("gaussian-interval-poller")
public class GaussianIntervalPoller extends ScheduledTaskPoller  {

  private static final TimeInterval DEFAULT_MEAN_INTERVAL = new TimeInterval(0L, TimeUnit.SECONDS);
  private static final TimeInterval DEFAULT_STANDARD_DEVIATION_INTERVAL = new TimeInterval(20L, TimeUnit.SECONDS);

  @Valid
  @AutoPopulated
  private TimeInterval meanInterval;
  @Valid
  @AutoPopulated
  private TimeInterval standardDeviationInterval;

  /**
   * <p>
   * Creates a new instance.  Default mean is 0 seconds and standard deviation is 20 seconds.
   * </p>
   */
  @SuppressWarnings("WeakerAccess")
  public GaussianIntervalPoller() {
  }

  @SuppressWarnings("WeakerAccess")
  public GaussianIntervalPoller(TimeInterval meanInterval, TimeInterval standardDeviationInterval) {
    setMeanInterval(meanInterval);
    setStandardDeviationInterval(standardDeviationInterval);
  }

  @Override
  public void init() throws CoreException {
    if(standardDeviationInterval() <= 0){
      throw new CoreException("Gaussian Interval Poller - Standard Deviation cannot be less than or equal to zero");
    }
    if(meanInterval() < 0){
      throw new CoreException("Gaussian Interval Poller - Mean cannot be less than zero");
    }
  }

  protected void scheduleTask() {
    long delay = delay();
    pollerTask = executor.schedule(new GaussianIntervalPollerTask(), delay, TimeUnit.MILLISECONDS);
    log.trace("Next Execution scheduled in {}ms", delay);
  }

  private class GaussianIntervalPollerTask implements Runnable {
    @Override
    public void run() {
      processMessages();
      scheduleTask();
    }
  }

  private long delay(){
    long delay;
    do {
      double val = ThreadLocalRandom.current().nextGaussian() * standardDeviationInterval() + meanInterval();
      delay = (int) Math.round(val);
    } while (delay <= 0);
    return delay;
  }

  long meanInterval() {
    return TimeInterval.toMillisecondsDefaultIfNull(getMeanInterval(), DEFAULT_MEAN_INTERVAL);
  }

  @SuppressWarnings("WeakerAccess")
  public TimeInterval getMeanInterval() {
    return meanInterval;
  }

  /**
   * Set the mean to be used in poll interval calculation.
   *
   * @param meanInterval the mean interval (default 0 seconds)
   */
  @SuppressWarnings("WeakerAccess")
  public void setMeanInterval(TimeInterval meanInterval) {
    this.meanInterval = meanInterval;
  }

  long standardDeviationInterval() {
    return TimeInterval.toMillisecondsDefaultIfNull(getStandardDeviationInterval(),
        DEFAULT_STANDARD_DEVIATION_INTERVAL);
  }

  @SuppressWarnings("WeakerAccess")
  public TimeInterval getStandardDeviationInterval() {
    return standardDeviationInterval;
  }

  /**
   * Set the standard deviation to be used in poll interval calculation.
   *
   * @param standardDeviationInterval the standard deviation interval (default 20 seconds)
   */
  @SuppressWarnings("WeakerAccess")
  public void setStandardDeviationInterval(TimeInterval standardDeviationInterval) {
    this.standardDeviationInterval = standardDeviationInterval;
  }
}
