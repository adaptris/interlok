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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Build the identity from metadata
 * 
 * @config metadata-identity-builder
 */
@XStreamAlias("metadata-identity-builder")
public class MetadataIdentityBuilder extends MetadataIdentityBuilderImpl {

  @XStreamImplicit(itemFieldName = "metadata-key")
  @AutoPopulated
  @NotNull
  private List<String> metadataKeys;

  public MetadataIdentityBuilder() {
    setMetadataKeys(new ArrayList<String>());
  }

  public MetadataIdentityBuilder(List<String> list) {
    this();
    setMetadataKeys(list);
  }

  public MetadataIdentityBuilder(MetadataSource type, List<String> list) {
    this();
    setMetadataKeys(list);
    setMetadataSource(type);
  }

  @Override
  public Map<String, Object> build(AdaptrisMessage msg) {
    Map<String, Object> result = new HashMap<>();
    for (String key : getMetadataKeys()) {
      result.put(key, getValue(msg, key));
    }
    return result;
  }

  /**
   * @return the metadataKeys
   */
  public List<String> getMetadataKeys() {
    return metadataKeys;
  }

  /**
   * @param m the metadataKeys to set
   */
  public void setMetadataKeys(List<String> m) {
    this.metadataKeys = Args.notNull(m, "metadataKeys");
  }

}
