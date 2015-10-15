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

/**
 * Interface for finding matching metadata on a AdaptrisMessage.
 * <p>
 * This is a generic interface that can be used to create a subset of @{@link MetadataElement} instances from a full set of
 * metadata.
 * </p>
 *
 * @author amcgrath
 *
 */
public interface MetadataFilter {

  /**
   * Return all the metadata that matches the filter.
   * 
   * @param message the AdaptrisMessage instance.
   * @return a {@link MetadataCollection} instance that has been filtered.
   */
  MetadataCollection filter(AdaptrisMessage message);

  /**
   * Return all the metadata that matches the filter.
   * 
   * @param original the original set of metadata possibly from {@link AdaptrisMessage#getMetadata()}
   * @return a {@link MetadataCollection} instance that has been filtered.
   */
  MetadataCollection filter(Set<MetadataElement> original);

  /**
   * Return all the metadata that matches the filter.
   *
   * @param original the original set of metadata.
   * @return a {@link MetadataCollection} instance that has been filtered.
   */
  MetadataCollection filter(MetadataCollection original);
}
