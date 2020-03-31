/*
 * Copyright 2020 Adaptris Ltd.
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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Takes a metadata value and escapes double quote.
 * <p>
 * Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will be changed to escaped double quote
 * </p>
 * 
 * @config metadata-value-escape-double-quote
 * 
 * 
 */
@XStreamAlias("metadata-value-escape-double-quote")
@AdapterComponent
@ComponentProfile(summary = "Changes matching metadata double quote into escaped double quote", tag = "service,metadata,string,escape,quote,double")
@DisplayOrder(order = {"metadataKeyRegexp", "metadataLogger"})
public class MetadataValueEscapeDoubleQuote extends ReformatMetadata {

  public MetadataValueEscapeDoubleQuote() {
    super();
  }

  @Override
  public String reformat(String toChange, String msgCharset) {
    return toChange.replaceAll("\"", "\\\\\"");
  }

}
