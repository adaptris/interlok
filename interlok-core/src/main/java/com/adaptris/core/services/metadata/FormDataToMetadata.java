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

package com.adaptris.core.services.metadata;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.mail.internet.ContentType;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.http.HttpConstants;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.URLHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Takes a application/x-www-url-form-encoded payload and extracts it as metadata.
 * <p>
 * If the client is sending data to the adapter, it is possible that the client will send data as
 * though it were a standard Html Form post. In situations like that, then the payload is a number
 * of URLEncoded key value pairs. This service can be used to convert all the Request parameters
 * into metadata.
 * </p>
 *
 * <p>
 * {@code contentTypeKey} will be checked to see if the content-type is in fact
 * {@code application/x-www-url-form-encoded}; if it is, then the payload is parsed as into
 * metadata, note that the payload is always unchanged.
 * </p>
 * 
 * @config www-url-form-encoded-payload-to-metadata
 * 
 */
@XStreamAlias("www-url-form-encoded-payload-to-metadata")
@AdapterComponent
@ComponentProfile(summary = "Turn a application/www-url-form-encoded payload into metadata.",
    tag = "service,metadata,http,https", since = "3.9.0")
@DisplayOrder(order = {"contentTypeKey", "metadataPrefix"})
public class FormDataToMetadata extends ServiceImp {
  public static final String DEFAULT_CONTENT_TYPE_KEY = HttpConstants.CONTENT_TYPE;
  public static final String DEFAULT_CONTENT_TYPE_VALUE = HttpConstants.WWW_FORM_URLENCODE;

  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = DEFAULT_CONTENT_TYPE_KEY)
  private String contentTypeKey;

  @InputFieldDefault(value = "")
  @AdvancedConfig
  private String metadataPrefix;

  public FormDataToMetadata() {
  }

  @Override
  public void prepare() throws CoreException {}

  @Override
  protected void initService() throws CoreException {}

  @Override
  protected void closeService() {}

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      if (!msg.headersContainsKey(contentTypeKey())) {
        log.trace("{} not found in metadata, ignoring", contentTypeKey());
        return;
      }
      String ct = msg.getMetadataValue(contentTypeKey());
      ContentType contentType = new ContentType(ct);
      if (!DEFAULT_CONTENT_TYPE_VALUE.equalsIgnoreCase(contentType.getBaseType())) {
        log.trace("{} does not match {}", ct, DEFAULT_CONTENT_TYPE_VALUE);
        return;
      }
      Map<String, String> params =
          URLHelper.queryStringToMap(msg.getContent(), StandardCharsets.UTF_8.name());
      for (Map.Entry<String, String> e : params.entrySet()) {
        msg.addMetadata(metadataPrefix() + e.getKey(), e.getValue());
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }


  public String getContentTypeKey() {
    return contentTypeKey;
  }

  /**
   * Set the metadata key for finding out the content-type.
   *
   * @param s the metadata key.
   */
  public void setContentTypeKey(String s) {
    contentTypeKey = s;
  }


  public String getMetadataPrefix() {
    return metadataPrefix;
  }

  public void setMetadataPrefix(String metadataPrefix) {
    this.metadataPrefix = metadataPrefix;
  }

  public FormDataToMetadata withMetadataPrefix(String s) {
    setMetadataPrefix(s);
    return this;
  }

  public FormDataToMetadata withContentTypeKey(String s) {
    setContentTypeKey(s);
    return this;
  }

  private String metadataPrefix() {
    return StringUtils.defaultIfBlank(getMetadataPrefix(), "");
  }

  private String contentTypeKey() {
    return StringUtils.defaultIfBlank(getContentTypeKey(), DEFAULT_CONTENT_TYPE_KEY);
  }


}
