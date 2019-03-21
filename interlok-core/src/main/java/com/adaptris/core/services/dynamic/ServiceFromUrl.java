/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.services.dynamic;

import java.io.InputStream;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.URLHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extract the service to execute based on the configured URL.
 * 
 * @config dynamic-service-from-url
 *
 */
@XStreamAlias("dynamic-service-from-url")
@ComponentProfile(summary = "Extract the service to execute from a URL (file/http etc)")
public class ServiceFromUrl implements ServiceExtractor {

  @NotBlank
  @InputFieldHint(expression = true)
  private String url;

  public ServiceFromUrl() {

  }

  public ServiceFromUrl(String url) {
    this();
    setUrl(url);
  }

  @Override
  public InputStream getInputStream(AdaptrisMessage m) throws Exception {
    String urlToConnectTo = m.resolve(getUrl());
    return URLHelper.connect(urlToConnectTo);
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
