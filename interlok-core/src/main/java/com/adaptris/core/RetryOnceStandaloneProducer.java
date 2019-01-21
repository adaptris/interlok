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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * An implementation of StandaloneProducer that on encountering an error producing a message, waits for a configurable period,
 * re-initialises the underlying components, then tries to produce once one more.
 * <p>
 * As some internal components have relationships that are persistent across their normal lifecycle, this class additionally
 * marshals each connection and producer implementation to XML and back again prior to initialisation.
 * </p>
 * 
 * @config retry-once-standalone-producer
 */
@XStreamAlias("retry-once-standalone-producer")
@AdapterComponent
@ComponentProfile(summary = "Produce a message where as part of a service chain; retrying once on failure", tag = "service")
@DisplayOrder(order = {"connection", "producer", "waitBeforeRetry", "marshaller"})
public class RetryOnceStandaloneProducer extends StandaloneProducer {

  private static final TimeInterval DEFAULT_WAIT_BEFORE_RETRY = new TimeInterval(30L, TimeUnit.SECONDS);

  private transient AdaptrisMarshaller marshaller;
  private TimeInterval waitBeforeRetry;

  public RetryOnceStandaloneProducer() {
    super();
    marshaller = DefaultMarshaller.getDefaultMarshaller();
  }

  /** @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage) */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      this.produce(msg);
    }
    catch (ProduceException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    this.produce(msg, null);
  }

  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination dest) throws ProduceException {
    try {
      tryProduce(msg, dest);
    }
    catch (Exception e) {
      restart();
      tryProduce(msg, dest);
    }
  }

  private void tryProduce(AdaptrisMessage msg, ProduceDestination dest) throws ProduceException {
    if (dest != null) {
      super.produce(msg, dest);
    }
    else {
      super.produce(msg);
    }
  }

  private void restart() throws ProduceException {
    log.warn("Exception producing message, sleeping for [" + waitBeforeRetry() + "] ms before retry");
    sleep();
    stopAndClose();
    try {
      initAndStart();
    }
    catch (CoreException e) {
      throw new ProduceException(e);
    }
  }

  private void sleep() {
    try {
      Thread.sleep(waitBeforeRetry());
    }
    catch (InterruptedException e2) {
      // ignore...
    }
  }

  private void initAndStart() throws CoreException {
    AdaptrisConnection c = (AdaptrisConnection) cloneComponent(getConnection());
    super.setConnection(c);
    AdaptrisMessageProducer p = (AdaptrisMessageProducer) cloneComponent(getProducer());
    super.setProducer(p);
    super.requestStart();
  }

  private void stopAndClose() {
    super.requestClose();
  }

  private AdaptrisComponent cloneComponent(AdaptrisComponent o) throws CoreException {
    String marshalled = marshaller.marshal(o);
    AdaptrisComponent sc = (AdaptrisComponent) marshaller.unmarshal(marshalled);
    return sc;
  }

  // properties

  long waitBeforeRetry() {
    return TimeInterval.toMillisecondsDefaultIfNull(getWaitBeforeRetry(),
        DEFAULT_WAIT_BEFORE_RETRY);
  }

  public TimeInterval getWaitBeforeRetry() {
    return waitBeforeRetry;
  }

  /**
   * Sets the period to wait before trying to produce again.
   * 
   * @param waitBeforeRetry the period to wait before trying to produce again, if not specified defaults to 30 seconds.
   */
  public void setWaitBeforeRetry(TimeInterval waitBeforeRetry) {
    this.waitBeforeRetry = waitBeforeRetry;
  }
}
