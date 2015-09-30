package com.adaptris.core.http.jetty;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterHandler} implementation stores HTTP headers as object metadata.
 * 
 * @config jetty-http-parameters-as-object-metadata
 * 
 */
@XStreamAlias("jetty-http-parameters-as-object-metadata")
public class ObjectMetadataParameterHandler extends ParameterHandlerImpl {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  public ObjectMetadataParameterHandler() {
  }

  public ObjectMetadataParameterHandler(String prefix) {
    this();
    setParameterPrefix(prefix);
  }

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    String prefix = defaultIfEmpty(itemPrefix, "");
    
    for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
      String key = e.nextElement();
      String value = request.getParameter(key);
      String metadataKey = prefix + key;
      log.trace("Adding Object Metadata [{}: {}]", metadataKey, value);
      message.addObjectMetadata(metadataKey, value);
    } 
  }

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request) {
    handleParameters(message, request, parameterPrefix());
  }
}
