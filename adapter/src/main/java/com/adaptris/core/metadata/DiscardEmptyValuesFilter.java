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

import org.apache.commons.lang3.StringUtils;

import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Metadata Filter implementation that removes all metadata which have an empty value.
 * 
 * @config discard-empty-values-metadata-filter
 * @since 3.7.1
 */
@XStreamAlias("discard-empty-values-metadata-filter")
public class DiscardEmptyValuesFilter extends MetadataFilterImpl
{


  public DiscardEmptyValuesFilter() {
  }

  @Override
  public MetadataCollection filter(MetadataCollection original) {
    MetadataCollection result = new MetadataCollection();
    for (MetadataElement element : original) {
      if (!StringUtils.isEmpty(element.getValue())) {
        result.add(element);
      }
    }
    return result;
  }

}
