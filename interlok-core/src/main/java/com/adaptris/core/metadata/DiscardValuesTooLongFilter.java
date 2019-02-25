/*
 * Copyright 2019 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core.metadata;

import java.util.stream.Collectors;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.MetadataCollection;
import com.adaptris.util.NumberUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Metadata Filter implementation that removes all metadata where the value exceeds the configured
 * length.
 * 
 * @config discard-values-too-long-metadata-filter
 * @since 3.8.4
 */
@XStreamAlias("discard-values-too-long-metadata-filter")
@ComponentProfile(
    summary = "Discard metadata where the length of the value exceeds the configured length",
    since = "3.8.4")
public class DiscardValuesTooLongFilter extends MetadataFilterImpl {

  private static final int DEFAULT_MAX_LENGTH = 256;

  @InputFieldDefault(value = "256")
  private Integer maxLengthBytes;

  public DiscardValuesTooLongFilter() {
  }

  public DiscardValuesTooLongFilter(Integer i) {
    this();
    setMaxLengthBytes(i);
  }

  @Override
  public MetadataCollection filter(MetadataCollection original) {
    MetadataCollection result = new MetadataCollection();
    int maxLen = maxLength();
    result.addAll(original.parallelStream().filter(e -> e.getValue().length() <= maxLen)
        .collect(Collectors.toList()));
    return result;
  }

  public Integer getMaxLengthBytes() {
    return maxLengthBytes;
  }

  /**
   * Set the max length for a metadata value before it gets discarded.
   * 
   * @param bytes the length; default is 256 if not specified.
   */
  public void setMaxLengthBytes(Integer bytes) {
    this.maxLengthBytes = bytes;
  }


  private int maxLength() {
    return NumberUtils.toIntDefaultIfNull(getMaxLengthBytes(), DEFAULT_MAX_LENGTH);
  }
}
