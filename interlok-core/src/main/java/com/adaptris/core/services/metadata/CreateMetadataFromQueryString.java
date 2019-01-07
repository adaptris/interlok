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

import com.adaptris.annotation.*;
import com.adaptris.core.*;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@XStreamAlias("create-metadata-from-query-string")
@AdapterComponent
@ComponentProfile(summary = "Change metadata query string into new metadata", tag = "service,metadata")
@DisplayOrder(order = {"metadataKey"})
public class CreateMetadataFromQueryString extends ServiceImp {

  @NotBlank
  @AffectsMetadata
  private String metadataKey;

  @InputFieldDefault(value = "false")
  private Boolean includeQueryPrefix;

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

  public Boolean getIncludeQueryPrefix() {
    return includeQueryPrefix;
  }

  public void setIncludeQueryPrefix(Boolean includeQueryPrefix) {
    this.includeQueryPrefix = includeQueryPrefix;
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String metadataValue = msg.getMetadataValue(getMetadataKey());
    URI uri = URI.create(getQueryPrefix() + metadataValue);
    List<NameValuePair> pairs = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8.toString());
    pairs.forEach(pair -> msg.addMetadata(pair.getName(), pair.getValue()));
  }

  @Override
  protected void initService() throws CoreException {

  }

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {
  }

  private String getQueryPrefix() {
    return includeQueryPrefix() ? "?" : "";
  }

  boolean includeQueryPrefix() {
    return includeQueryPrefix != null ? includeQueryPrefix.booleanValue() : false;
  }
}
