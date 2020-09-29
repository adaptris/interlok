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

import static com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase.asCollection;
import static com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase.execute;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultEventHandler;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.WaitService;
import com.adaptris.core.services.aggregator.AppendingMessageAggregator;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.TimeInterval;

public class FixedSplitJoinServiceTest {

  @Test
  public void testService() throws Exception {
    FixedSplitJoinService service = new FixedSplitJoinService();
    service.setAggregator(new AppendingMessageAggregator());
    service.setSplitter(new LineCountSplitter(1));
    service.setPoolsize(10);
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setService(new WaitService(new TimeInterval(100L, TimeUnit.MILLISECONDS), true));

    AdaptrisMessage msg = createLineCountMessageInput(50);
    ExampleServiceCase.execute(service, msg);

    List<String> result = IOUtils.readLines(msg.getReader());
    // Using an appending message aggregator just means that we double up on the original message...
    assertEquals(50 * 2, result.size());
  }

  @Test
  public void testService_Events() throws Exception {
    FixedSplitJoinService service = new FixedSplitJoinService();
    MockMessageProducer events = new MockMessageProducer();
    service.setAggregator(new AppendingMessageAggregator());
    service.setSplitter(new LineCountSplitter(1));
    service.setPoolsize(10);
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setService(new WaitService(new TimeInterval(100L, TimeUnit.MILLISECONDS), true));
    service.setSendEvents(true);
    service.registerEventHandler(LifecycleHelper.initAndStart(new DefaultEventHandler(events)));
    AdaptrisMessage msg = createLineCountMessageInput(50);
    ExampleServiceCase.execute(service, msg);

    List<String> result = IOUtils.readLines(msg.getReader());
    // Using an appending message aggregator just means that we double up on the original message...
    assertEquals(50 * 2, result.size());
    // Check we got 50 events.
    assertEquals(50, events.getMessages().size());
  }

  @Test(expected = ServiceException.class)
  public void testTimeout() throws Exception {
    FixedSplitJoinService service = new FixedSplitJoinService();
    service.setAggregator(new AppendingMessageAggregator());
    service.setSplitter(new LineCountSplitter(1));
    service.setPoolsize(10);
    service.setTimeout(new TimeInterval(50L, TimeUnit.MILLISECONDS));
    service.setService(new WaitService(new TimeInterval(100L, TimeUnit.MILLISECONDS), false));
    service.setSendEvents(true);

    AdaptrisMessage msg = createLineCountMessageInput(50);
    try {
      ExampleServiceCase.execute(service, msg);
    } catch (ServiceException e) {
      // This will be a RuntimeException (from hasNext()?) that wraps a TimeoutException
      // That's in turn wrapped by a ServiceException.
      assertNotNull(e.getCause().getCause());
      throw e;
    }
  }

  @Test(expected = ServiceException.class)
  public void testService_WithException() throws Exception {
    FixedSplitJoinService service = new FixedSplitJoinService();
    service.setAggregator(new AppendingMessageAggregator());
    service.setSplitter(new LineCountSplitter(1));
    service.setPoolsize(10);
    service.setTimeout(new TimeInterval(5L, TimeUnit.SECONDS));
    service.setService(new ThrowExceptionService(new ConfiguredException("always-fail")));
    service.registerEventHandler(LifecycleHelper.initAndStart(new DefaultEventHandler()));
    AdaptrisMessage msg = createLineCountMessageInput(50);
    try {
      ExampleServiceCase.execute(service, msg);
    } catch (ServiceException e) {
      assertEquals("always-fail", e.getMessage());
      throw e;
    }
  }

