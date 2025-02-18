package com.adaptris.core.services.transcoding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.MultiPayloadMessageFactory;
import com.adaptris.core.MultiPayloadMessageMimeEncoder;
import com.adaptris.core.stubs.MockEncoder;
import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class DecodingServiceTest extends TranscodingServiceCase {

  private static final String OVERRIDE_HEADER_VALUE = "value";

  @Test
  public void testInit() throws Exception {
    DecodingService service = new DecodingService();
    try {
      LifecycleHelper.init(service);
      fail();
    } catch (CoreException expected) {
    }
    service.setEncoder(new MockEncoder());
    LifecycleHelper.init(service);
    service = new DecodingService(new MockEncoder());
    LifecycleHelper.init(service);
  }

  @Test
  public void testSetEncoder() throws Exception {
    DecodingService s = new DecodingService();
    assertNull(s.getEncoder());
    MockEncoder me = new MockEncoder();
    s = new DecodingService(me);
    assertEquals(me, s.getEncoder());
    s = new DecodingService();
    s.setEncoder(me);
    assertEquals(me, s.getEncoder());
  }

  @Test
  public void testSetMessageFactory() throws Exception {
    DecodingService s = new DecodingService();
    assertNull(s.getMessageFactory());
    s = new DecodingService(new MockEncoder());
    assertNull(s.getMessageFactory());
    assertTrue(s.getEncoder().currentMessageFactory() instanceof DefaultMessageFactory);
    s = new DecodingService(new MockEncoder());
    AdaptrisMessageFactory amf = new StubMessageFactory();
    s.setMessageFactory(amf);
    try {
      LifecycleHelper.init(s);
      assertEquals(amf, s.getMessageFactory());
      assertTrue(s.getEncoder().currentMessageFactory() instanceof StubMessageFactory);
      assertEquals(amf, s.getEncoder().currentMessageFactory());
    } finally {
      LifecycleHelper.close(s);
    }
  }

  @Test
  public void testSetOverrideMetadata() throws Exception {

    DecodingService s = new DecodingService();

    assertNull(s.getOverrideMetadata());
    assertFalse(s.isOverrideMetadata());

    s.setOverrideMetadata(true);
    assertTrue(s.isOverrideMetadata());

    s.setOverrideMetadata(false);
    assertFalse(s.isOverrideMetadata());
  }

  @Test
  public void testMockEncoder() throws Exception {
    DecodingService service = new DecodingService(new MockEncoder());
    AdaptrisMessage msg = createSimpleMessage();
    execute(service, msg);
    assertEquals(TEST_PAYLOAD, new String(msg.getPayload()));
  }

  @Test
  public void testMimeEncoder() throws Exception {
    DecodingService service = new DecodingService(new MimeEncoder());
    AdaptrisMessage msg = createMimeMessage();
    assertFalse(msg.headersContainsKey(TEST_METADATA_KEY));
    assertFalse(msg.headersContainsKey(TEST_METADATA_KEY_2));
    execute(service, msg);
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY));
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY_2));
    assertEquals(TEST_METADATA_VALUE, msg.getMetadataValue(TEST_METADATA_KEY));
    assertEquals(TEST_METADATA_VALUE_2, msg.getMetadataValue(TEST_METADATA_KEY_2));
    assertEquals(TEST_PAYLOAD, new String(msg.getPayload()));
  }

  @Test
  public void testMimeEncoder_MultiPayload() throws Exception {
    DecodingService service = new DecodingService(new MultiPayloadMessageMimeEncoder());
    service.setMessageFactory(new MultiPayloadMessageFactory());

    String multiPayloadMime = "Message-ID: mesage-id\n"
        + "Mime-Version: 1.0\n"
        + "Content-Type: multipart/mixed; \n"
        + "    boundary=\"----=_Part_43_688598885.1729067201704\"\n"
        + "AdaptrisMessage/current-payload-id: default-payload\n"
        + "\n"
        + "------=_Part_43_688598885.1729067201704\n"
        + "Content-Id: AdaptrisMessage/payload/default-payload\n"
        + "\n"
        + "The quick brown fox jumped over the lazy dog.\n"
        + "------=_Part_43_688598885.1729067201704\n"
        + "Content-Id: AdaptrisMessage/payload/second-payload\n"
        + "\n"
        + "The quick brown fox jumped over the lazy dog 2.\n"
        + "------=_Part_43_688598885.1729067201704\n"
        + "Content-Id: AdaptrisMessage/metadata\n"
        + "\n"
        + "#\n"
        + "#Wed Oct 16 17:26:41 JST 2024\n"
        + "helloMetadataKey=Hello\n"
        + "worldMetadataKey=World\n"
        + "\n"
        + "------=_Part_43_688598885.1729067201704--\n";

    AdaptrisMessage msg = new MultiPayloadMessageFactory().newMessage(multiPayloadMime);
    execute(service, msg);
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY));
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY_2));
    assertEquals(TEST_METADATA_VALUE, msg.getMetadataValue(TEST_METADATA_KEY));
    assertEquals(TEST_METADATA_VALUE_2, msg.getMetadataValue(TEST_METADATA_KEY_2));
    assertEquals(TEST_PAYLOAD, new String(msg.getPayload()));
  }

  @Test
  public void testMimeEncoder_OverrideHeader() throws Exception {
    DecodingService service = new DecodingService(new MimeEncoder());
    service.setOverrideMetadata(true);
    AdaptrisMessage msg = createMimeMessage();
    msg.addMetadata(TEST_METADATA_KEY, OVERRIDE_HEADER_VALUE);
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY));
    assertEquals(OVERRIDE_HEADER_VALUE, msg.getMetadataValue(TEST_METADATA_KEY));
    execute(service, msg);
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY));
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY_2));
    assertEquals(TEST_METADATA_VALUE, msg.getMetadataValue(TEST_METADATA_KEY));
    assertEquals(TEST_METADATA_VALUE_2, msg.getMetadataValue(TEST_METADATA_KEY_2));
    assertEquals(TEST_PAYLOAD, new String(msg.getPayload()));
  }

  @Test
  public void testMimeEncoder_DoNotOverrideHeader() throws Exception {
    DecodingService service = new DecodingService(new MimeEncoder());
    service.setOverrideMetadata(false);
    AdaptrisMessage msg = createMimeMessage();
    msg.addMetadata(TEST_METADATA_KEY, OVERRIDE_HEADER_VALUE);
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY));
    assertEquals(OVERRIDE_HEADER_VALUE, msg.getMetadataValue(TEST_METADATA_KEY));
    execute(service, msg);
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY));
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY_2));
    assertEquals(OVERRIDE_HEADER_VALUE, msg.getMetadataValue(TEST_METADATA_KEY));
    assertEquals(TEST_METADATA_VALUE_2, msg.getMetadataValue(TEST_METADATA_KEY_2));
    assertEquals(TEST_PAYLOAD, new String(msg.getPayload()));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    DecodingService decodingService = new DecodingService();
    decodingService.setEncoder(new MimeEncoder());
    decodingService.setOverrideMetadata(false);
    return decodingService;
  }

}
