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

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;

public abstract class MetadataIdentityBuilderImpl extends IdentityBuilderImpl {

  /**
   * Types of metadata.
   * 
   */
  public enum MetadataSource {
    /**
     * Standard Metadata.
     * 
     */
    Standard {
      @Override
      String get(AdaptrisMessage msg, String key) {
        return msg.getMetadataValue(key);
      }
    },
    /**
     * Object Metadata.
     * 
     */
    Object {
      @Override
      Object get(AdaptrisMessage msg, String key) {
        return msg.getObjectHeaders().get(key);
      }
    };

    abstract Object get(AdaptrisMessage msg, String key);
  };

  @AdvancedConfig
  private MetadataSource metadataSource;

  public MetadataIdentityBuilderImpl() {
  }

  /**
   * @return the metadataSource
   */
  public MetadataSource getMetadataSource() {
    return metadataSource;
  }

  /**
   * @param metadataSource the metadataSource to set
   */
  public void setMetadataSource(MetadataSource metadataSource) {
    this.metadataSource = Args.notNull(metadataSource, "metadataSource");
  }

  protected MetadataSource metadataSource() {
    return getMetadataSource() != null ? getMetadataSource() : MetadataSource.Standard;
  }

  protected Object getValue(AdaptrisMessage msg, String key) {
    return metadataSource().get(msg, key);
  }
}
