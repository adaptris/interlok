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

import java.net.URLDecoder;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link URLDecoder#decode(String, String)} on metadata values.
 * 
 * @config url-decode-metadata-service
 * 
 */
@XStreamAlias("url-decode-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "URL Decode some metadata", tag = "service,metadata")
@DisplayOrder(order = {"metadataKeyRegexp"})
public class UrlDecodeMetadataService extends ReformatMetadata {

  public UrlDecodeMetadataService() {
    super();
  }

  public UrlDecodeMetadataService(String metadataRegexp) {
    super(metadataRegexp);
  }

  @Override
  public String reformat(String s, String msgCharset) throws Exception {
    return URLDecoder.decode(s, defaultIfEmpty(msgCharset, "UTF-8"));
  }

}
