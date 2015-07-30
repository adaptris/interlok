package com.adaptris.core.http;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link HeaderHandler} implementation stores HTTP headers as object metadata.
 * 
 * @config http-headers-as-object-metadata
 * 
 */
@XStreamAlias("http-headers-as-object-metadata")
public class ObjectMetadataHeaderHandler implements HeaderHandler {

  @Override
  public void handleHeaders(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    String prefix = itemPrefix == null ? "" : itemPrefix;
    
    for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      String value = request.getHeader(key);
      message.addObjectMetadata(prefix + key, value);
    }
  }

}
