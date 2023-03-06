/*
 * Copyright 2015 Adaptris Ltd.
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
 */
package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.http.HttpConstants;
import com.adaptris.core.http.auth.ResourceTargetMatcher;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Build an {@link HttpConstants#AUTHORIZATION} header from metadata.
 * 
 * @author gdries
 * @config http-metadata-authorization-header
 * 
 */
@JacksonXmlRootElement(localName = "http-metadata-authorization-header")
@XStreamAlias("http-metadata-authorization-header")
public class MetadataAuthorizationHeader implements HttpURLConnectionAuthenticator {

  @NotBlank
  private String metadataKey;
  
  private transient String headerValue;
  
  public MetadataAuthorizationHeader() {

  }

  @Override
  public void setup(String target, AdaptrisMessage msg, ResourceTargetMatcher auth) throws CoreException {
    headerValue = msg.getMetadataValue(getMetadataKey());
  }

  @Override
  public void configureConnection(HttpURLConnection conn) {
    if (!StringUtils.isBlank(headerValue)) {
      conn.addRequestProperty(HttpConstants.AUTHORIZATION, headerValue);
    }
  }

  @Override
  public void close() {
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * The metadata key to retrieve the value for the Authorization header from
   */
  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

}
