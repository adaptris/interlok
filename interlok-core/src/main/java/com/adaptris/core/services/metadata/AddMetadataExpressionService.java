/*
 * Copyright 2024 Adaptris Ltd.
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
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * <p>
 * Extends of {@link com.adaptris.core.services.metadata.AddMetadataService} by resolving any expressions in
 * key and value.
 * </p>
 *
 * @config add-metadata-expression-service
 *
 *
 */
@XStreamAlias("add-metadata-expression-service")
@AdapterComponent
@ComponentProfile(summary = "Add static metadata to a message, converting any expressions", tag = "service,metadata")
@DisplayOrder(order = {"appendKeys", "resultKey", "metadataLogger"})
public class AddMetadataExpressionService extends AddMetadataService {

  /**
   * <p>
   * Creates a new instance.  Default key for result metatadata is
   * 'metadata-appender-service'.
   * </p>
   */
  public AddMetadataExpressionService() {
    super();
  }

  @Override
  protected void beforeAdd(MetadataElement element, AdaptrisMessage msg) {
    element.setKey(msg.resolve(element.getKey()));
    element.setValue(msg.resolve(element.getValue()));
  }
}
