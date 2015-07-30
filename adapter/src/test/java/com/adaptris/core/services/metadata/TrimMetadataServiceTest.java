package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class TrimMetadataServiceTest extends MetadataServiceExample {

  private static final String SOURCE_METADATA_KEY = "sourceMetadataKey";
  private static final String PADDED = "  ABCDEFG   ";
  private static final String TRIMMED = PADDED.trim();
  public TrimMetadataServiceTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  private static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(SOURCE_METADATA_KEY, PADDED);
    return msg;
  }

  public void testService() throws Exception {
    TrimMetadataService service = new TrimMetadataService(SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(SOURCE_METADATA_KEY));
    assertEquals(TRIMMED, msg.getMetadataValue(SOURCE_METADATA_KEY));
  }

  public void testServiceWithEmptyString() throws Exception {
    TrimMetadataService service = new TrimMetadataService(SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(SOURCE_METADATA_KEY, "");
    execute(service, msg);
    assertTrue(msg.containsKey(SOURCE_METADATA_KEY));
    assertEquals("", msg.getMetadataValue(SOURCE_METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new TrimMetadataService(".*matchingMetadataKeysWhichNeedToBeTrimmed.*");
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!-- This is used to trim metadata values of whitespace" + "\n-->\n";
  }
}
