package com.adaptris.core.stubs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DummyLogHandler extends MockLogHandler {
  public boolean isCompressed() {
    return false;
  }

  public InputStream retrieveLog(LogFileType type) throws IOException {
    return new ByteArrayInputStream(LOG_EXTRACT.getBytes());
  }
}
