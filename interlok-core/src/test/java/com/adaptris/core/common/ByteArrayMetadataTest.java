/*
 * Copyright 2016 Adaptris Ltd.
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
 */
package com.adaptris.core.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class ByteArrayMetadataTest {

  private static final String HELLO_WORLD = "Hello World";
  private static final byte[] BYTE_ARRAY = "Hello World".getBytes(Charset.defaultCharset());
  private static final String KEY = "key";

  @Test
  public void testWrapString() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY, HELLO_WORLD);
    byte[] wrapped = new ByteArrayFromMetadata().withKey(KEY).wrap(msg);
    assertTrue(MessageDigest.isEqual(BYTE_ARRAY, wrapped));
  }

  @Test
  public void testWrapEmptyString() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(KEY, "");
    byte[] wrapped = new ByteArrayFromMetadata().withKey(KEY).wrap(msg);
    assertEquals(0, wrapped.length);
  }


}
