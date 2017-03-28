/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * {@link MetadataFilter} implementation that wraps a list of filters.
 * 
 * @config composite-metadata-filter
 * 
 */
@XStreamAlias("composite-metadata-filter")
public class CompositeMetadataFilter implements MetadataFilter {

  @NotNull
  @Valid
  @AutoPopulated
  @XStreamImplicit
  private List<MetadataFilter> filters;

  public CompositeMetadataFilter() {
    setFilters(new ArrayList<MetadataFilter>());
  }

  public CompositeMetadataFilter(MetadataFilter... filters) {
    this();
    setFilters(new ArrayList<MetadataFilter>(Arrays.asList(filters)));
  }

  @Override
  public MetadataCollection filter(AdaptrisMessage message) {
    return filter(message.getMetadata());
  }

  @Override
  public MetadataCollection filter(Set<MetadataElement> original) {
    return filter(new MetadataCollection(original));
  }

  @Override
  public MetadataCollection filter(MetadataCollection original) {
    MetadataCollection result = original;
    for (MetadataFilter f : filters) {
      result = f.filter(result);
    }
    return result;
  }

  /**
   * @return the filters
   */
  public List<MetadataFilter> getFilters() {
    return filters;
  }

  /**
   * @param filters the filters to set
   */
  public void setFilters(List<MetadataFilter> filters) {
    this.filters = filters;
  }

}
