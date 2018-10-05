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

package com.adaptris.core.services.system;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Provides the metadata value associated with the specified key as a command line argument
 * 
 * @config system-command-metadata-argument
 * 
 * @author sellidge
 */
@XStreamAlias("system-command-metadata-argument")
public class MetadataCommandArgument implements CommandArgument {

  @NotBlank
  private String key;
  

  public MetadataCommandArgument() {

  }
  
  public MetadataCommandArgument(String arg) {
    this();
    setKey(arg);
  }
  

  @Override
  public String retrieveValue(AdaptrisMessage msg) {
    return msg.getMetadataValue(key);
  }

  public String getKey() {
    return key;
  }

  /**
   * The metadata key to be dereferenced
   * @param key
   */
  public void setKey(String key) {
    this.key = key;
  }

}
