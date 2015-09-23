package com.adaptris.core.http;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

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
public class MetadataParameterHandler extends ParameterHandlerImpl {


  public MetadataParameterHandler() {
  }

  public MetadataParameterHandler(String prefix) {
    this();
    setParameterPrefix(prefix);
  }


  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    String prefix = defaultIfEmpty(itemPrefix, "");
    
    for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
      String key = e.nextElement();
      String value = request.getParameter(key);
      message.addMetadata(prefix + key, value);
    } 
  }

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request) {
    handleParameters(message, request, parameterPrefix());
  }
}
