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

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link Service} that filters metadata keys based on a {@link MetadataFilter}.
 * </p>
 * 
 * @config metadata-filter-service
 * 
 * @license BASIC
 * @see java.util.regex.Pattern
 */
@XStreamAlias("metadata-filter-service")
public class MetadataFilterService extends ServiceImp {

  @NotNull
  @AutoPopulated
  @Valid
  private MetadataFilter filter;

  public MetadataFilterService() {
    setFilter(new NoOpMetadataFilter());
  }

  @Override
  public void doService(AdaptrisMessage msg) {
    log.trace("Filtering metadata using [" + filter.getClass().getCanonicalName() + "]");
    MetadataCollection filtered = filter.filter(msg);
    msg.clearMetadata();
    StringBuffer filteredKeys = new StringBuffer("Metadata keys preserved:");
    for (MetadataElement e : filtered) {
      filteredKeys.append(" ");
      filteredKeys.append(e.getKey());
      msg.addMetadata(e);
    }
    log.trace(filteredKeys.toString());
  }


  @Override
  protected void initService() throws CoreException {

  }

  @Override
  protected void closeService() {

  }
  public MetadataFilter getFilter() {
    return filter;
  }

  public void setFilter(MetadataFilter mf) {
    if (mf == null) {
      throw new IllegalArgumentException("Filter may not be null");
    }
    filter = mf;
  }

  @Override
  public void prepare() throws CoreException {
  }

}
