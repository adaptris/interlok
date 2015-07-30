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
