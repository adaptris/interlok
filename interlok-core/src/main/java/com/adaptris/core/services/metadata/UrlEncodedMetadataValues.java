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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;

public abstract class UrlEncodedMetadataValues extends ServiceImp {

  protected static final String AMPERSAND = "&";

  @AdvancedConfig
  @InputFieldHint(style = "BLANKABLE")
  private String separator;
  @Valid
  @InputFieldDefault(value = "RemoveAllMetadataFilter")
  private MetadataFilter metadataFilter;

  public UrlEncodedMetadataValues() {
  }

  @Override
  public void prepare() throws CoreException {}

  @Override
  protected void initService() throws CoreException {}

  @Override
  protected void closeService() {}

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  public void setMetadataFilter(MetadataFilter metadataFilter) {
    this.metadataFilter = metadataFilter;
  }

  private MetadataFilter metadataFilter() {
    return ObjectUtils.defaultIfNull(getMetadataFilter(), new RemoveAllMetadataFilter());
  }


  /**
   * @return the querySeparator
   */
  public String getSeparator() {
    return separator;
  }

  /**
   * Set the separator to be used in between each parameter in the String..
   * 
   * <p>
   * Although '&amp;' is the conventional standard (or even a semi-colon ';'), there isn't a formal
   * standard for separating query parameters; RFC3986 simply states:
   * </p>
   * 
   * <pre>
   * {@code
   *    URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
   *    query         = *( pchar / "/" / "?" )
   * }
   * </pre>
   * 
   * @param s the separator to set, defaults to null which indicates '&amp;'.
   */
  public void setSeparator(String s) {
    separator = s;
  }

  protected String separator() {
    return ObjectUtils.defaultIfNull(getSeparator(), AMPERSAND);
  }

  protected String buildEncodedString(AdaptrisMessage msg) throws Exception {
    MetadataCollection filtered = metadataFilter().filter(msg);
    StringBuilder result = new StringBuilder();
    for (MetadataElement e : filtered) {
      if (result.length() > 1) {
        // This is not the first parameter so add a separator
        result.append(separator());
      }
      result.append(e.getKey()).append("=")
            .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8.name()));
    }
    return result.toString();
  }

  public <T extends UrlEncodedMetadataValues> T withMetadataFilter(MetadataFilter filter) {
    setMetadataFilter(filter);
    return (T) this;
  }

  public <T extends UrlEncodedMetadataValues> T withQuerySeparator(String s) {
    setSeparator(s);
    return (T) this;
  }
}
