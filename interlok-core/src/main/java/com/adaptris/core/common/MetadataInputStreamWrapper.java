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

package com.adaptris.core.common;

import java.io.InputStream;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.types.MessageWrapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageWrapper} implementation wraps a metadata value as an {@link InputStream}.
 * 
 * @config metadata-input-stream-wrapper
 * @since 3.9.0
 */
@XStreamAlias("metadata-input-stream-wrapper")
@DisplayOrder(order = {"metadataKey", "contentEncoding"})
@ComponentProfile(summary = "MessageWrapper implementation wraps a metadata value as an InputStream", since = "3.9.0")
public class MetadataInputStreamWrapper extends MetadataStreamInputParameter
    implements MessageWrapper<InputStream> {

  public MetadataInputStreamWrapper() {
    super();
  }
  
  public MetadataInputStreamWrapper(String key) {
    this();
    setMetadataKey(key);
  }

  @Override
  public InputStream wrap(InterlokMessage m) throws Exception {
    return extract(m);
  }

}
