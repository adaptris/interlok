package com.adaptris.core.http;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterHandler} implementation that stores headers as standard metadata.
 * 
 * @config http-parameters-as-metadata
 * 
 */
@XStreamAlias("http-parameters-as-metadata")
public class MetadataParameterHandler implements ParameterHandler {

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    String prefix = itemPrefix == null ? "" : itemPrefix;
    
    for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
      String key = e.nextElement();
      String value = request.getParameter(key);
      message.addMetadata(prefix + key, value);
    } 
  }
}
