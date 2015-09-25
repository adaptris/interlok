package com.adaptris.core.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link HeaderHandler} implementation stores HTTP headers as object metadata.
 * 
 * @config http-headers-as-object-metadata
 * @deprecated since 3.0.6 use {@link com.adaptris.core.http.jetty.ObjectMetadataHeaderHandler} instead.
 */
@XStreamAlias("http-headers-as-object-metadata")
@Deprecated
public class ObjectMetadataHeaderHandler extends com.adaptris.core.http.jetty.ObjectMetadataHeaderHandler {

  private static transient boolean warningLogged;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public ObjectMetadataHeaderHandler() {
    super();
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(),
          com.adaptris.core.http.jetty.ObjectMetadataHeaderHandler.class.getName());
      warningLogged = true;
    }
  }
}
