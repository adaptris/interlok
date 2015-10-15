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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link com.adaptris.core.http.server.ParameterHandler} implementation stores HTTP headers as object metadata.
 * 
 * @config jetty-http-parameters-as-object-metadata
 * 
 */
@XStreamAlias("jetty-http-parameters-as-object-metadata")
public class ObjectMetadataParameterHandler extends ParameterHandlerImpl {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  public ObjectMetadataParameterHandler() {
  }

  public ObjectMetadataParameterHandler(String prefix) {
    this();
    setParameterPrefix(prefix);
  }

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    String prefix = defaultIfEmpty(itemPrefix, "");
    
    for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
      String key = e.nextElement();
      String value = request.getParameter(key);
      String metadataKey = prefix + key;
      log.trace("Adding Object Metadata [{}: {}]", metadataKey, value);
      message.addObjectMetadata(metadataKey, value);
    } 
  }

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request) {
    handleParameters(message, request, parameterPrefix());
  }
}
