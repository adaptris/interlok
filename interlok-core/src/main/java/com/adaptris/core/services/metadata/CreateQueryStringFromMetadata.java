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

import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
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
@DisplayOrder(order = {"metadata-filter", "resultKey", "separator", "includeQueryPrefix"})
public class CreateQueryStringFromMetadata extends UrlEncodedMetadataValues {

  @NotBlank
  @AffectsMetadata
  private String resultKey;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean includeQueryPrefix;
  @Deprecated
  @Removal(version = "3.11.0", message = "use separator instead")
  private String querySeparator;
  private transient boolean warningLogged = false;

  public CreateQueryStringFromMetadata() {
  }

  @Override
  public void prepare() throws CoreException {
    Args.notBlank(resultKey, "resultKey");
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      StringBuilder queryString = new StringBuilder(includeQueryPrefix() ? "?" : "");
      queryString.append(buildEncodedString(msg));
      if (queryString.length() > 1) {
        // We have added some parameters
        msg.addMetadata(getResultKey(), queryString.toString());
      } else {
        // No params - return an empty string
        msg.addMetadata(getResultKey(), "");
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  protected String separator() {
    if (getQuerySeparator() != null) {
      LoggingHelper.logWarning(warningLogged, () -> {
        warningLogged = true;
      }, "query-separator deprecated; use a separator instead");
      return getQuerySeparator();
    }
    return super.separator();
  }

  public String getResultKey() {
    return resultKey;
  }

  public void setResultKey(String resultKey) {
    this.resultKey = resultKey;
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

  private boolean includeQueryPrefix() {
    return BooleanUtils.toBooleanDefaultIfNull(getIncludeQueryPrefix(), true);
  }

  @Deprecated
  @Removal(version = "3.11.0", message = "use separator instead")
  public String getQuerySeparator() {
    return querySeparator;
  }

  @Deprecated
  @Removal(version = "3.11.0", message = "use separator instead")
  public void setQuerySeparator(String s) {
    this.querySeparator = s;
  }
}
