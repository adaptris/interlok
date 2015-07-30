package com.adaptris.core.http;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterHandler} implementation stores HTTP headers as object metadata.
 * 
 * @config http-parameters-as-object-metadata
 * 
 */
@XStreamAlias("http-parameters-as-object-metadata")
public class ObjectMetadataParameterHandler implements ParameterHandler {

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    String prefix = itemPrefix == null ? "" : itemPrefix;
    
    for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
      String key = e.nextElement();
      String value = request.getParameter(key);
      message.addObjectMetadata(prefix + key, value);
    } 
  }

}
