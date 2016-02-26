package com.adaptris.core.http.auth;

import java.net.HttpURLConnection;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.http.HttpConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("http-metadata-authorization-header")
public class MetadataAuthorizationHeader implements HttpAuthenticator {

  @NotBlank
  private String metadataKey;
  
  private transient String headerValue;
  
  @Override
  public void setup(String target, AdaptrisMessage msg) throws CoreException {
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
