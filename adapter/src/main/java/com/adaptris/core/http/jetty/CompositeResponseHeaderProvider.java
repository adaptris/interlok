package com.adaptris.core.http.jetty;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.ResponseHeaderProvider;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ResponseHeaderProvider} implementation that uses a nested set of providers to private HTTP response headers.
 * 
 * @config jetty-composite-response-headers
 * 
 */
@XStreamAlias("jetty-composite-response-headers")
public class CompositeResponseHeaderProvider implements ResponseHeaderProvider<HttpServletResponse> {

  @AutoPopulated
  @NotNull
  @Valid
  private List<ResponseHeaderProvider<HttpServletResponse>> providers;


  public CompositeResponseHeaderProvider() {
    setProviders(new ArrayList<ResponseHeaderProvider<HttpServletResponse>>());
  }

  public CompositeResponseHeaderProvider(ResponseHeaderProvider<HttpServletResponse>... providers) {
    this();
    for (ResponseHeaderProvider<HttpServletResponse> p : providers) {
      addProvider(p);
    }
  }


  @Override
  public HttpServletResponse addHeaders(AdaptrisMessage msg, HttpServletResponse target) {
    HttpServletResponse response = target;
    for (ResponseHeaderProvider<HttpServletResponse> p : getProviders()) {
      response = p.addHeaders(msg, response);
    }
    return response;
  }


  public List<ResponseHeaderProvider<HttpServletResponse>> getProviders() {
    return providers;
  }


  public void setProviders(List<ResponseHeaderProvider<HttpServletResponse>> p) {
    this.providers = Args.notNull(p, "ResponseHeaderProviders");
  }

  public void addProvider(ResponseHeaderProvider<HttpServletResponse> p) {
    providers.add(Args.notNull(p, "ResponseHeaderProvider"));
  }

}
