package com.adaptris.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class MultiPayloadMessageFactoryTest extends AdaptrisMessageFactoryImplCase {
  private final MultiPayloadMessageFactory messageFactory = new MultiPayloadMessageFactory();

  private static final String ID = "custom-message-id";
  private static final String ENCODING = "UTF-8";
  private static final String CONTENT = "Bacon ipsum dolor amet jowl boudin salami strip steak turkey.";
  private static final byte[] PAYLOAD = "Cupcake ipsum dolor sit amet fruitcake jelly-o tootsie roll.".getBytes(Charset.forName(ENCODING));

  private static final Set<MetadataElement> METADATA = new HashSet<>();

  @Before
  public void setUp() {
    messageFactory.setDefaultCharEncoding(ENCODING);
    METADATA.add(new MetadataElement("KEY", "VALUE"));
  }

  @Test
  public void testMessageFactoryEtAl() throws Exception {
    messageFactory.setDefaultPayloadId(ID);
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage();
    assertEquals(messageFactory.getDefaultPayloadId(), message.getCurrentPayloadId());
    assertEquals(messageFactory.getDefaultCharEncoding(), message.getContentEncoding());
    assertEquals(0, message.getSize());
    message.switchPayload("null-payload");
    message.setPayload(null);
    assertEquals(0, message.getPayload().length);
    message.setContent(null, ENCODING);
    assertEquals(0, message.getContent().length());
    message.addContent("bacon", "empty", ENCODING);
    message.addContent("bacon", CONTENT);
    assertEquals(CONTENT, message.getContent("bacon"));
    assertEquals(ENCODING, message.getContentEncoding("bacon"));
    AdaptrisMessage clone = (AdaptrisMessage) message.clone();
    assertTrue(clone.equivalentForTracking(message));
    message.setContentEncoding("US-ASCII");
    assertFalse(clone.equivalentForTracking(message));
    message.setPayload(null);
    assertFalse(clone.equivalentForTracking(message));
    message.setCurrentPayloadId("ham");
    assertFalse(clone.equivalentForTracking(message));
    message.deletePayload(message.getCurrentPayloadId());
    assertFalse(clone.equivalentForTracking(message));
    message.setMetadata(METADATA);
    assertFalse(clone.equivalentForTracking(message));
    assertFalse(message.equivalentForTracking(messageFactory.newMessage()));
    assertFalse(message.equivalentForTracking(DefaultMessageFactory.getDefaultInstance().newMessage()));
  }

  @Test
  public void testMessageFactoryPayload() {
    messageFactory.setDefaultPayloadId(null);
    messageFactory.setDefaultCharEncoding(null);
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(PAYLOAD);
    assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
    assertEquals(1, message.getPayloadCount());
    assertArrayEquals(PAYLOAD, message.getPayload());
    assertEquals(0, message.getMetadata().size());
  }

  @Test
  public void testMessageFactoryPayloadID() {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(ID, PAYLOAD);
    assertTrue(message.hasPayloadId(ID));
    assertEquals(ID, message.getCurrentPayloadId());
    assertEquals(1, message.getPayloadCount());
    assertArrayEquals(PAYLOAD, message.getPayload());
    assertEquals(0, message.getMetadata().size());
  }

  @Test
  public void testMessageFactoryPayloadMetadata() {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(PAYLOAD, METADATA);
    assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
    assertEquals(1, message.getPayloadCount());
    assertArrayEquals(PAYLOAD, message.getPayload());
    assertEquals(1, message.getMetadata().size());
  }

  @Test
  public void testMessageFactoryPayloadMetadataID() {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(ID, PAYLOAD, METADATA);
    assertEquals(ID, message.getCurrentPayloadId());
    assertEquals(1, message.getPayloadCount());
    assertArrayEquals(PAYLOAD, message.getPayload());
    assertEquals(1, message.getMetadata().size());
  }

  @Test
  public void testMessageFactoryContent() {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(CONTENT);
    assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
    assertEquals(1, message.getPayloadCount());
    assertEquals(CONTENT, message.getContent());
    assertEquals(0, message.getMetadata().size());
  }

  @Test
  public void testMessageFactoryContentEncoding() {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(CONTENT, ENCODING);
    assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
    assertEquals(1, message.getPayloadCount());
    assertEquals(CONTENT, message.getContent());
    assertEquals(ENCODING, message.getContentEncoding());
    assertEquals(0, message.getMetadata().size());
  }

  @Test
  public void testMessageFactoryContentEncodingMetadata() {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(CONTENT, ENCODING, METADATA);
    assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
    assertEquals(1, message.getPayloadCount());
    assertEquals(CONTENT, message.getContent());
    assertEquals(ENCODING, message.getContentEncoding());
    assertEquals(1, message.getMetadata().size());
  }

  @Test
  public void testMessageFactoryContentMetadata() {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(CONTENT, METADATA);
    assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, message.getCurrentPayloadId());
    assertEquals(1, message.getPayloadCount());
    assertEquals(CONTENT, message.getContent());
    assertEquals(messageFactory.getDefaultCharEncoding(), message.getContentEncoding());
    assertEquals(1, message.getMetadata().size());
  }

  @Test
  public void testMessageFactoryContentEncodingMetadataID() {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(ID, CONTENT, ENCODING, METADATA);
    assertEquals(ID, message.getCurrentPayloadId());
    assertEquals(1, message.getPayloadCount());
    assertEquals(CONTENT, message.getContent());
    assertEquals(ENCODING, message.getContentEncoding());
    assertEquals(1, message.getMetadata().size());
  }

  @Test
  public void testMessageFactoryCloneMessage() throws Exception {
    AdaptrisMessage singleMessage = DefaultMessageFactory.getDefaultInstance().newMessage(PAYLOAD, METADATA);
    MultiPayloadAdaptrisMessage multiMessage = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(singleMessage, null);
    assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, multiMessage.getCurrentPayloadId());
    assertEquals(1, multiMessage.getPayloadCount());
    assertEquals(0, multiMessage.getPayload().length);
    assertEquals(1, multiMessage.getMetadata().size());
  }

  @Test
  public void testMessageFactoryCloneMessageMetadata() throws Exception {
    AdaptrisMessage singleMessage = DefaultMessageFactory.getDefaultInstance().newMessage(PAYLOAD, METADATA);
    singleMessage.getMessageLifecycleEvent().addMleMarker(new MleMarker());
    List<String> keys = new ArrayList<>();
    keys.add("KEY");
    keys.add("UNKNOWN");
    messageFactory.setDefaultCharEncoding(null);
    MultiPayloadAdaptrisMessage multiMessage = (MultiPayloadAdaptrisMessage) messageFactory.newMessage(singleMessage, keys);
    assertEquals(MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID, multiMessage.getCurrentPayloadId());
    assertEquals(1, multiMessage.getPayloadCount());
    assertEquals(0, multiMessage.getPayload().length);
    assertEquals(1, multiMessage.getMetadata().size());
  }

  @Test
  public void testMessageStreams() throws Exception {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage((byte[])null);
    InputStream is = message.getInputStream();
    assertTrue(is.read() == -1);
    OutputStream os = message.getOutputStream();
    os.write(PAYLOAD);
    os.close();
    is = message.getInputStream();
    byte[] bytes = new byte[PAYLOAD.length];
    is.read(bytes);
    assertArrayEquals(PAYLOAD, bytes);
    message.setContentEncoding(null);
    try (Writer writer = message.getWriter()) {
      writer.write(CONTENT);
    }
    assertEquals(CONTENT, message.getContent());
  }

  @Test
  public void testOutputForLogging() throws Exception {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage("cupcake", PAYLOAD);
    message.addContent("bacon", CONTENT);
    String logged = message.getPayloadForLogging();
    assertTrue(message.getPayloadCount() == 2);
    assertTrue(logged.contains("bacon:" + CONTENT));
    assertTrue(logged.contains("cupcake"));
    assertFalse(logged.contains("cupcake:" + new String(PAYLOAD)));
  }

  @Test
  public void testResolve() {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage) messageFactory.newMessage("bacon", CONTENT, ENCODING, METADATA);
    message.addPayload("cake", PAYLOAD);
    message.switchPayload("bacon");
    assertEquals(CONTENT.length() + "," + CONTENT, message.resolve("%message{%size},%payload_id{bacon}", true));
    assertEquals(CONTENT.length() + "," + CONTENT, message.resolve("%message{%size},%payload_id{bacon}", false));
    assertEquals(CONTENT.length() + "," + CONTENT, message.resolve("%message{%size},%payload{id:bacon}", true));
    assertEquals(CONTENT.length() + "," + CONTENT, message.resolve("%message{%size},%payload{id:bacon}", false));
    assertEquals(new String(PAYLOAD), message.resolve("%payload_id{cake}"));
    assertNull(message.resolve(null));
    assertEquals("VALUE", message.resolve("%message{KEY}"));

    message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage("bacon", CONTENT, ENCODING);
    message.addContent("joke", "The other day I was listening to a song about superglue, it’s been stuck in my head ever since.");
    message.switchPayload("bacon");
    String s = message.resolve("Thank you for your message.\n\nHere's a random joke: %payload_id{joke}.\n\n-- \n\nSomething interesting...\n");

    try {
      message.resolve("%payload_id{fail}");
      fail();
    } catch (Exception e) {
      /* expected */
    }
  }

  @Override
  protected AdaptrisMessageFactory getMessageFactory() {
    return messageFactory;
  }
}
