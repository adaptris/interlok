/*
 * Copyright 2017 Adaptris Ltd.
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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class SimpleNamespaceContextTest extends SimpleNamespaceContext {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCreateKeyValuePairSet() {
    assertNull(SimpleNamespaceContext.create(null));
    assertNull(SimpleNamespaceContext.create(new KeyValuePairSet()));
    assertNotNull(SimpleNamespaceContext.create(createNamespaceEntries()));
    assertNotNull(SimpleNamespaceContext.create(createWithDefaultEntries()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidNamespace() throws Exception {
    SimpleNamespaceContext.create(createBrokenEntries());
  }

  @Test
  public void testCreateKeyValuePairSetAdaptrisMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertNull(SimpleNamespaceContext.create(null, msg));
    assertNull(SimpleNamespaceContext.create(new KeyValuePairSet(), msg));
    NamespaceContext ctx = SimpleNamespaceContext.create(createNamespaceEntries());
    msg.addObjectHeader(SimpleNamespaceContext.class.getCanonicalName(), ctx);
    NamespaceContext ctx2 = SimpleNamespaceContext.create(null, msg);
    assertEquals(ctx, ctx2);
    NamespaceContext ctx3 = SimpleNamespaceContext.create(createNamespaceEntries(), msg);
    assertNotSame(ctx, ctx3);
  }

  @Test
  public void testNamespaceURI() throws Exception {
    NamespaceContext ctx = SimpleNamespaceContext.create(createNamespaceEntries());
    assertEquals(XMLConstants.NULL_NS_URI, ctx.getNamespaceURI(null));
    assertEquals(XMLConstants.NULL_NS_URI, ctx.getNamespaceURI("blahblah"));
    assertEquals("http://www.w3.org/2001/XMLSchema", ctx.getNamespaceURI("xsd"));
  }

  @Test
  public void testGetPrefix() throws Exception {
    NamespaceContext ctx = SimpleNamespaceContext.create(createNamespaceEntries());
    assertNull(ctx.getPrefix(null));
    assertNull(ctx.getPrefix("http://www.w3.org/XML/2001/namespace"));
    assertEquals("xsd", ctx.getPrefix("http://www.w3.org/2001/XMLSchema"));
  }

  @Test
  public void testGetPrefixes() throws Exception {
    NamespaceContext ctx = SimpleNamespaceContext.create(createNamespaceEntries());
    assertNull(ctx.getPrefixes(null));
    assertNotNull(ctx.getPrefixes("http://www.w3.org/2001/XMLSchema"));
    assertNull(ctx.getPrefixes("http://www.w3.org/XML/2001/namespace"));
  }

  static KeyValuePairSet createNamespaceEntries() {
    KeyValuePairSet result = new KeyValuePairSet();
    result.add(new KeyValuePair("xsd", "http://www.w3.org/2001/XMLSchema"));
    result.add(new KeyValuePair("xs", "http://www.w3.org/2001/XMLSchema"));
    return result;
  }

  private static KeyValuePairSet createWithDefaultEntries() {
    KeyValuePairSet result = createNamespaceEntries();
    result.add(new KeyValuePair(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI));
    result.add(new KeyValuePair(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI));
    return result;
  }

  private static KeyValuePairSet createBrokenEntries() {
    KeyValuePairSet result = createNamespaceEntries();
    // XmlConstants.XML_NS_URI == http://www.w3.org/XML/1998/namespace
    // so this should now fail.
    result.add(new KeyValuePair("xml", "http://www.w3.org/XML/2001/namespace"));
    return result;
  }
}
