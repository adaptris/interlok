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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.util.Args;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link com.adaptris.core.Service} that filters metadata keys based on a {@link MetadataFilter}.
 * </p>
 * 
 * @config metadata-filter-service
 * 
 * 
 * @see java.util.regex.Pattern
 */
@JacksonXmlRootElement(localName = "metadata-filter-service")
@XStreamAlias("metadata-filter-service")
@AdapterComponent
@ComponentProfile(summary = "Filter and remove metadata", tag = "service,metadata")
@DisplayOrder(order = {"filter", "metadataLogger"})
public class MetadataFilterService extends MetadataServiceImpl {

  @NotNull
  @AutoPopulated
  @Valid
  @AffectsMetadata
  private MetadataFilter filter;

  public MetadataFilterService() {
    setFilter(new NoOpMetadataFilter());
  }

  @Override
  public void doService(AdaptrisMessage msg) {
    log.trace("Filtering metadata using [{}]", filter.getClass().getCanonicalName());
    MetadataCollection filtered = filter.filter(msg);
    msg.clearMetadata();
    msg.setMetadata(filtered.toSet());
    logMetadata("Metadata preserved : {}", filtered);
  }

  public MetadataFilter getFilter() {
    return filter;
  }

  public void setFilter(MetadataFilter mf) {
    filter = Args.notNull(mf, "filter");
  }

  public MetadataFilterService withFilter(MetadataFilter mf) {
    setFilter(mf);
    return this;
  }
}
