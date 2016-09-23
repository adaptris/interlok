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
package com.adaptris.core.security.access;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Build the identity from metadata which is mapped to specific values.
 * 
 * <p>
 * In the event that your metadata keys match the values within the identity map then you are better off using
 * {@link MetadataIdentityBuilder} instead.
 * </p>
 * 
 * @config mapped-metadata-identity-builder
 */
@XStreamAlias("mapped-metadata-identity-builder")
public class MappedMetadataIdentityBuilder extends MetadataIdentityBuilderImpl {

  // Use a list in case someone wants to map the same metadata value onto
  // multiple identity keys.
  @AutoPopulated
  @NotNull
  @Valid
  private KeyValuePairList metadataMap;

  public MappedMetadataIdentityBuilder() {
    setMetadataMap(new KeyValuePairList());
  }

  public MappedMetadataIdentityBuilder(KeyValuePairList map) {
    this();
    setMetadataMap(map);
  }

  public MappedMetadataIdentityBuilder(MetadataSource type, KeyValuePairList map) {
    this();
    setMetadataMap(map);
    setMetadataSource(type);
  }

  @Override
  public Map<String, Object> build(AdaptrisMessage msg) {
    Map<String, Object> result = new HashMap<>();
    for (KeyValuePair kvp : getMetadataMap()) {
      result.put(kvp.getValue(), getValue(msg, kvp.getKey()));
    }
    return result;
  }

  /**
   * @return the metadataMap
   */
  public KeyValuePairList getMetadataMap() {
    return metadataMap;
  }

  /**
   * Sets a {@link KeyValuePairList} in which the key is the metadata key, and the value is the key for the identity map.
   *
   * @param m a {@link KeyValuePairList}
   */
  public void setMetadataMap(KeyValuePairList metadataMap) {
    this.metadataMap = Args.notNull(metadataMap, "metadataMap");
  }

}
