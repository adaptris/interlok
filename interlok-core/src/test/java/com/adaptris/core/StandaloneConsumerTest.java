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

import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class StandaloneConsumerTest extends BaseCase {

  public StandaloneConsumerTest(String arg0) {
    super(arg0);
  }

  public void testSetAdaptrisMessageListener() throws Exception {
    MockMessageProducer prod = new MockMessageProducer();

    StandardWorkflow aml1 = new StandardWorkflow();
    aml1.setConsumer(new MockMessageConsumer());
    aml1.setProducer(prod);

    Channel channel = new MockChannel();
    channel.getWorkflowList().add(aml1);
    channel.prepare();

    LifecycleHelper.init(aml1);
    LifecycleHelper.start(aml1);


    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("xxx");

    aml1.onAdaptrisMessage(msg);

    // test the WF directly...
    assertTrue(prod.getMessages().size() == 1);

    MockMessageConsumer consumer1 = new MockMessageConsumer();

    StandaloneConsumer sc = new StandaloneConsumer();
    sc.setConsumer(consumer1);
    sc.registerAdaptrisMessageListener(aml1);
    LifecycleHelper.init(sc);

    consumer1.submitMessage(msg);

    // basic test SC...
    assertTrue(((MockMessageProducer) aml1.getProducer()).getMessages()
        .size() == 2);
    stop(sc);
    stop(channel);
  }

  public void testPollingConsumerLifecycle() throws Exception {
    AdaptrisPollingConsumer consumer1 = new AdaptrisPollingConsumer() {

      @Override
      public void prepareConsumer() throws CoreException {
      }

      @Override
      protected int processMessages() {
        return 0;
      }
    };
    consumer1.setDestination(new ConfiguredConsumeDestination());
    StandaloneConsumer sc = new StandaloneConsumer(consumer1);
    sc.registerAdaptrisMessageListener(new MockMessageListener());
    LifecycleHelper.initAndStart(sc);
    LifecycleHelper.stopAndClose(sc);
  }

  public void testPollingConsumerContinueProcessing() throws Exception {
    AdaptrisPollingConsumer consumer = new AdaptrisPollingConsumer() {

      @Override
      public void prepareConsumer() throws CoreException {
      }

      @Override
      protected int processMessages() {
        return 0;
      }
    };
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(1L, TimeUnit.DAYS)));
    consumer.setMaxMessagesPerPoll(10);
    consumer.setReacquireLockBetweenMessages(false);
    StandaloneConsumer sc = new StandaloneConsumer(consumer);
    sc.registerAdaptrisMessageListener(new MockMessageListener());
    // Not started, so should always return false.
    assertFalse(consumer.continueProcessingMessages(1));
    LifecycleHelper.initAndStart(sc);
    assertTrue(consumer.continueProcessingMessages(5));
    assertFalse(consumer.continueProcessingMessages(11));
    assertFalse(consumer.continueProcessingMessages(10));
    LifecycleHelper.stopAndClose(sc);

    consumer.setReacquireLockBetweenMessages(true);
    assertFalse(consumer.continueProcessingMessages(1));
    LifecycleHelper.initAndStart(sc);
    assertTrue(consumer.continueProcessingMessages(5));
    assertFalse(consumer.continueProcessingMessages(11));
    assertFalse(consumer.continueProcessingMessages(10));
    LifecycleHelper.stopAndClose(sc);

  }

  public void testXmlRoundTrip() throws Exception {
    StandaloneConsumer input = new StandaloneConsumer();
    // input.setEncoder(new MimeEncoder());
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String xml = m.marshal(input);
    log.trace(xml);
    StandaloneConsumer output = (StandaloneConsumer) m.unmarshal(xml);
    assertRoundtripEquality(input, output);
  }

  public void testSetConsumer() throws Exception {
    NullMessageConsumer c = new NullMessageConsumer();
    c.setUniqueId("abc");
    StandaloneConsumer sc = new StandaloneConsumer(c);
    assertEquals(c, sc.getConsumer());
    try {
      sc.setConsumer(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(c, sc.getConsumer());
  }

  public void testSetConnection() throws Exception {
    NullConnection c = new NullConnection();
    StandaloneConsumer sc = new StandaloneConsumer(c);
    assertEquals(c, sc.getConnection());
    try {
      sc.setConnection(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(c, sc.getConnection());
  }

  public void testMessageEventGeneratorQualifier() throws Exception {
    NullMessageConsumer c = new NullMessageConsumer();
    c.setUniqueId("abc");
    StandaloneConsumer sc = new StandaloneConsumer(c);
    assertEquals("abc", sc.createQualifier());
    c.setUniqueId("");
    assertEquals("", sc.createQualifier());
  }

  public void testMessageEventGeneratorCreateName() throws Exception {
    NullMessageConsumer c = new NullMessageConsumer();
    StandaloneConsumer sc = new StandaloneConsumer(c);
    assertEquals(NullMessageConsumer.class.getName(), sc.createName());
    assertEquals(sc.getConsumer().createName(), sc.createName());
  }

  public void testBackReferences() throws Exception {
    StandaloneConsumer consumer = new StandaloneConsumer();
    NullConnection conn = new NullConnection();
    consumer.setConnection(conn);
    assertEquals(conn, consumer.getConnection());
    // redmineID #4468 - need to initialise consumer first before backrefs are ready.
    LifecycleHelper.init(consumer);
    assertEquals(1, conn.retrieveExceptionListeners().size());
    assertTrue(consumer == conn.retrieveExceptionListeners().toArray()[0]);

    // Now marshall and see if it's the same.
    XStreamMarshaller m = new XStreamMarshaller();
    String xml = m.marshal(consumer);
    StandaloneConsumer consumer2 = (StandaloneConsumer) m.unmarshal(xml);
    // If the setter has been used, then these two will be "true"
    assertNotNull(consumer2.getConnection());
    LifecycleHelper.init(consumer2);

    assertEquals(1, consumer2.getConnection().retrieveExceptionListeners().size());
    assertTrue(consumer2 == consumer2.getConnection().retrieveExceptionListeners().toArray()[0]);
  }

}
