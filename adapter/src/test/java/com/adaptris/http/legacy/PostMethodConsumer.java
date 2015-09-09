package com.adaptris.http.legacy;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * This is the standard class that receives documents via HTTP.
 * <p>
 * <strong>Requires an HTTP license</strong>
 * </p>
 ** <p>
 * In the adapter configuration file this class is aliased as <b>post-method-consumer</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author lchan
 * @author $Author: lchan $
 * @deprecated since 2.9.0 use {@link com.adaptris.core.http.jetty.HttpsConnection} instead
 */
@Deprecated
@XStreamAlias("post-method-consumer")
public class PostMethodConsumer extends GenericConsumer {

  /** @see com.adaptris.http.legacy.GenericConsumer#getMethod() */
  @Override
  protected String getMethod() {
    return "POST";
  }
}
