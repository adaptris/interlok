package com.adaptris.core.http.jetty;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.ResponseHeaderProvider;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ResponseHeaderProvider} implementation that provides a static configured set of headers.
 * 
 * @config jetty-configured-response-headers
 * 
 */
@XStreamAlias("jetty-configured-response-headers")
public class ConfiguredResponseHeaderProvider implements ResponseHeaderProvider<HttpServletResponse> {

  @NotNull
  @Valid
  @AutoPopulated
  private KeyValuePairSet headers;

  public ConfiguredResponseHeaderProvider() {
    setHeaders(new KeyValuePairSet());
  }

  public ConfiguredResponseHeaderProvider(KeyValuePair... pairs) {
    this();
    for (KeyValuePair p : pairs) {
      getHeaders().add(p);
    }
  }

  @Override
  public HttpServletResponse addHeaders(AdaptrisMessage msg, HttpServletResponse target) {
    for (KeyValuePair k : getHeaders()) {
      target.addHeader(k.getKey(), k.getValue());
    }
    return target;
  }

  public KeyValuePairSet getHeaders() {
    return headers;
  }

  public void setHeaders(KeyValuePairSet headers) {
    this.headers = Args.notNull(headers, "headers");
  }
}
