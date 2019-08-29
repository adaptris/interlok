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

package com.adaptris.core.http;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import java.util.Locale;
import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Provides a content type derived from metadata.
 * <p>
 * Note that the content type charset will be derived from {@link com.adaptris.core.AdaptrisMessage#getCharEncoding()}
 * so configuring a mime type of {@code text/xml} when the message has a char encoding of
 * {@code UTF-8} will return {@code text/xml; charset="UTF-8"}. No validation is done on the resulting string or on the value
 * that is taken from metadata.
 * </p>
 * 
 * @config http-metadata-content-type-provider
 */
@XStreamAlias("http-metadata-content-type-provider")
@DisplayOrder(order = {"metataKey", "defaultMimeType"})
public class MetadataContentTypeProvider extends ContentTypeProviderImpl {

  @NotBlank
  private String metadataKey;
  @AutoPopulated
  @NotBlank
  private String defaultMimeType;


  public MetadataContentTypeProvider() {
    setDefaultMimeType("text/plain");
  }

  public MetadataContentTypeProvider(String key) {
    this();
    setMetadataKey(key);
  }

  @Override
  public String getContentType(AdaptrisMessage msg) throws CoreException {
    return build(extract(msg), msg.getContentEncoding());
  }

  private String extract(AdaptrisMessage msg) throws CoreException {
    if (isBlank(getMetadataKey())) {
      throw new CoreException("metadata key is blank");
    }
    return defaultIfBlank(msg.getMetadataValue(getMetadataKey()), defaultMimeType).toLowerCase(Locale.ROOT);
  }


  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * Set the metadata item containing content type.
   * 
   * @param key the key containing the base content type
   */
  public void setMetadataKey(String key) {
    this.metadataKey = Args.notBlank(key, "Metadata Key");
  }

  public String getDefaultMimeType() {
    return defaultMimeType;
  }

  /**
   * Set the default mime type to use if the metadata key does not exist.
   * 
   * @param mt the mime type; defaults to text/plain
   */
  public void setDefaultMimeType(String mt) {
    this.defaultMimeType = Args.notBlank(mt, "Default Mime Type");
  }

}
