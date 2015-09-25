package com.adaptris.core.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link HeaderHandler} implementation that stores HTTP headers as standard metadata.
 * 
 * @config http-headers-as-metadata
 * @deprecated since 3.0.6 use {@link com.adaptris.core.http.jetty.MetadataHeaderHandler} instead.
 * 
 */
@XStreamAlias("http-headers-as-metadata")
@Deprecated
public class MetadataHeaderHandler extends com.adaptris.core.http.jetty.MetadataHeaderHandler {

  private static transient boolean warningLogged;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public MetadataHeaderHandler() {
    super();
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(),
          com.adaptris.core.http.jetty.MetadataHeaderHandler.class.getName());
      warningLogged = true;
    }
  }
}
