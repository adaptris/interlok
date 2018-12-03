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

package com.adaptris.core.services.findreplace;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ReplacementSource} implementation which returns the metadata value when the replacement value is treated as a metadata
 * key.
 * 
 * <p>
 * Used with {@link FindAndReplaceService} to replace text in the message.
 * </p>
 * 
 * @config metadata-replacement-source
 */
@XStreamAlias("metadata-replacement-source")
@DisplayOrder(order = {"value"})
public class MetadataReplacementSource extends AbstractReplacementSource {

  public MetadataReplacementSource() {
    super();
  }

  public MetadataReplacementSource(String value) {
    this();
    setValue(value);
  }

  public String obtainValue(AdaptrisMessage msg) {
    return msg.getMetadataValue(this.getValue());
  }
}