  @Test
  public void testService_DidWorkSuccessfully() throws Exception {
    FixedSplitJoinService service = new FixedSplitJoinService();
    service.setServiceErrorHandler(
        new NoExceptionIfWorkDone().withMetadataKey(NoExceptionIfWorkDone.DEFAULT_METADATA_KEY));
    service.setSplitter(new LineCountSplitter(1));
    service.setService(
        asCollection(new MockExceptionStrategyService(MockExceptionStrategyService.MODE.NEUTRAL)));
    service.setPoolsize(10);
    service.setTimeout(new TimeInterval(5L, TimeUnit.SECONDS));
    service.setAggregator(new AppendingMessageAggregator());
    service.registerEventHandler(LifecycleHelper.initAndStart(new DefaultEventHandler()));
    AdaptrisMessage msg = createLineCountMessageInput(50);
    execute(service, msg);
    List<String> result = IOUtils.readLines(msg.getReader());
    // Using an appending message aggregator just means that we double up on the original message...
    // Since we aren't doing any filtering... then we will always get 100 msgs.
    assertEquals(50 * 2, result.size());
  }

  @Test(expected = ServiceException.class)
  public void testService_DidNoWork() throws Exception {
    FixedSplitJoinService service = new FixedSplitJoinService();
    service.setServiceErrorHandler(new NoExceptionIfWorkDone().withMetadataKey(NoExceptionIfWorkDone.DEFAULT_METADATA_KEY));
    service.setSplitter(new LineCountSplitter(1));
    service.setService(asCollection(new MockExceptionStrategyService(MockExceptionStrategyService.MODE.ERROR)));
    service.setPoolsize(10);
    service.setTimeout(new TimeInterval(5L, TimeUnit.SECONDS));
    service.setAggregator(new AppendingMessageAggregator());
    service.registerEventHandler(LifecycleHelper.initAndStart(new DefaultEventHandler()));
    AdaptrisMessage msg = createLineCountMessageInput(50);
    execute(service, msg);
  }

  @Test
  public void testService_DidSomeWork() throws Exception {
    FixedSplitJoinService service = new FixedSplitJoinService();
    service.setServiceErrorHandler(
        new NoExceptionIfWorkDone().withMetadataKey(NoExceptionIfWorkDone.DEFAULT_METADATA_KEY));
    service.setSplitter(new LineCountSplitter(1));
    service.setService(
        asCollection(new MockExceptionStrategyService(MockExceptionStrategyService.MODE.MIXED)));
    service.setPoolsize(10);
    service.setTimeout(new TimeInterval(5L, TimeUnit.SECONDS));
    service.setAggregator(new AppendingMessageAggregator());
    service.registerEventHandler(LifecycleHelper.initAndStart(new DefaultEventHandler()));
    AdaptrisMessage msg = createLineCountMessageInput(50);
    execute(service, msg);
    List<String> result = IOUtils.readLines(msg.getReader());
    // Using an appending message aggregator just means that we double up on the original message...
    // Since we aren't doing any filtering... then we will always get 100 msgs.
    assertEquals(50 * 2, result.size());
  }

  @Test
  public void testService_IgnoreExceptions() throws Exception {
    FixedSplitJoinService service = new FixedSplitJoinService();
    service.setServiceErrorHandler(new IgnoreAllExceptions());
    service.setSplitter(new LineCountSplitter(1));
    service.setService(
        asCollection(new MockExceptionStrategyService(MockExceptionStrategyService.MODE.ERROR)));
    service.setPoolsize(10);
    service.setTimeout(new TimeInterval(5L, TimeUnit.SECONDS));
    service.setAggregator(new AppendingMessageAggregator());
    service.registerEventHandler(LifecycleHelper.initAndStart(new DefaultEventHandler()));
    AdaptrisMessage msg = createLineCountMessageInput(50);
    execute(service, msg);
    List<String> result = IOUtils.readLines(msg.getReader());
    // Using an appending message aggregator just means that we double up on the original message...
    // Since we aren't doing any filtering... then we will always get 100 msgs.
    assertEquals(50 * 2, result.size());
  }


  private static AdaptrisMessage createLineCountMessageInput(int lines) {
    StringWriter out = new StringWriter();
    try (PrintWriter print = new PrintWriter(out)) {
      for (int i = 0; i < lines; i++) {
        print.println("Pack my jug with a dozen liquor boxes.");
      }
    }
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(out.toString());
  }
}
