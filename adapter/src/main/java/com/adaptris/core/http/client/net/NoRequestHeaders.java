package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.client.RequestHeaderProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link RequestHeaderHandler} that adds no additional headers
 * 
 * @config apache-http-no-request-headers
 * @author lchan
 * 
 */
@XStreamAlias("http-no-request-headers")
public class NoRequestHeaders implements RequestHeaderProvider<HttpURLConnection> {


  @Override
  public HttpURLConnection addHeaders(AdaptrisMessage msg, HttpURLConnection target) {
    return target;
  }

}
