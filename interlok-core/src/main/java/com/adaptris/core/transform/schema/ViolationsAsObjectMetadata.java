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
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Uses {@link SchemaViolations} and adds it as object metadata.
 * 
 * @config schema-violations-as-object-metadata
 */
@XStreamAlias("schema-violations-as-object-metadata")
@ComponentProfile(
    summary = "Render an XML representation of the schema violations as object metadata",
    since = "3.10.2")
public class ViolationsAsObjectMetadata extends ViolationHandlerImpl {

  @AdvancedConfig
  @InputFieldDefault(value = DEFAULT_KEY)
  private String objectMetadataKey;

  @Override
  protected void render(SchemaViolations violations, AdaptrisMessage msg) throws ServiceException {
    msg.addObjectHeader(key(), violations);
  }

  private String key() {
    return StringUtils.defaultIfBlank(getObjectMetadataKey(), DEFAULT_KEY);
  }

  public String getObjectMetadataKey() {
    return objectMetadataKey;
  }

  /**
   * The object metadata key.
   * 
   * <p>
   * If not explicitly specified defaults to {@value com.adaptris.core.transform.schema.ViolationHandlerImpl#DEFAULT_KEY}
   * </p>
   */
  public void setObjectMetadataKey(String s) {
    this.objectMetadataKey = s;
  }

  public ViolationsAsObjectMetadata withObjectMetadataKey(String key) {
    setObjectMetadataKey(key);
    return this;
  }
}
