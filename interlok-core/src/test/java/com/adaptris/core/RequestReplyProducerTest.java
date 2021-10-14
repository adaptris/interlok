package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.stubs.MockRequestReplyProducer;
import com.adaptris.core.util.LifecycleHelper;

public class RequestReplyProducerTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testProduce() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
    MockRequestReplyProducer mock = createAndStart();
    try {
      mock.produce(msg);
      assertEquals(1, mock.getProducedMessages().size());
    } finally {
      LifecycleHelper.stopAndClose(mock);
    }
  }

  @Test
  public void testProduce_AdaptrisMessage_ProduceDestination() throws Exception {
    MockRequestReplyProducer mock = createAndStart();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
      mock.produce(msg);
      assertEquals(1, mock.getProducedMessages().size());
    } finally {
      LifecycleHelper.stopAndClose(mock);
    }
  }

  @Test
  public void testRequest_AdaptrisMessage() throws Exception {
    MockRequestReplyProducer mock = createAndStart();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
      AdaptrisMessage reply = mock.request(msg);
      assertSame(msg, reply);
      assertTrue(msg.headersContainsKey(MockRequestReplyProducer.REPLY_METADATA_KEY));
    } finally {
      LifecycleHelper.stopAndClose(mock);
    }
  }

  @Test
  public void testRequest_AdaptrisMessage_IgnoreReply() throws Exception {
    MockRequestReplyProducer mock = createAndStart();
    mock.setIgnoreReplyMetadata(true);
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
      AdaptrisMessage reply = mock.request(msg);
      assertSame(msg, reply);
      assertFalse(msg.headersContainsKey(MockRequestReplyProducer.REPLY_METADATA_KEY));
    } finally {
      LifecycleHelper.stopAndClose(mock);
    }
  }

  @Test
  public void testRequest_AdaptrisMessage_Long() throws Exception {
    MockRequestReplyProducer mock = createAndStart();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
      AdaptrisMessage reply = mock.request(msg, 10L);
      assertSame(msg, reply);
      assertTrue(msg.headersContainsKey(MockRequestReplyProducer.REPLY_METADATA_KEY));
    } finally {
      LifecycleHelper.stopAndClose(mock);
    }
  }

  @Test
  public void testRequest_AdaptrisMessage_ProduceDestination() throws Exception {
    MockRequestReplyProducer mock = createAndStart();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
      AdaptrisMessage reply = mock.request(msg);
      assertSame(msg, reply);
      assertTrue(msg.headersContainsKey(MockRequestReplyProducer.REPLY_METADATA_KEY));
    } finally {
      LifecycleHelper.stopAndClose(mock);
    }
  }

  @Test
  public void testRequest_AdaptrisMessage_ProduceDestination_Long() throws Exception {
    MockRequestReplyProducer mock = createAndStart();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("dummy");
      AdaptrisMessage reply = mock.request(msg, 10L);
      assertSame(msg, reply);
      assertTrue(msg.headersContainsKey(MockRequestReplyProducer.REPLY_METADATA_KEY));
    } finally {
      LifecycleHelper.stopAndClose(mock);
    }
  }

  private MockRequestReplyProducer createAndStart() throws Exception {
    return LifecycleHelper.initAndStart(new MockRequestReplyProducer());
  }
}
