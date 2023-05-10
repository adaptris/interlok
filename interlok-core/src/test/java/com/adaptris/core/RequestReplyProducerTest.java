package com.adaptris.core;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.stubs.MockRequestReplyProducer;
import com.adaptris.core.util.LifecycleHelper;

public class RequestReplyProducerTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
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
