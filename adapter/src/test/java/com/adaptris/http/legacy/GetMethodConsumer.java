package com.adaptris.http.legacy;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>GenericConsumer</code> which handles GETs. <code>handleRequest</code> method is identical to
 * <code>PostMethodConsumer</code>, needs to be refactored.
 * </p>
 * <p>
 * <strong>Requires an HTTP license</strong>
 * </p>
 * 
 * @deprecated since 2.9.0 use {@link com.adaptris.core.http.jetty.MessageConsumer} instead
 * <p>
 * In the adapter configuration file this class is aliased as <b>http-get-method-consumer</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
@Deprecated
@XStreamAlias("http-get-method-consumer")
public class GetMethodConsumer extends GenericConsumer {

  /** @see com.adaptris.http.legacy.GenericConsumer#getMethod() */
  @Override
  protected String getMethod() {
    return "GET";
  }
}
