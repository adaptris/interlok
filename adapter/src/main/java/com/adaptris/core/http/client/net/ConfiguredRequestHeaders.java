package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;
import java.net.URLConnection;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.client.RequestHeaderProvider;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link RequestHeaderHandler} that applies static configured values as headers.
 * 
 * @config http-configured-request-headers
 * 
 */
@XStreamAlias("http-configured-request-headers")
public class ConfiguredRequestHeaders implements RequestHeaderProvider<HttpURLConnection> {
  @NotNull
  @AutoPopulated
  private KeyValuePairSet headers;

  public ConfiguredRequestHeaders() {
    headers = new KeyValuePairSet();
  }


  @Override
  public HttpURLConnection addHeaders(AdaptrisMessage msg, HttpURLConnection target) {
    for (KeyValuePair k : getHeaders()) {
      target.addRequestProperty(k.getKey(), k.getValue());
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
