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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class RetryingServiceWrapperTest extends GeneralServiceExample {

  private RetryingServiceWrapper retryingServiceWrapper;
  private Service wrappedService;


  @BeforeEach
  public void setUp() throws Exception {
    retryingServiceWrapper = new RetryingServiceWrapper();

    wrappedService = Mockito.mock(Service.class);
  }

  public void tearDown() throws Exception {

  }

  @Test
  public void testFailsAfterThreeAttempts() throws Exception {
    // Always throw an exception from the wrapped service
    doThrow(new ServiceException("Expected fail.")).when(wrappedService).doService(any(AdaptrisMessage.class));

    // now setup the retrying service wrapper;
    retryingServiceWrapper.setDelayBetweenRetries(new TimeInterval(1L, TimeUnit.SECONDS));
    retryingServiceWrapper.setNumRetries(3);
    retryingServiceWrapper.setService(wrappedService);

    LifecycleHelper.init(retryingServiceWrapper);
    LifecycleHelper.start(retryingServiceWrapper);

    try {
      retryingServiceWrapper.doService(DefaultMessageFactory.getDefaultInstance().newMessage());
      fail("Should have thrown an exception.");
    } catch (ServiceException ex) {
      // expected doService() 4 times, 1st try, then 3 retries.
      verify(wrappedService, times(4)).doService(any(AdaptrisMessage.class));
    }
  }

  @Test
  public void testLifecycleWrappedService() throws Exception {
    retryingServiceWrapper.setDelayBetweenRetries(new TimeInterval(1L, TimeUnit.SECONDS));
    retryingServiceWrapper.setNumRetries(3);
    retryingServiceWrapper.setService(wrappedService);

    LifecycleHelper.init(retryingServiceWrapper);
    assertNotNull(retryingServiceWrapper.wrappedServices());
    assertEquals(1, retryingServiceWrapper.wrappedServices().length);

    LifecycleHelper.start(retryingServiceWrapper);
    LifecycleHelper.stop(retryingServiceWrapper);
    LifecycleHelper.close(retryingServiceWrapper);

    verify(wrappedService, times(1)).requestInit();
    verify(wrappedService, times(1)).requestStart();
    verify(wrappedService, times(1)).requestStop();
    verify(wrappedService, times(1)).requestClose();
  }

  @Test
  public void testFailsOnceAndRestarts() throws Exception {
    // Fail once, then pass
    doThrow(new ServiceException("Expected fail.")).
    doNothing().when(wrappedService).doService(any(AdaptrisMessage.class));

    // now setup the retrying service wrapper;
    retryingServiceWrapper.setDelayBetweenRetries(new TimeInterval(1L, TimeUnit.SECONDS));
    retryingServiceWrapper.setNumRetries(3);
    retryingServiceWrapper.setService(wrappedService);
    retryingServiceWrapper.setRestartOnFailure(true);

    LifecycleHelper.init(retryingServiceWrapper);
    LifecycleHelper.start(retryingServiceWrapper);

    retryingServiceWrapper.doService(DefaultMessageFactory.getDefaultInstance().newMessage());
    verify(wrappedService, times(2)).doService(any(AdaptrisMessage.class));
    // init and start will be called once at RetryServiceWrapper init and start and then again on the restart.
    verify(wrappedService, times(2)).requestInit();
    verify(wrappedService, times(2)).requestStart();
  }

  @Test
  public void testFailsOnceNoRestarts() throws Exception {
    // Fail once, then pass
    doThrow(new ServiceException("Expected fail.")).
    doNothing().when(wrappedService).doService(any(AdaptrisMessage.class));

    // now setup the retrying service wrapper;
    retryingServiceWrapper.setDelayBetweenRetries(new TimeInterval(1L, TimeUnit.SECONDS));
    retryingServiceWrapper.setNumRetries(3);
    retryingServiceWrapper.setService(wrappedService);
    retryingServiceWrapper.setRestartOnFailure(false);

    LifecycleHelper.init(retryingServiceWrapper);
    LifecycleHelper.start(retryingServiceWrapper);

    retryingServiceWrapper.doService(DefaultMessageFactory.getDefaultInstance().newMessage());
    verify(wrappedService, times(2)).doService(any(AdaptrisMessage.class));
    // init and start will be called once at RetryServiceWrapper init and start and then again on the restart.
    verify(wrappedService, times(1)).requestInit();
    verify(wrappedService, times(1)).requestStart();
  }

  @Test
  public void testInfiniteAttemptsPassesElevethTime() throws Exception {
    // throw an exception first 10 times, then pass
    doThrow(new ServiceException("Expected fail.")).
    doThrow(new ServiceException("Expected fail.")).
    doThrow(new ServiceException("Expected fail.")).
    doThrow(new ServiceException("Expected fail.")).
    doThrow(new ServiceException("Expected fail.")).
    doThrow(new ServiceException("Expected fail.")).
    doThrow(new ServiceException("Expected fail.")).
    doThrow(new ServiceException("Expected fail.")).
    doThrow(new ServiceException("Expected fail.")).
    doThrow(new ServiceException("Expected fail.")).
    doNothing().when(wrappedService).doService(any(AdaptrisMessage.class));

    // now setup the retrying service wrapper;
    retryingServiceWrapper.setDelayBetweenRetries(new TimeInterval(1L, TimeUnit.MILLISECONDS));
    retryingServiceWrapper.setNumRetries(0); // infinite retries
    retryingServiceWrapper.setService(wrappedService);

    LifecycleHelper.init(retryingServiceWrapper);
    LifecycleHelper.start(retryingServiceWrapper);

    retryingServiceWrapper.doService(DefaultMessageFactory.getDefaultInstance().newMessage());
    verify(wrappedService, times(11)).doService(any(AdaptrisMessage.class));
  }

  @Test
  public void testInfiniteAttemptsExistsOnShutdown() throws Exception {
    // fail everytime.
    doThrow(new ServiceException("Expected fail.")).when(wrappedService).doService(any(AdaptrisMessage.class));

    // now setup the retrying service wrapper;
    retryingServiceWrapper.setDelayBetweenRetries(new TimeInterval(1L, TimeUnit.MILLISECONDS));
    retryingServiceWrapper.setNumRetries(0); // infinite retries
    retryingServiceWrapper.setService(wrappedService);

    LifecycleHelper.init(retryingServiceWrapper);
    LifecycleHelper.start(retryingServiceWrapper);

    // without a call to close(), this should run forever...
    new Thread("RetryingServiceWrapper thread") {
      @Override
      public void run() {
        try {
          retryingServiceWrapper.doService(DefaultMessageFactory.getDefaultInstance().newMessage());
        } catch (ServiceException e) {
        }
      }
    };

    Thread.sleep(1000);

    // mimic the adapter shutting down
    LifecycleHelper.stop(retryingServiceWrapper);
    // if this test ends, then we know we're good.
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JmsConnection connection = new JmsConnection();
    connection.setVendorImplementation(new StandardJndiImplementation("MyConnectionFactory"));

    StandaloneProducer wrappedService = new StandaloneProducer();
    wrappedService.setConnection(connection);
    wrappedService.setProducer(new PtpProducer().withQueue(("MyQueueName")));

    RetryingServiceWrapper service = new RetryingServiceWrapper();
    service.setUniqueId("Retrying Service Wrapper");
    service.setNumRetries(10);
    service.setRestartOnFailure(true);
    service.setDelayBetweenRetries(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setService(wrappedService);

    return service;
  }

  @Override
  protected boolean doStateTests() {
    return false;
  }

}
