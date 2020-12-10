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
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import lombok.Getter;
import lombok.Setter;

public abstract class ComparatorImpl implements MetadataComparator {

  @AffectsMetadata
  @AutoPopulated
  @Getter
  @Setter
  private String resultKey;

  @Getter
  @Setter
  private String value;

  public ComparatorImpl() {
    setResultKey(getClass().getCanonicalName());
  }

  protected abstract boolean compare(String a, String b);

  @Override
  public boolean apply(AdaptrisMessage message, String object) {
    return compare(message.resolve(value), message.resolve(object));
  }
}
