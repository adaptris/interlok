package com.adaptris.core.http.jetty;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link HeaderHandler} implementation stores HTTP headers as object metadata.
 * 
 * @config jetty-http-headers-as-object-metadata
 * 
 */
@XStreamAlias("jetty-http-headers-as-object-metadata")
public class ObjectMetadataHeaderHandler extends HeaderHandlerImpl {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  public ObjectMetadataHeaderHandler() {

  }

  public ObjectMetadataHeaderHandler(String prefix) {
    this();
    setHeaderPrefix(prefix);
  }


  @Override
  public void handleHeaders(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    String prefix = defaultIfEmpty(itemPrefix, "");
    
    for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      String value = request.getHeader(key);
      String metadataKey = prefix + key;
      log.trace("Adding Header {}:{}", metadataKey, value);
      message.addObjectMetadata(metadataKey, value);
    }
  }


  @Override
  public void handleHeaders(AdaptrisMessage msg, HttpServletRequest request) {
    handleHeaders(msg, request, headerPrefix());
  }
}
