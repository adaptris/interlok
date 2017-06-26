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
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ResolverTest {

  private static final String PUBLIC_ID = "-//Mort Bay Consulting//DTD Configure//EN";
  private static final String SYSTEM_ID = "http://www.eclipse.org/jetty/configure.dtd";
  private static final String HREF_RELATIVE = "./configure.dtd";
  private static final String HREF = "http://www.eclipse.org/jetty/configure.dtd";
  private static final String BASE = "http://www.eclipse.org/jetty/";
  private static final String UNRESOLVEABLE = "unhandledProtocol://zzlc.zzlc.zzlc/configure.dtd";

  @Test
  public void testEntityResolver() throws Exception {
    Resolver resolver = new Resolver();
    assertNotNull(resolver.resolveEntity(PUBLIC_ID, SYSTEM_ID));
    assertEquals(1, resolver.size());
    assertNotNull(resolver.resolveEntity(PUBLIC_ID, SYSTEM_ID));
    assertEquals(1, resolver.size());
  }

  @Test
  public void testEntityResolver_Unresolvable() throws Exception {
    Resolver resolver = new Resolver();
    assertNull(resolver.resolveEntity(PUBLIC_ID, UNRESOLVEABLE));
    assertEquals(0, resolver.size());
  }

  @Test
  public void testResolveURI() throws Exception {
    Resolver resolver = new Resolver();
    resolver.setAdditionalDebug(Boolean.TRUE);
    assertNotNull(resolver.resolve(HREF_RELATIVE, BASE));
    assertEquals(1, resolver.size());
    assertNotNull(resolver.resolve(HREF_RELATIVE, BASE));
    assertEquals(1, resolver.size());
  }

  @Test
  public void testResolveURI_Absolute() throws Exception {
    Resolver resolver = new Resolver();
    assertNotNull(resolver.resolve(HREF, BASE));
    assertEquals(1, resolver.size());
    assertNotNull(resolver.resolve(HREF, BASE));
    assertEquals(1, resolver.size());
  }

  @Test
  public void testResolveURI_Unresolvable() throws Exception {
    Resolver resolver = new Resolver();
    assertNull(resolver.resolve(PUBLIC_ID, UNRESOLVEABLE));
    assertEquals(0, resolver.size());
  }

  @Test
  public void testCacheSize() throws Exception {
    Resolver resolver = new Resolver();
    assertNull(resolver.getMaxDestinationCacheSize());
    assertEquals(50, resolver.maxDestinationCacheSize());
    resolver.setMaxDestinationCacheSize(0);
    assertEquals(Integer.valueOf(0), resolver.getMaxDestinationCacheSize());
    assertEquals(0, resolver.maxDestinationCacheSize());

    assertNotNull(resolver.resolveEntity(PUBLIC_ID, SYSTEM_ID));
    assertEquals(0, resolver.size());
    assertNotNull(resolver.resolveEntity(PUBLIC_ID, SYSTEM_ID));
    assertEquals(0, resolver.size());
  }

}
