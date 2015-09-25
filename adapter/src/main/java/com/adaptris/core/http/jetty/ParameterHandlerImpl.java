package com.adaptris.core.http.jetty;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.http.server.ParameterHandler;

/**
 * Abstract {@link ParameterHandler} implementation that provides a prefix.
 * 
 * @author lchan
 *
 */
public abstract class ParameterHandlerImpl implements ParameterHandler<HttpServletRequest> {

  private String parameterPrefix;

  public ParameterHandlerImpl() {

  }

  public String getParameterPrefix() {
    return parameterPrefix;
  }

  public void setParameterPrefix(String headerPrefix) {
    this.parameterPrefix = headerPrefix;
  }

  /**
   * Return the parameter prefix with null protection.
   * 
   * @return the prefix
   */
  protected String parameterPrefix() {
    return defaultIfEmpty(getParameterPrefix(), "");
  }
}
