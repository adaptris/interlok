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

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link com.adaptris.core.http.server.ParameterHandler} implementation that stores headers as standard metadata.
 * 
 * @config jetty-http-parameters-as-metadata
 * 
 */
@XStreamAlias("jetty-http-parameters-as-metadata")
public class MetadataParameterHandler extends ParameterHandlerImpl {


  public MetadataParameterHandler() {
  }

  public MetadataParameterHandler(String prefix) {
    this();
    setParameterPrefix(prefix);
  }


  private void handleParameters(AdaptrisMessage message, HttpServletRequest request, String itemPrefix) {
    String prefix = defaultIfEmpty(itemPrefix, "");
    
    for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
      String key = e.nextElement();
      String value = request.getParameter(key);
      message.addMetadata(prefix + key, value);
    } 
  }

  @Override
  public void handleParameters(AdaptrisMessage message, HttpServletRequest request) {
    handleParameters(message, request, parameterPrefix());
  }
}
