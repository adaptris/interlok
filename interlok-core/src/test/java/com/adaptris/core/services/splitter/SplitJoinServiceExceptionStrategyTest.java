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

package com.adaptris.core.services.splitter;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.aggregator.MimeAggregator;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.util.TimeInterval;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.concurrent.TimeUnit;

import static com.adaptris.core.ServiceCase.asCollection;
import static com.adaptris.core.ServiceCase.execute;
import static org.junit.Assert.fail;

@SuppressWarnings("deprecation")
public class SplitJoinServiceExceptionStrategyTest {

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  protected SplitJoinService createServiceForTests() {
    return new SplitJoinService();
  }

  @Test
  public void testService_WithMetadataExceptionStrategy_default() throws Exception {
    MetadataFlagPoolingFutureExceptionStrategy exceptionStrategy = new MetadataFlagPoolingFutureExceptionStrategy();
    exceptionStrategy.setMetadataFlagKey(MockExceptionStrategyService.SERVICE_RESULT);

    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = createServiceForTests();
    service.setExceptionStrategy(exceptionStrategy);

    // The service doesn't actually matter right now.
    service.setService(asCollection(new ThrowExceptionService(new ConfiguredException(testName.getMethodName()))));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    service.setAggregator(new MimeAggregator());
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {
    }
  }

  @Test
  public void testService_WithMetadataExceptionStrategy_handleSuccessWithExceptions() throws Exception {
    MetadataFlagPoolingFutureExceptionStrategy exceptionStrategy = new MetadataFlagPoolingFutureExceptionStrategy();
    exceptionStrategy.setMetadataFlagKey(MockExceptionStrategyService.SERVICE_RESULT);

    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = createServiceForTests();
    service.setExceptionStrategy(exceptionStrategy);

    // The service doesn't actually matter right now.
    service.setService(asCollection(new MockExceptionStrategyService(MockExceptionStrategyService.MODE.MIXED)));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    service.setAggregator(new MimeAggregator());
    try {
      execute(service, msg);
    }
    catch (ServiceException expected) {
      fail();
    }
  }

  @Test
  public void testService_WithMetadataExceptionStrategy_handleExceptions() throws Exception {
    MetadataFlagPoolingFutureExceptionStrategy exceptionStrategy = new MetadataFlagPoolingFutureExceptionStrategy();
    exceptionStrategy.setMetadataFlagKey(MockExceptionStrategyService.SERVICE_RESULT);

    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = createServiceForTests();
    service.setExceptionStrategy(exceptionStrategy);

    // The service doesn't actually matter right now.
    service.setService(asCollection(new MockExceptionStrategyService(MockExceptionStrategyService.MODE.ERROR)));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    service.setAggregator(new MimeAggregator());
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {
    }
  }

  @Test
  public void testService_WithMetadataExceptionStrategy_handleSuccess() throws Exception {
    MetadataFlagPoolingFutureExceptionStrategy exceptionStrategy = new MetadataFlagPoolingFutureExceptionStrategy();
    exceptionStrategy.setMetadataFlagKey(MockExceptionStrategyService.SERVICE_RESULT);

    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = createServiceForTests();
    service.setExceptionStrategy(exceptionStrategy);

    // The service doesn't actually matter right now.
    service.setService(asCollection(new MockExceptionStrategyService(MockExceptionStrategyService.MODE.SUCCESS)));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    service.setAggregator(new MimeAggregator());
    try {
      execute(service, msg);
    }
    catch (ServiceException expected) {
      fail();
    }
  }

  @Test
  public void testService_WithMetadataExceptionStrategy_handleNoSuccessNoException() throws Exception {
    MetadataFlagPoolingFutureExceptionStrategy exceptionStrategy = new MetadataFlagPoolingFutureExceptionStrategy();
    exceptionStrategy.setMetadataFlagKey(MockExceptionStrategyService.SERVICE_RESULT);

    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = createServiceForTests();
    service.setExceptionStrategy(exceptionStrategy);

    // The service doesn't actually matter right now.
    service.setService(asCollection(new MockExceptionStrategyService(MockExceptionStrategyService.MODE.NEUTRAL)));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    service.setAggregator(new MimeAggregator());
    try {
      execute(service, msg);
    }
    catch (ServiceException expected) {
      fail();
    }
  }
}
