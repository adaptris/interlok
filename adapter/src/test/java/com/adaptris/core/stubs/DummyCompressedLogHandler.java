package com.adaptris.core.stubs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.zip.GZIPOutputStream;

public class DummyCompressedLogHandler extends MockLogHandler {

  private byte[] compressedBytes;

  public DummyCompressedLogHandler() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream(out);
    PrintStream printer = new PrintStream(gzip);
    printer.print(LOG_EXTRACT);
    printer.flush();
    gzip.finish();
    printer.close();
    compressedBytes = out.toByteArray();
  }

  public boolean isCompressed() {
    return true;
  }

  public InputStream retrieveLog(LogFileType type) throws IOException {
    return new ByteArrayInputStream(compressedBytes);
  }
}
