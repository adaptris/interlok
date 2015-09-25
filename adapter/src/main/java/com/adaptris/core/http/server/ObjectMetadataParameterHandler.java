package com.adaptris.core.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterHandler} implementation stores HTTP headers as object metadata.
 * 
 * @config http-parameters-as-object-metadata
 * @deprecated since 3.0.6 use {@link com.adaptris.core.http.jetty.ObjectMetadataParameterHandler} instead.
 */
@XStreamAlias("http-parameters-as-object-metadata")
@Deprecated
public class ObjectMetadataParameterHandler extends com.adaptris.core.http.jetty.ObjectMetadataParameterHandler {

  private static transient boolean warningLogged;
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  public ObjectMetadataParameterHandler() {
    super();
    if (!warningLogged) {
      log.warn("[{}] is deprecated, use [{}] instead", this.getClass().getSimpleName(),
          com.adaptris.core.http.jetty.ObjectMetadataParameterHandler.class.getName());
      warningLogged = true;
    }
  }
}
