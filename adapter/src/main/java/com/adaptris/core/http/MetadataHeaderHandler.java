package com.adaptris.core.http;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link HeaderHandler} implementation that stores HTTP headers as standard metadata.
 * 
 * @config http-headers-as-metadata
 * 
 */
@XStreamAlias("http-headers-as-metadata")
public class MetadataHeaderHandler implements HeaderHandler {

  @Override
  public void handleHeaders(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    String prefix = itemPrefix == null ? "" : itemPrefix;

    for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      String value = request.getHeader(key);
      message.addMetadata(prefix + key, value);
    }
  }
}
