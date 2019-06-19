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

import java.io.InputStream;
import java.io.OutputStream;
import org.apache.activemq.util.ByteArrayOutputStream;
import org.apache.commons.io.IOUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.types.MessageWrapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Returns the message payload as as byte array.
 * 
 * @config byte-array-from-payload
 */
@XStreamAlias("byte-array-from-payload")
@ComponentProfile(summary = "Returns the message payload as as byte array.", since = "3.9.0")
public class ByteArrayFromPayload implements MessageWrapper<byte[]> {

  public ByteArrayFromPayload() {

  }

  @Override
  public byte[] wrap(InterlokMessage m) throws Exception {
    if (m instanceof AdaptrisMessage) {
      return ((AdaptrisMessage) m).getPayload();
    }
    return toByteArray(m);
  }

  private byte[] toByteArray(InterlokMessage m) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (InputStream i = m.getInputStream(); OutputStream o = out) {
      IOUtils.copy(i, o);
    }
    return out.toByteArray();
  }
}
