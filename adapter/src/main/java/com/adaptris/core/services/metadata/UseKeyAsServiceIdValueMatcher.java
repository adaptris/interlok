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

import org.apache.commons.lang.StringUtils;

import com.adaptris.util.KeyValuePairBag;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of MetadataValueMatcher for {@link MetadataValueBranchingService} which returns the serviceKey as identifier of
 * the next Service to apply.
 * <p>
 * This simply allows MetadataValueBranchingService to be used without maintaining a set of mappings between metadata keys and
 * service IDs where the relationship is 1 to 1.
 * </p>
 * 
 * @config use-key-as-service-id-value-matcher
 */
@XStreamAlias("use-key-as-service-id-value-matcher")
public class UseKeyAsServiceIdValueMatcher implements MetadataValueMatcher {

  public String getNextServiceId(String serviceKey, KeyValuePairBag mappings) {
    return StringUtils.defaultIfBlank(serviceKey, null);
  }
}
