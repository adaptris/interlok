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

import com.adaptris.core.stubs.FailFirstMockMessageProducer;
import com.adaptris.core.stubs.StaticCounterFailFirstMockMessageProducer;
import com.adaptris.util.TimeInterval;

public class RetryOnceStandaloneProducerTest extends GeneralServiceExample {

  private RetryOnceStandaloneProducer service;

  public RetryOnceStandaloneProducerTest(String name) {
    super(name);
    service = new RetryOnceStandaloneProducer();
    service.setWaitBeforeRetry(new TimeInterval(0L, TimeUnit.MILLISECONDS));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new RetryOnceStandaloneProducer(); // defaults are fine for example
  }

  public void testSetWait() throws Exception {
    TimeInterval defaultInterval = new TimeInterval(30L, TimeUnit.SECONDS);
    TimeInterval interval = new TimeInterval(60L, TimeUnit.SECONDS);

    RetryOnceStandaloneProducer p = new RetryOnceStandaloneProducer();
    assertNull(p.getWaitBeforeRetry());
    assertEquals(defaultInterval.toMilliseconds(), p.waitBeforeRetry());

    p.setWaitBeforeRetry(interval);
    assertEquals(interval, p.getWaitBeforeRetry());
    assertEquals(interval.toMilliseconds(), p.waitBeforeRetry());

    p.setWaitBeforeRetry(null);
    assertNull(p.getWaitBeforeRetry());
    assertEquals(defaultInterval.toMilliseconds(), p.waitBeforeRetry());
  }

  public void testProducerIsSuccessfulFirstTimeAsService() {
    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    }
    catch (Exception e) {
      fail();
    }
  }

  public void testProducerFailsTwiceAsService() {
    FailFirstMockMessageProducer producer = new FailFirstMockMessageProducer();
    producer.setFailUntilCount(2);

    service.setProducer(producer);

    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail();
    }
    catch (Exception e) {
       // expected
    }
  }

  public void testProducerFailsFirstTimeOnlyAsService() {
	  StaticCounterFailFirstMockMessageProducer producer = new StaticCounterFailFirstMockMessageProducer();
	  producer.setFailUntilCount(0);
	  producer.resetCount();
    this.service.setProducer(producer);

    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    }
    catch (Exception e) {
      fail();
    }
  }

  public void testProducerIsSuccessfulFirstTimeAsProducer() {
    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    }
    catch (Exception e) {
      fail();
    }
  }

  public void testProducerFailsTwiceAsProducer() {
    FailFirstMockMessageProducer producer = new FailFirstMockMessageProducer();
    producer.setFailUntilCount(2);

    service.setProducer(producer);

    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail();
    }
    catch (Exception e) {
       // expected
    }
  }

  public void testProducerFailsFirstTimeOnlyAsProducer() {
	  StaticCounterFailFirstMockMessageProducer producer = new StaticCounterFailFirstMockMessageProducer();
	  producer.setFailUntilCount(0);
	  producer.resetCount();
    this.service.setProducer(producer);

    try {
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    }
    catch (Exception e) {
      fail();
    }
  }
}
