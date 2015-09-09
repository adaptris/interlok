package com.adaptris.http.legacy;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @deprecated use {@link SimpleHttpProducer} instead (since 2.6.1)
 * <p>
 * In the adapter configuration file this class is aliased as <b>post-method-producer</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
@Deprecated
@XStreamAlias("post-method-producer")
public class PostMethodProducer extends SimpleHttpProducer {

  public PostMethodProducer() {
    super();
    log.warn(this.getClass().getCanonicalName() + " is deprecated, use "
        + SimpleHttpProducer.class.getCanonicalName() + " instead");
    setMethod("POST");
  }
}
