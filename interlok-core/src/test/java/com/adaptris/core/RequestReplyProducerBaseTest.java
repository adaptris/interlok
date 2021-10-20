package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.core.util.LifecycleHelper;

public class RequestReplyProducerBaseTest extends RequestReplyProducerBase {

  @Before
  public void setUp() throws Exception {
    LifecycleHelper.initAndStart(this);
  }

  @After
  public void teardown() throws Exception {
    LifecycleHelper.stopAndClose(this);
  }

  @Test
  public void testCreateName() {
    assertEquals(RequestReplyProducerBaseTest.class.getName(), createName());
  }

  @Test
  public void testCopyReplyContents() throws Exception {
    AdaptrisMessage reply = new DefaultMessageFactory().newMessage("hello world");
    AdaptrisMessage original = new DefaultMessageFactory().newMessage();
    copyReplyContents(reply, original);
    assertEquals("hello world", original.getContent());
  }

  @Test(expected = ProduceException.class)
  public void testCopyReplyContents_Failure() throws Exception {
    AdaptrisMessage reply = new DefectiveMessageFactory(WhenToBreak.BOTH).newMessage();
    AdaptrisMessage original = new DefaultMessageFactory().newMessage();
    copyReplyContents(reply, original);
  }

  @Test
  public void testCopyReplyContents_BothFileBacked() throws Exception {
    AdaptrisMessage reply = new FileBackedMessageFactory().newMessage("hello world");
    AdaptrisMessage original = new FileBackedMessageFactory().newMessage();
    copyReplyContents(reply, original);
    assertEquals("hello world", original.getContent());
    assertTrue(original.getObjectHeaders().containsKey(reply.getUniqueId()));
  }

  @Test
  public void testCopyReplyContents_OneFileBacked() throws Exception {
    AdaptrisMessage reply = new FileBackedMessageFactory().newMessage("hello world");
    AdaptrisMessage original = new DefaultMessageFactory().newMessage();
    copyReplyContents(reply, original);
    assertEquals("hello world", original.getContent());
    assertFalse(original.getObjectHeaders().containsKey(reply.getUniqueId()));
  }

  @Test
  public void testMergeReply() throws Exception {
    AdaptrisMessage reply = new DefaultMessageFactory().newMessage("hello world", StandardCharsets.UTF_8.name());
    reply.addMessageHeader("hello", "world");
    reply.addObjectHeader("goodbye", "cruel world");
    AdaptrisMessage original = new DefaultMessageFactory().newMessage();
    AdaptrisMessage msg = mergeReply(reply,  original);
    assertSame(original, msg);
    assertEquals("hello world", msg.getContent());
    assertEquals(StandardCharsets.UTF_8.name(), msg.getContentEncoding());
    assertTrue(msg.headersContainsKey("hello"));
    assertTrue(msg.getObjectHeaders().containsKey("goodbye"));
  }

  @Test
  public void testMergeReply_Same() throws Exception {
    AdaptrisMessage reply =
        new DefaultMessageFactory().newMessage("hello world", StandardCharsets.UTF_8.name());
    reply.addMessageHeader("hello", "world");
    reply.addObjectHeader("goodbye", "cruel world");
    AdaptrisMessage msg = mergeReply(reply, reply);
    assertSame(reply, msg);
  }

  @Test
  public void testMergeReply_IgnoreReplyMetadata() throws Exception {
    AdaptrisMessage reply = new FileBackedMessageFactory().newMessage("hello world");
    AdaptrisMessage original = new FileBackedMessageFactory().newMessage();
    reply.addMessageHeader("hello", "world");
    reply.addObjectHeader("goodbye", "cruel world");
    setIgnoreReplyMetadata(true);
    AdaptrisMessage msg = mergeReply(reply, original);

    assertSame(original, msg);
    assertEquals("hello world", msg.getContent());
    assertNull(msg.getContentEncoding());
    assertFalse(msg.headersContainsKey("hello"));
    assertFalse(msg.getObjectHeaders().containsKey("goodbye"));
    // The reply should be "in-scope" for GC avoidance.
    assertTrue(msg.getObjectHeaders().containsKey(reply.getUniqueId()));
  }


  @Override
  public AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException {
    return null;
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException {
    return request(msg);
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return null;
  }

  @Override
  protected long defaultTimeout() {
    return 0;
  }

}
