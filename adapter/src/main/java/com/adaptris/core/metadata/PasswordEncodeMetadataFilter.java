/*
 * Copyright Adaptris Ltd.
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

package com.adaptris.core.metadata;

import org.apache.commons.lang.StringUtils;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.MetadataElement;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Metadata Filter implementation that encodes all values that match the specified key.
 * <p>
 * Note that keys that do not match will still be returned; just that keys that do match {@link #getPasswordPatterns()} will have
 * {@link Password#encode(String,String)} applied to the values.Please note that a new {@code MetadataElement} is created as a
 * result of the encode/deocde process, so any changes may not be reflected in the original message.
 * </p>
 * 
 * @config password-encoder-metadata-filter
 * @since 3.8.1
 */
@XStreamAlias("password-encoder-metadata-filter")
public class PasswordEncodeMetadataFilter extends PasswordMetadataFilter {

  @InputFieldDefault(value = Password.PORTABLE_PASSWORD)
  private String style;

  protected MetadataElement handlePassword(MetadataElement element) throws PasswordException {
    return new MetadataElement(element.getKey(), Password.encode(element.getValue(), style()));
  }

  public String getStyle() {
    return style;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  private String style() {
    return StringUtils.defaultIfBlank(getStyle(), Password.PORTABLE_PASSWORD);
  }

  public PasswordEncodeMetadataFilter withStyle(String s) {
    setStyle(s);
    return this;
  }

}
