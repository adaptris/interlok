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

package com.adaptris.core.http.jetty;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link com.adaptris.core.http.server.HeaderHandler} implementation that stores HTTP headers as standard metadata.
 * 
 * @config jetty-http-headers-as-metadata
 * 
 */
@XStreamAlias("jetty-http-headers-as-metadata")
public class MetadataHeaderHandler extends HeaderHandlerImpl {

  public MetadataHeaderHandler() {
  }

  public MetadataHeaderHandler(String prefix) {
    this();
    setHeaderPrefix(prefix);
  }

  @Override
  public void handleHeaders(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    String prefix = defaultIfEmpty(itemPrefix, "");
    for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      String value = request.getHeader(key);
      String metadataKey = prefix + key;
      log.trace("Adding Metadata [{}: {}]", metadataKey, value);
      message.addMetadata(prefix + key, value);
    }
  }
  
  @Override
  public void handleHeaders(AdaptrisMessage msg, HttpServletRequest request) {
    handleHeaders(msg, request, headerPrefix());
  }


}
