package com.adaptris.core.common;

import java.io.InputStream;

public class InputStreamWithEncoding {
  public final InputStream inputStream;
  public final String encoding;
  
  public InputStreamWithEncoding(InputStream inputStream, String encoding) {
    this.inputStream = inputStream;
    this.encoding = encoding;
  }
  
}
