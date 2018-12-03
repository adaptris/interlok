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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.Removal;
import com.adaptris.core.util.Args;

/**
 * <p>
 * Partial implementation of <code>Poller</code>.
 * </p>
 */
public abstract class PollerImp implements Poller {
  
  @Deprecated
  @Removal(version = "3.9.0")
  private String uniqueId;
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private transient AdaptrisPollingConsumer consumer;
  private transient Callback callback = e -> {};

  protected boolean attemptLock() {
    return retrieveConsumer().attemptLock();  }

  protected void releaseLock() {
    retrieveConsumer().releaseLock();
  }

  // properties

  /** @see com.adaptris.core.Poller#retrieveConsumer() */
  @Override
  public AdaptrisPollingConsumer retrieveConsumer() {
    return consumer;
  }

  /** @see com.adaptris.core.Poller#registerConsumer
   *   (com.adaptris.core.AdaptrisPollingConsumer) */
  @Override
  public void registerConsumer(AdaptrisPollingConsumer c) {
    consumer = c;
  }

  public <T extends Poller> T withPollerCallback(Callback c) {
    callback = Args.notNull(c, "callback");
    return (T) this;
  }

  /**
   * <p>
   * Message processing behaviour, which is common to concrete implementations.
   * (The difference is how it is triggered.)
   * </p>
   */
  protected void processMessages() {
    String oldName = retrieveConsumer().renameThread();
    int count = -1;
    if (attemptLock()) { // try to get the lock
      try {
        long start = System.currentTimeMillis();
        count = retrieveConsumer().processMessages();
        log.debug("time to process [{}] messages [{}] ms", count, (System.currentTimeMillis() - start));
      }
      catch (Exception e) { // mop up any runtime
        log.error("exception thrown to run", e);

        try {
          retrieveConsumer().handleConnectionException();
        }
        catch (CoreException e2) {
          log.error("exception handling connection exception", e2);
        }
      }
      finally { // we always have the lock here
        releaseLock();
      }
    }
    else {
      log.trace("couldn't get lock in run (previous poll has not finished?); waiting for next scheduled poll.");
    }
    callback.pollTriggered(count);
    Thread.currentThread().setName(oldName);
  }

  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   * 
   * @deprecated since 3.6.3
   */
  @Deprecated
  @Removal(version = "3.9.0")
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   * 
   * @deprecated since 3.6.3
   */
  @Deprecated
  @Removal(version = "3.9.0")
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  @FunctionalInterface
  public interface Callback {
    void pollTriggered(int msgsAttempted);
  }
}
