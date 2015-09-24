package com.adaptris.core.http;

import static org.apache.commons.lang.StringUtils.isBlank;

public abstract class ContentTypeProviderImpl implements ContentTypeProvider {


  protected String build(String mimeType, String charset) {
    StringBuilder buf = new StringBuilder();
    buf.append(mimeType);
    if (!isBlank(charset)) {
      buf.append("; charset=");
      buf.append(charset);
    }
    return buf.toString();
  }
}
