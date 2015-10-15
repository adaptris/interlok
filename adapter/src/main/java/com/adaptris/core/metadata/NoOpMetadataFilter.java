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

package com.adaptris.core.metadata;

import java.util.Set;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Metadata Filter implementation that does no filtering.
 * 
 * @config no-op-metadata-filter
 */
@XStreamAlias("no-op-metadata-filter")
public class NoOpMetadataFilter implements MetadataFilter {


  public NoOpMetadataFilter() {
  }

  @Override
  public MetadataCollection filter(AdaptrisMessage message) {
    return filter(message.getMetadata());
  }

  @Override
  public MetadataCollection filter(Set<MetadataElement> original) {
    return filter(new MetadataCollection(original));
  }

  /**
   * Simply returns a shallow clone of the original metadata set.
   *
   */
  @Override
  public MetadataCollection filter(MetadataCollection original) {
    return (MetadataCollection) original.clone();
  }

}
