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

package com.adaptris.util.text.xml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.adaptris.util.KeyValuePair;

public class MappedResolverTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  private static final String PUBLIC_ID = "-//Mort Bay Consulting//DTD Configure//EN";
  private static final String SYSTEM_ID = "http://www.eclipse.org/jetty/configure.dtd";
  private static final String KEY_MAPPED_URL = "resolver.mapped.url";

  @Test
  public void testEntityResolver_Mapping_Absolute() throws Exception {
    MappedResolver resolver = new MappedResolver();
    resolver.getMappings().add(new KeyValuePair(SYSTEM_ID, PROPERTIES.getProperty(KEY_MAPPED_URL)));
    assertNotNull(resolver.resolveEntity(PUBLIC_ID, SYSTEM_ID));
    assertEquals(1, resolver.size());
    assertNotNull(resolver.resolveEntity(PUBLIC_ID, SYSTEM_ID));
    assertEquals(1, resolver.size());
  }

  @Test
  public void testEntityResolver_NoMapping() throws Exception {
    MappedResolver resolver = new MappedResolver();
    assertNotNull(resolver.resolveEntity(PUBLIC_ID, SYSTEM_ID));
    assertEquals(1, resolver.size());
    assertNotNull(resolver.resolveEntity(PUBLIC_ID, SYSTEM_ID));
    assertEquals(1, resolver.size());
  }

}
