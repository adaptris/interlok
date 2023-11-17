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

package com.adaptris.interlok.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

public class DefaultSerializableMessageTest {

  @BeforeEach
  public void setUp() throws Exception {

  }

  @AfterEach
  public void tearDown() throws Exception {

  }


  @Test
  public void testConvenienceMethods(TestInfo info) throws Exception {
    Map<String, String> hdrs = new HashMap<>();
    hdrs.put(info.getDisplayName(), info.getDisplayName());


    DefaultSerializableMessage msg =
        new DefaultSerializableMessage().withUniqueId(info.getDisplayName()).withMessageHeaders(hdrs)
        .withPayload(info.getDisplayName()).withPayloadEncoding(info.getDisplayName())
        .withNextServiceId(info.getDisplayName());
    assertEquals(info.getDisplayName(), msg.getUniqueId());
    assertEquals(info.getDisplayName(), msg.getContent());
    assertEquals(info.getDisplayName(), msg.getContentEncoding());
    assertEquals(1, msg.getMessageHeaders().size());
    assertEquals(info.getDisplayName(), msg.getMessageHeaders().get(info.getDisplayName()));
    assertEquals(info.getDisplayName(), msg.getNextServiceId());
  }


  @Test
  public void testAddMessageHeader(TestInfo info) {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();
    assertEquals(0, msg.getMessageHeaders().size());
    msg.addMessageHeader(info.getDisplayName(), info.getDisplayName());
    assertEquals(1, msg.getMessageHeaders().size());
    assertEquals(info.getDisplayName(), msg.getMessageHeaders().get(info.getDisplayName()));
  }

  @Test
  public void testRemoveMessageHeader(TestInfo info) {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();

    Map<String, String> hdrs = new HashMap<>();
    hdrs.put(info.getDisplayName(), info.getDisplayName());

    msg.setMessageHeaders(hdrs);
    assertEquals(1, msg.getMessageHeaders().size());
    assertEquals(info.getDisplayName(), msg.getMessageHeaders().get(info.getDisplayName()));

    msg.removeMessageHeader(info.getDisplayName());
    assertEquals(0, msg.getMessageHeaders().size());
    assertNull(msg.getMessageHeaders().get(info.getDisplayName()));

  }

  @Test
  public void testMessageHeaders(TestInfo info) {

    Map<String, String> hdrs = new HashMap<>();
    hdrs.put(info.getDisplayName(), info.getDisplayName());

    DefaultSerializableMessage msg = new DefaultSerializableMessage();
    msg.setMessageHeaders(hdrs);
    assertEquals(1, msg.getMessageHeaders().size());
    assertEquals(info.getDisplayName(), msg.getMessageHeaders().get(info.getDisplayName()));

    msg.setMessageHeaders((Map<String, String>) null);
    assertEquals(0, msg.getMessageHeaders().size());
  }

  @Test
  public void testMessageHeaders_Properties(TestInfo info) {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();

    Properties hdrs = new Properties();
    hdrs.setProperty(info.getDisplayName(), info.getDisplayName());
    msg.withHeadersFromProperties(hdrs);
    assertEquals(1, msg.getMessageHeaders().size());
    assertEquals(info.getDisplayName(), msg.getMessageHeaders().get(info.getDisplayName()));

    assertNotSame(hdrs, msg.getMessageHeaders());

    msg.withHeadersFromProperties((Properties) null);
    assertEquals(0, msg.getMessageHeaders().size());
  }


  @Test
  public void testUniqueId(TestInfo info) {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();
    assertNotNull(msg.getUniqueId());
    msg.setUniqueId(info.getDisplayName());
    assertEquals(info.getDisplayName(), msg.getUniqueId());
  }

  @Test
  public void testPayload(TestInfo info) {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();
    assertNull(msg.getContent());
    msg.setContent(info.getDisplayName());
    assertEquals(info.getDisplayName(), msg.getContent());
  }

  @Test
  public void testPayloadEncoding(TestInfo info) {
    DefaultSerializableMessage msg = new DefaultSerializableMessage();
    assertNull(msg.getContentEncoding());
    msg.setContentEncoding(info.getDisplayName());
    assertEquals(info.getDisplayName(), msg.getContentEncoding());
  }

  @Test
  public void testSerialize(TestInfo info) throws Exception {
    Map<String, String> hdrs = new HashMap<>();
    hdrs.put(info.getDisplayName(), info.getDisplayName());

    DefaultSerializableMessage input = new DefaultSerializableMessage().withUniqueId(info.getDisplayName())
        .withMessageHeaders(hdrs).withPayload(info.getDisplayName()).withPayloadEncoding(info.getDisplayName())
        .withNextServiceId(info.getDisplayName());

    DefaultSerializableMessage msg = roundTrip(input);
    assertEquals(info.getDisplayName(), msg.getUniqueId());
    assertEquals(info.getDisplayName(), msg.getContent());
    assertEquals(info.getDisplayName(), msg.getContentEncoding());
    assertEquals(1, msg.getMessageHeaders().size());
    assertEquals(info.getDisplayName(), msg.getMessageHeaders().get(info.getDisplayName()));
    assertEquals(info.getDisplayName(), msg.getNextServiceId());

  }

  private DefaultSerializableMessage roundTrip(DefaultSerializableMessage input) throws Exception {
    XStream m = new XStream(new PureJavaReflectionProvider());
    m.allowTypesByWildcard(new String[] { "com.adaptris.**" });
    m.processAnnotations(DefaultSerializableMessage.class);
    return (DefaultSerializableMessage) m.fromXML(m.toXML(input));
  }

}
