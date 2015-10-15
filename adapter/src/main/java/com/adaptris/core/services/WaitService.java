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

package com.adaptris.core.services;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>Service</code> for testing which sleeps for a configurable period.
 * </p>
 * 
 * @config wait-service
 * @license BASIC
 */
@XStreamAlias("wait-service")
public class WaitService extends ServiceImp {

  private static final TimeInterval DEFAULT_WAIT = new TimeInterval(20L, TimeUnit.SECONDS);

  private TimeInterval waitInterval;
  private Boolean randomize;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public WaitService() {
  }

  public WaitService(TimeInterval wait) {
    this(wait, null);
  }

  public WaitService(TimeInterval wait, Boolean randomize) {
    this();
    setWaitInterval(wait);
    setRandomize(randomize);
  }

  public WaitService(String uniqueId) {
    this();
    setUniqueId(uniqueId);
  }

  /**
   * <p>
   * Waits for the configured number of milliseconds.
   * </p>
   *
   * @param msg the message to apply service to
   * @throws ServiceException wrapping any underlying <code>Exception</code>s
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {

    try {
      long waitMs = waitMs();
      log.trace("waiting for [" + waitMs + "] ms...");
      Thread.sleep(waitMs);
    }
    catch (InterruptedException e) {
      ;
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() {
    // na
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    // na
  }

  long waitMs() {
    long maxWaitMs = getWaitInterval() != null ? getWaitInterval().toMilliseconds() : DEFAULT_WAIT.toMilliseconds();
    return randomizeWait() ? ThreadLocalRandom.current().nextLong(maxWaitMs) : maxWaitMs;
  }

  public TimeInterval getWaitInterval() {
    return waitInterval;
  }

  /**
   * Set how long to wait for.
   * 
   * @param interval if not specified then the default is 20 seconds.
   */
  public void setWaitInterval(TimeInterval interval) {
    this.waitInterval = interval;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }

  public Boolean getRandomize() {
    return randomize;
  }

  /**
   * Set to true to randomize the wait time between 0 and the value specified by {@link #setWaitInterval(TimeInterval)}
   * 
   * @param b default null (false)
   */
  public void setRandomize(Boolean b) {
    this.randomize = b;
  }

  boolean randomizeWait() {
    return getRandomize() != null ? getRandomize().booleanValue() : false;
  }
}
