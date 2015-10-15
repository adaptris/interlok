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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.http.server.ResponseHeaderProvider;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ResponseHeaderProvider} implementation that providers HTTP response headers from metadata.
 * 
 * @config jetty-metadata-response-headers
 * 
 */
@XStreamAlias("jetty-metadata-response-headers")
public class MetadataResponseHeaderProvider implements ResponseHeaderProvider<HttpServletResponse> {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotNull
  @Valid
  private MetadataFilter filter;

  public MetadataResponseHeaderProvider() {

  }

  public MetadataResponseHeaderProvider(MetadataFilter f) {
    this();
    setFilter(f);
  }

  @Override
  public HttpServletResponse addHeaders(AdaptrisMessage msg, HttpServletResponse target) {
    MetadataCollection subset = getFilter().filter(msg);
    for (MetadataElement me : subset) {
      log.trace("Adding Response Header [{}: {}]", me.getKey(), me.getValue());
      target.addHeader(me.getKey(), me.getValue());
    }
    return target;
  }

  public MetadataFilter getFilter() {
    return filter;
  }

  public void setFilter(MetadataFilter filter) {
    this.filter = Args.notNull(filter, "Metadata Filter");
  }

}
