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
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Provides a static content type.
 * <p>
 * Differs from {@link ConfiguredContentTypeProvider} because it doesn't try to add a charset.
 * </p>
 * 
 * @config http-raw-content-type-provider
 */
@XStreamAlias("http-raw-content-type-provider")
@DisplayOrder(order = {"contentType"})
public class RawContentTypeProvider implements ContentTypeProvider {

  @NotBlank
  @AutoPopulated
  @InputFieldHint(expression = true)
  private String contentType;

  public RawContentTypeProvider() {
    setContentType("text/plain");
  }

  public RawContentTypeProvider(String type) {
    this();
    setContentType(type);
  }

  @Override
  public String getContentType(AdaptrisMessage msg) throws CoreException {
    // Unfold the header to force it onto the same line regardless.
    return msg.resolve(getContentType()).replaceAll("\\s\\r\\n\\s+", " ").replaceAll("\\r\\n\\s+", " ");
  }


  public String getContentType() {
    return contentType;
  }

  /**
   * Set the base content type.
   * 
   * @param type the base content type; defaults to text/plain
   */
  public void setContentType(String type) {
    this.contentType = Args.notBlank(type, "Content Type");
  }

}
