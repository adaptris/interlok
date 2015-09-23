package com.adaptris.core.http;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

/**
 * Abstract {@link ParameterHandler} implementation that provides a prefix.
 * 
 * @author lchan
 *
 */
public abstract class ParameterHandlerImpl implements ParameterHandler {

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
