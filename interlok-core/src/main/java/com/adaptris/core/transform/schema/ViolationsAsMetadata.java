/*
 * Copyright 2020 Adaptris Ltd.
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
package com.adaptris.core.transform.schema;

import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Uses {@link SchemaViolations} and adds it as standard metadata.
 * 
 * <p>
 * This renders any schema violations as XML and stores it as standard metadata against the
 * specified key.
 * </p>
 *
 * @config schema-violations-as-metadata
 */
@XStreamAlias("schema-violations-as-metadata")
@ComponentProfile(
    summary = "Render an XML representation of the schema violations as standard metadata",
    since = "3.10.2")
public class ViolationsAsMetadata extends ViolationHandlerImpl {
  @AdvancedConfig
  @InputFieldDefault(value = DEFAULT_KEY)
  private String metadataKey;

  @Override
  protected void render(SchemaViolations violations, AdaptrisMessage msg) throws ServiceException {
    try {
      msg.addMessageHeader(key(), toString(violations));
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  private String key() {
    return StringUtils.defaultIfBlank(getMetadataKey(), DEFAULT_KEY);
  }

  public ViolationsAsMetadata withMetadataKey(String key) {
    setMetadataKey(key);
    return this;
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * The metadata key.
   * 
   * <p>
   * If not explicitly specified defaults to {@value #DEFAULT_KEY}
   * </p>
   */
  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

}
