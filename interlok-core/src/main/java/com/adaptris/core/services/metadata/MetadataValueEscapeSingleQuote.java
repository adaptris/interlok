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
 * Takes a metadata value and escapes single quote.
 * <p>
 * Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will be changed to escaped single quote
 * </p>
 * 
 * @config metadata-value-escape-single-quote
 * 
 * 
 */
@XStreamAlias("metadata-value-escape-single-quote")
@AdapterComponent
@ComponentProfile(summary = "Changes matching metadata single quote into escaped single quote", tag = "service,metadata", since = "3.10.1")
@DisplayOrder(order = {"metadataKeyRegexp", "metadataLogger"})
public class MetadataValueEscapeSingleQuote extends ReformatMetadata {

  public MetadataValueEscapeSingleQuote() {
    super();
  }

  @Override
  public String reformat(String toChange, String msgCharset) {
    return toChange.replaceAll("'", "\\\\'");
  }

}
