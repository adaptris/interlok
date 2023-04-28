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

package com.adaptris.core;

import static com.adaptris.core.AdaptrisMessageFactory.OVERRIDE_DEFAULT_MSGID_GEN_PROP;
import static com.adaptris.core.AdaptrisMessageFactory.OVERRIDE_DEFAULT_MSG_FACTORY_PROP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.PseudoRandomIdGenerator;

// Force the order of the tests.
// Because system properties.
// Also, upgrading to junit-jupiter with junit-pioneer (for setEnvironmentVariable) has
// some side-effects seem hard to explain, but potentially due to test ordering.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdaptrisMessageFactoryTest {

  @Order(1)
  @Test
  public void testGetDefaultInstance() {
    AdaptrisMessageFactory m1 = AdaptrisMessageFactory.getDefaultInstance();
    assertNotNull(m1);
    assertNotNull(m1.newMessage());
    AdaptrisMessageFactory m2 = AdaptrisMessageFactory.getDefaultInstance();
    assertNotNull(m2);
    assertEquals(m1, m2);
  }

  @Order(2)
  @Test
  public void testResolve_FromEnv() {
    String resolved_1 = AdaptrisMessageFactory.resolve("PATH", "testSystemPropertyKey", "myDefaultValue");
    assertNotSame("myDefaultValue", resolved_1);
  }

  @Order(3)
  @Test
  public void testResolve_FromSysProp() {
    GuidGenerator guid = new GuidGenerator();
    String propertyKey = guid.safeUUID();
    System.setProperty(propertyKey, "testSystemPropertyValue");
    String resolved = AdaptrisMessageFactory.resolve("NON_EXISTENT_VAR", propertyKey, "myDefaultValue");
    assertEquals("testSystemPropertyValue", resolved);
  }

  @Order(4)
  @Test
  public void testResolve_UsesDefault() {
    String resolved = AdaptrisMessageFactory.resolve("NON_EXISTENT_VAR", new GuidGenerator().safeUUID(), "myDefaultValue");
    assertEquals("myDefaultValue", resolved);
  }

  @Order(5)
  @Test
  public void test_10_CreateMessageFactory_Override() {
    System.setProperty(OVERRIDE_DEFAULT_MSG_FACTORY_PROP, StubMessageFactory.class.getCanonicalName());
    AdaptrisMessageFactory factory = AdaptrisMessageFactory.createDefaultFactory();
    assertEquals(StubMessageFactory.class, factory.getClass());
    // Because system properties, set it to a sensible default
    System.setProperty(OVERRIDE_DEFAULT_MSG_FACTORY_PROP, DefaultMessageFactory.class.getCanonicalName());
  }

  @Order(6)
  @Test
  public void test_20_CreateMessageFactory_NonExistent() {
    System.setProperty(OVERRIDE_DEFAULT_MSG_FACTORY_PROP,"does.not.exist.MessageFactory");
    Assertions.assertThrows(RuntimeException.class, () -> {
      AdaptrisMessageFactory.createDefaultFactory();
    });
    // Because system properties, set it to a sensible default
    System.setProperty(OVERRIDE_DEFAULT_MSG_FACTORY_PROP, DefaultMessageFactory.class.getCanonicalName());
  }

  @Order(7)
  @Test
  public void test_30_CreateUidGenerator_Override() {
    System.setProperty(OVERRIDE_DEFAULT_MSGID_GEN_PROP, PseudoRandomIdGenerator.class.getCanonicalName());
    IdGenerator guid = AdaptrisMessageFactory.createDefaultIdGenerator();
    assertEquals(PseudoRandomIdGenerator.class, guid.getClass());
    // Because system properties, set it to a sensible default
    System.setProperty(OVERRIDE_DEFAULT_MSGID_GEN_PROP, GuidGenerator.class.getCanonicalName());
  }

  @Order(8)
  @Test
  public void test_40_CreateUidGenerator_NonExistent() {
    System.setProperty(OVERRIDE_DEFAULT_MSGID_GEN_PROP,"does.not.exist.guid.Generator");
    Assertions.assertThrows(RuntimeException.class, () -> {
      AdaptrisMessageFactory.createDefaultIdGenerator();
    });
    // Because system properties, set it to a sensible default
    System.setProperty(OVERRIDE_DEFAULT_MSGID_GEN_PROP, GuidGenerator.class.getCanonicalName());
  }

}
