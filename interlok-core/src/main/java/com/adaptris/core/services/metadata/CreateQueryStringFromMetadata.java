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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service that creates a URL query string from the specified metadata keys.
 * 
 * @config create-query-string-from-metadata
 * 
 * @author sellidge
 */
@XStreamAlias("create-query-string-from-metadata")
@AdapterComponent
@ComponentProfile(summary = "Create the query portion of a URL from metadata", tag = "service,metadata,http,https")
@DisplayOrder(order = {"metadata-filter", "metadataKeys", "resultKey", "querySeparator"})
public class CreateQueryStringFromMetadata extends ServiceImp {

  private static final String AMPERSAND = "&";

  @NotBlank
  @AffectsMetadata
  private String resultKey;
  @AdvancedConfig
  @InputFieldHint(style = "BLANKABLE")
  private String querySeparator;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean includeQueryPrefix;
  @Valid
  @InputFieldDefault(value = "RemoveAllMetadataFilter")
  private MetadataFilter metadataFilter;

  public CreateQueryStringFromMetadata() {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    StringBuilder queryString = new StringBuilder(includeQueryPrefix() ? "?" : "");

    MetadataCollection filtered = metadataFilter().filter(msg);
    for (MetadataElement e : filtered) {
      if (queryString.length() > 1) {
        // This is not the first parameter so add a separator
        queryString.append(querySeparator());
      }
      try {
        queryString.append(e.getKey()).append("=").append(URLEncoder.encode(e.getValue(), "UTF-8"));
      } catch (UnsupportedEncodingException ex) {
        // This will not occur, but we will deal with it nonetheless.
        throw ExceptionHelper.wrapServiceException(ex);
      }
    }
    if (queryString.length() > 1) {
      // We have added some parameters
      msg.addMetadata(getResultKey(), queryString.toString());
    }
    else {
      // No params - return an empty string
      msg.addMetadata(getResultKey(), "");
    }
  }


  @Override
  protected void initService() throws CoreException {}

  @Override
  protected void closeService() {
  }

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  public void setMetadataFilter(MetadataFilter metadataFilter) {
    this.metadataFilter = metadataFilter;
  }

  private MetadataFilter metadataFilter() {
    return ObjectUtils.defaultIfNull(getMetadataFilter(), new RemoveAllMetadataFilter());
  }

  public String getResultKey() {
    return resultKey;
  }

  public void setResultKey(String resultKey) {
    this.resultKey = resultKey;
  }

  /**
   * @return the querySeparator
   */
  public String getQuerySeparator() {
    return querySeparator;
  }

  /**
   * Set the separator to be used in between each parameter in the query String..
   * 
   * <p>
   * Although '&amp;' is the conventional standard (or even a semi-colon ';'), there isn't a formal standard for separating query
   * parameters; RFC3986 simply states:
   * </p>
   * 
   * <pre>
   * {@code
   *    URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
   *    query         = *( pchar / "/" / "?" )
   * }
   * </pre>
   * 
   * @param s the querySeparator to set, defaults to null which indicates '&amp;'.
   */
  public void setQuerySeparator(String s) {
    querySeparator = s;
  }

  String querySeparator() {
    return getQuerySeparator() == null ? AMPERSAND : getQuerySeparator();
  }

  @Override
  public void prepare() throws CoreException {
  }

  public Boolean getIncludeQueryPrefix() {
    return includeQueryPrefix;
  }

  /**
   * Whether or not to include the standard query prefix
   * 
   * @param b
   */
  public void setIncludeQueryPrefix(Boolean b) {
    this.includeQueryPrefix = b;
  }

  boolean includeQueryPrefix() {
    return BooleanUtils.toBooleanDefaultIfNull(getIncludeQueryPrefix(), true);
  }
}
