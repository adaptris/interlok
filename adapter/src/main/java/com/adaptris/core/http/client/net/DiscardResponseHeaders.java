package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.client.ResponseHeaderHandler;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ResponseHeaderHandler} implementation that discards the headers from the HTTP response.
 * 
 * @author lchan
 * @config http-discard-response-headers
 */
@XStreamAlias("http-discard-response-headers")
public class DiscardResponseHeaders implements ResponseHeaderHandler<HttpURLConnection> {

  @Override
  public AdaptrisMessage handle(HttpURLConnection headers, AdaptrisMessage msg) {
    return msg;
  }

}
