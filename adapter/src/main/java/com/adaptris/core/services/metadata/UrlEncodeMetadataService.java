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

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.net.URLEncoder;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link URLEncoder#encode(String, String)} on metadata values.
 * 
 * @config url-encode-metadata-service
 */
@XStreamAlias("url-encode-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "URL Encode some metadata", tag = "service,metadata")
@DisplayOrder(order = {"metadataKeyRegexp"})
public class UrlEncodeMetadataService extends ReformatMetadata {

  public UrlEncodeMetadataService() {
    super();
  }

  public UrlEncodeMetadataService(String metadataRegexp) {
    super(metadataRegexp);
  }

  @Override
  protected String reformat(String s, String msgCharset) throws Exception {
    return URLEncoder.encode(s, defaultIfEmpty(msgCharset, "UTF-8"));
  }

}
