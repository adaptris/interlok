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
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Provides a static content type.
 * <p>
 * Note that the content type character set will be derived from
 * {@link com.adaptris.core.AdaptrisMessage#getCharEncoding()} so configuring a mime type of {@code text/xml} when the
 * message has a char encoding of {@code UTF-8} will return {@code text/xml; charset=UTF-8}
 * </p>
 * 
 * @config http-configured-content-type-provider
 */
@XStreamAlias("http-configured-content-type-provider")
@DisplayOrder(order = {"mimeType"})
public class ConfiguredContentTypeProvider extends ContentTypeProviderImpl {

  @NotBlank
  @AutoPopulated
  private String mimeType;

  public ConfiguredContentTypeProvider() {
    setMimeType("text/plain");
  }

  public ConfiguredContentTypeProvider(String type) {
    this();
    setMimeType(type);
  }

  @Override
  public String getContentType(AdaptrisMessage msg) throws CoreException {
    return build(getMimeType(), msg.getContentEncoding());
  }


  public String getMimeType() {
    return mimeType;
  }

  /**
   * Set the base content type.
   * 
   * @param type the base content type; defaults to text/plain
   */
  public void setMimeType(String type) {
    this.mimeType = Args.notBlank(type, "Mime Type");
  }

}
