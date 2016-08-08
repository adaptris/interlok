package com.adaptris.core.services.codec;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.ServiceCase;

public abstract class CodecServiceCase extends ServiceCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   *
   */
  public static final String BASE_DIR_KEY = "CodecServiceExamples.baseDir";

  static final String TEST_METADATA_KEY = "helloMetadataKey";
  static final String TEST_METADATA_KEY_2 = "worldMetadataKey";
  static final String TEST_METADATA_VALUE = "Hello";
  static final String TEST_METADATA_VALUE_2 = "World";
  static final String TEST_PAYLOAD = "The quick brown fox jumped over the lazy dog.";

  public CodecServiceCase(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  protected AdaptrisMessage createSimpleMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(TEST_PAYLOAD);
    msg.addMetadata(TEST_METADATA_KEY, TEST_METADATA_VALUE);
    msg.addMetadata(TEST_METADATA_KEY_2, TEST_METADATA_VALUE_2);
    return msg;
  }
  protected AdaptrisMessage createMimeMessage() throws Exception{
    AdaptrisMessage tempMessage = createSimpleMessage();
    MimeEncoder mimeEncoder = new MimeEncoder();
    byte[] payload = mimeEncoder.encode(tempMessage);
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
  }
}
