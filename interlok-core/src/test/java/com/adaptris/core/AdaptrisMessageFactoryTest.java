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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;

import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.PseudoRandomIdGenerator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Force the order of the tests.
// Because system properties.
// Also, upgrading to junit-jupiter with junit-pioneer (for setEnvironmentVariable) has
// some side-effects seem hard to explain, but potentially due to test ordering.
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdaptrisMessageFactoryTest {

  @Test
  public void testGetDefaultInstance() {
    AdaptrisMessageFactory m1 = AdaptrisMessageFactory.getDefaultInstance();
    assertNotNull(m1);
    assertNotNull(m1.newMessage());
    AdaptrisMessageFactory m2 = AdaptrisMessageFactory.getDefaultInstance();
    assertNotNull(m2);
    assertEquals(m1, m2);
  }

  @Test
  public void testResolve_FromEnv() {
    String resolved_1 = AdaptrisMessageFactory.resolve("PATH", "testSystemPropertyKey", "myDefaultValue");
    assertNotSame("myDefaultValue", resolved_1);
  }

  @Test
  public void testResolve_FromSysProp() {
    GuidGenerator guid = new GuidGenerator();
    String propertyKey = guid.safeUUID();
    System.setProperty(propertyKey, "testSystemPropertyValue");
    String resolved = AdaptrisMessageFactory.resolve("NON_EXISTENT_VAR", propertyKey, "myDefaultValue");
    assertEquals("testSystemPropertyValue", resolved);
  }

  @Test
  public void testResolve_UsesDefault() {
    String resolved = AdaptrisMessageFactory.resolve("NON_EXISTENT_VAR", new GuidGenerator().safeUUID(), "myDefaultValue");
    assertEquals("myDefaultValue", resolved);
  }

  @Test
  public void test_10_CreateMessageFactory_Override() {
    System.setProperty(OVERRIDE_DEFAULT_MSG_FACTORY_PROP, StubMessageFactory.class.getCanonicalName());
    AdaptrisMessageFactory factory = AdaptrisMessageFactory.createDefaultFactory();
    assertEquals(StubMessageFactory.class, factory.getClass());
    // Because system properties, set it to a sensible default
    System.setProperty(OVERRIDE_DEFAULT_MSG_FACTORY_PROP, DefaultMessageFactory.class.getCanonicalName());
  }

  @Test
  public void test_20_CreateMessageFactory_NonExistent() {
    System.setProperty(OVERRIDE_DEFAULT_MSG_FACTORY_PROP,"does.not.exist.MessageFactory");
    assertThrows(RuntimeException.class, AdaptrisMessageFactory::createDefaultFactory);
    // Because system properties, set it to a sensible default
    System.setProperty(OVERRIDE_DEFAULT_MSG_FACTORY_PROP, DefaultMessageFactory.class.getCanonicalName());
  }

  @Test
  public void test_30_CreateUidGenerator_Override() {
    System.setProperty(OVERRIDE_DEFAULT_MSGID_GEN_PROP, PseudoRandomIdGenerator.class.getCanonicalName());
    IdGenerator guid = AdaptrisMessageFactory.createDefaultIdGenerator();
    assertEquals(PseudoRandomIdGenerator.class, guid.getClass());
    // Because system properties, set it to a sensible default
    System.setProperty(OVERRIDE_DEFAULT_MSGID_GEN_PROP, GuidGenerator.class.getCanonicalName());
  }

  @Test
  public void test_40_CreateUidGenerator_NonExistent() {
    System.setProperty(OVERRIDE_DEFAULT_MSGID_GEN_PROP,"does.not.exist.guid.Generator");
    assertThrows(RuntimeException.class, AdaptrisMessageFactory::createDefaultIdGenerator);
    // Because system properties, set it to a sensible default
    System.setProperty(OVERRIDE_DEFAULT_MSGID_GEN_PROP, GuidGenerator.class.getCanonicalName());
  }

}
