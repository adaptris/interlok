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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class ByteArrayObjectMetadataTest {

  private static final byte[] BYTE_ARRAY = "Hello World".getBytes(Charset.defaultCharset());
  private static final String KEY = "key";

  @Test
  public void testWrapNullObject() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.getObjectHeaders().put(KEY, null);
    byte[] wrapped = new ByteArrayFromObjectMetadata().withKey(KEY).wrap(msg);
    assertNull(wrapped);
  }
  
  @Test(expected = ClassCastException.class)
  public void testWrapNotByteArray() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.getObjectHeaders().put(KEY, new Object());
    byte[] wrapped = new ByteArrayFromObjectMetadata().withKey(KEY).wrap(msg);
  }

  @Test
  public void testWrapByteArray() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.getObjectHeaders().put(KEY, BYTE_ARRAY);
    byte[] wrapped = new ByteArrayFromObjectMetadata().withKey(KEY).wrap(msg);
    assertTrue(MessageDigest.isEqual(BYTE_ARRAY, wrapped));
  }

  @Test
  public void testWrapString() throws Exception {
    String s = StringUtils.toEncodedString(BYTE_ARRAY, Charset.defaultCharset());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.getObjectHeaders().put(KEY, s);
    byte[] wrapped = new ByteArrayFromObjectMetadata().withKey(KEY).wrap(msg);
    assertTrue(MessageDigest.isEqual(BYTE_ARRAY, wrapped));
  }

  @Test
  public void testWrapEmptyString() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.getObjectHeaders().put(KEY, "");
    byte[] wrapped = new ByteArrayFromObjectMetadata().withKey(KEY).wrap(msg);
    assertEquals(0, wrapped.length);
  }

}
