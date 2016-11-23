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

package com.adaptris.core.services.metadata.compare;

import com.adaptris.annotation.AffectsMetadata;

public abstract class ComparatorImpl implements MetadataComparator {
  
  @AffectsMetadata
  private String resultKey;
  
  public ComparatorImpl() {
    setResultKey(getClass().getCanonicalName());
  }

  public String getResultKey() {
    return resultKey;
  }

  /**
   * Set the key where we store the result.
   * 
   * @param rk the key, default is the classname
   */
  public void setResultKey(String rk) {
    this.resultKey = rk;
  }

}
