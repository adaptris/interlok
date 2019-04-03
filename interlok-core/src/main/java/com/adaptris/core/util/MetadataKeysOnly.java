/*
 * Copyright 2019 Adaptris Ltd.
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
package com.adaptris.core.util;

import java.util.Collection;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.MetadataLogger;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * MetadataLogger implementation that that logs metadata keys only
 * 
 * @config message-logging-with-metadata-keys-only
 */
@ComponentProfile(summary = "Log metadata keys only", since = "3.8.4")
@XStreamAlias("message-logging-with-metadata-keys-only")
public class MetadataKeysOnly implements MetadataLogger {

  @Override
  public String toString(Collection<MetadataElement> elements) {
    return String.format("Metadata Keys : %s", format(elements).toString());
  }

  private Collection<MetadataElement> format(Collection<MetadataElement> set) {
    MetadataCollection metadata = new MetadataCollection();
    set.parallelStream().forEach(e -> {
      metadata.add(new KeysOnly(e.getKey(), e.getValue()));
    });
    return metadata;
  }

  private class KeysOnly extends MetadataElement {

    private static final long serialVersionUID = 2019040201L;

    public KeysOnly(String key, String value) {
      super(key, value);
    }

    @Override
    public String toString() {
      return String.format("[%s]", getKey());
    }
  }

}
