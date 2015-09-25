package com.adaptris.core.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterHandler} implementation that ignores HTTP parameters.
 * 
 * @config http-ignore-parameters
 * @deprecated since 3.0.6 use {@link com.adaptris.core.http.jetty.NoOpParameterHandler} instead.
 */
@XStreamAlias("http-ignore-parameters")
@Deprecated
public class NoOpParameterHandler extends com.adaptris.core.http.jetty.NoOpParameterHandler {


  private static transient boolean warningLogged;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public NoOpParameterHandler() {
    super();
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(),
          com.adaptris.core.http.jetty.NoOpParameterHandler.class.getName());
      warningLogged = true;
    }
  }
}
