/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.common;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Wraps a metadata value as a byte[]
 * 
 * @config byte-array-from-metadata
 */
@XStreamAlias("byte-array-from-metadata")
@ComponentProfile(summary="Turn a metadata value into a byte array.", since="3.9.0")
@DisplayOrder(order = {"key", "translator"})
public class ByteArrayFromMetadata extends ByteArrayFromMetadataWrapper {

  public ByteArrayFromMetadata() {
    super();
  }

  @Override
  public byte[] wrap(InterlokMessage m) throws Exception {
    String value = m.getMessageHeaders().get(getKey());
    return toByteArray(value);
  }

}
