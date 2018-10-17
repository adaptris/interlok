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

import com.adaptris.util.KeyValuePairBag;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Exact value match implementation of MetadataValueMatcher for {@link MetadataValueBranchingService}.
 * 
 * @config equals-value-matcher
 * 
 * @see MetadataValueBranchingService
 * @author lchan
 */
@XStreamAlias("equals-value-matcher")
public class EqualsValueMatcher implements MetadataValueMatcher {

  public EqualsValueMatcher() {
  }

  public String getNextServiceId(String serviceKey, KeyValuePairBag mappings) {
    return mappings.getValue(serviceKey);
  }

}
