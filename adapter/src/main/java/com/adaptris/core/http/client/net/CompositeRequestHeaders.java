package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.client.RequestHeaderProvider;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Implementation of {@link RequestHeaderProvider} that uses nested providers to add headers to a {@link
 * HttpURLConnection}.
 * 
 * <p>This implementation is primarily so that you can mix and match both static and metadata driven headers; the order in which
 * you configure them determines what is actually present as headers.
 * </p>
 * @config http-composite-request-headers
 * 
 */
@XStreamAlias("http-composite-request-headers")
public class CompositeRequestHeaders implements RequestHeaderProvider<HttpURLConnection> {
  @XStreamImplicit
  @NotNull
  @AutoPopulated
  private List<RequestHeaderProvider<HttpURLConnection>> providers;

  public CompositeRequestHeaders() {
    providers = new ArrayList<RequestHeaderProvider<HttpURLConnection>>();
  }


  @Override
  public HttpURLConnection addHeaders(AdaptrisMessage msg, HttpURLConnection target) {
    HttpURLConnection http = target;
    for (RequestHeaderProvider<HttpURLConnection> h : getProviders()) {
      http = h.addHeaders(msg, http);
    }
    return http;
  }


  public List<RequestHeaderProvider<HttpURLConnection>> getProviders() {
    return providers;
  }

  public void setProviders(List<RequestHeaderProvider<HttpURLConnection>> handlers) {
    this.providers = Args.notNull(handlers, "Request Header Providers");
  }

  public void addProvider(RequestHeaderProvider<HttpURLConnection> handler) {
    getProviders().add(Args.notNull(handler, "Request Header Provider"));
  }
}
