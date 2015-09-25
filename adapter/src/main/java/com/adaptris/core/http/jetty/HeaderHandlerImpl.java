package com.adaptris.core.http.jetty;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.http.server.HeaderHandler;

/**
 * Abstract {@link HeaderHandler} implementation that provides a prefix.
 * 
 * @author lchan
 *
 */
public abstract class HeaderHandlerImpl implements HeaderHandler<HttpServletRequest> {

  private String headerPrefix;

  public HeaderHandlerImpl() {

  }

  public String getHeaderPrefix() {
    return headerPrefix;
  }

  public void setHeaderPrefix(String headerPrefix) {
    this.headerPrefix = headerPrefix;
  }

  /**
   * Return the header prefix with null protection.
   * 
   * @return the prefix
   */
  protected String headerPrefix() {
    return defaultIfEmpty(getHeaderPrefix(), "");
  }
}
