package com.adaptris.validation.constraints;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlExpressionValidator extends ExpressionValidator<UrlExpression> {

  private String protocol;
  private String host;
  private int port;

  @Override
  public void initialize(UrlExpression constraintAnnotation) {
    setPattern(constraintAnnotation.pattern());
    protocol = constraintAnnotation.protocol();
    host = constraintAnnotation.host();
    port = constraintAnnotation.port();
  }

  @Override
  public boolean onIsValid(String value) {
    return isUrl(value);
  }

  private boolean isUrl(String value) {
    if (value.length() == 0) {
      return true;
    }

    URL url;
    try {
      url = new URL(value.toString());
    } catch (MalformedURLException e) {
      return false;
    }

    if (protocol != null && protocol.length() > 0 && !url.getProtocol().equals(protocol)) {
      return false;
    }

    if (host != null && host.length() > 0 && !url.getHost().equals(host)) {
      return false;
    }

    if (port != -1 && url.getPort() != port) {
      return false;
    }

    return true;
  }

}
