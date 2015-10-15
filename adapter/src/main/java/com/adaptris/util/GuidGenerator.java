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

package com.adaptris.util;

import java.util.UUID;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Creates a GUID using {@link UUID#randomUUID()}.
 * 
 * 
 @config guid-generator
 */
@XStreamAlias("guid-generator")
public class GuidGenerator implements IdGenerator {

  public GuidGenerator() {
  }

  /**
   * Get the next unique ID.
   *
   * @return the next unique ID
   */
  public String getUUID() {
    return UUID.randomUUID().toString();
  }

  /**
   *
   * @see com.adaptris.util.IdGenerator#create(java.lang.Object)
   */
  @Override
  public String create(Object msg) {
    return getUUID();
  }

  /**
   * Get a safe UUID
   *
   * @return a UUID stripped of colons and dashes
   */
  public String safeUUID() {
    return getUUID().replaceAll(":", "").replaceAll("-", "");
  }
}
