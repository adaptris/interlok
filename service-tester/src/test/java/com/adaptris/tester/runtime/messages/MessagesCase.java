package com.adaptris.tester.runtime.messages;

import com.adaptris.tester.STExampleConfigCase;

import java.util.HashMap;
import java.util.Map;

public abstract class MessagesCase extends STExampleConfigCase {

  public static final String BASE_DIR_KEY = "MessagesCase.baseDir";
  protected static final String METADATA_KEY = "key";
  protected static final String METADATA_VALUE = "value";
  protected static final String PAYLOAD = "payload";
  protected Map<String, String> metadata;

  public MessagesCase(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }

  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    metadata = new HashMap<>();
    metadata.put(METADATA_KEY, METADATA_VALUE);
  }
}
