/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
