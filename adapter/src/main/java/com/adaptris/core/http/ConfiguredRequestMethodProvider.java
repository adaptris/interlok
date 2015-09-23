package com.adaptris.core.http;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Static implementation of {@link RequestMethodProvider}.
 *
 * @config http-configured-request-method
 * @author lchan
 *
 */
@XStreamAlias("http-configured-request-method")
public class ConfiguredRequestMethodProvider implements RequestMethodProvider {

  @NotNull
  @AutoPopulated
  private RequestMethod method;

  public ConfiguredRequestMethodProvider() {
    setMethod(RequestMethod.POST);
  }

  public ConfiguredRequestMethodProvider(RequestMethod p) {
    this();
    setMethod(p);
  }

  @Override
  public RequestMethod getMethod(AdaptrisMessage msg) {
    return getMethod();
  }

  public RequestMethod getMethod() {
    return method;
  }

  public void setMethod(RequestMethod method) {
    this.method = Args.notNull(method, "Method");
  }

}
