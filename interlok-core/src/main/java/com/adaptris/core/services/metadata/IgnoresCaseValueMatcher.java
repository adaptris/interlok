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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Ignores case match implementation of MetadataValueMatcher for {@link MetadataValueBranchingService}.
 * 
 * @config ignores-case-value-matcher
 * 
 * @author lchan
 */
@JacksonXmlRootElement(localName = "ignores-case-value-matcher")
@XStreamAlias("ignores-case-value-matcher")
public class IgnoresCaseValueMatcher implements MetadataValueMatcher {

  public IgnoresCaseValueMatcher() {
  }

  public String getNextServiceId(String serviceKey, KeyValuePairBag mappings) {
    return mappings.getValueIgnoringKeyCase(serviceKey);
  }

}
