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

package com.adaptris.core.services.metadata.xpath;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings("deprecation")
public class ConfiguredXpathQueryTest extends ConfiguredXpathQueryCase {


  @Override
  protected ConfiguredXpathQuery create() {
    return new ConfiguredXpathQuery();
  }

  private ConfiguredXpathQuery init(ConfiguredXpathQuery query, String xpathQuery) throws CoreException {
    query.setMetadataKey("result");
    query.setXpathQuery(xpathQuery);
    query.verify();
    return query;
  }

  @Test
  public void testResolveXpath_EmptyResults_NotAllowed() throws Exception {
    ConfiguredXpathQuery query = init(create(), "//@MissingAttribute");
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    try {
      MetadataElement result = query.resolveXpath(doc, new XPath(), query.createXpathQuery(msg));
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testResolveXpath_EmptyResults_Allowed() throws Exception {
    ConfiguredXpathQuery query = init(create(), "//@MissingAttribute");
    query.setAllowEmptyResults(Boolean.TRUE);
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    MetadataElement result = query.resolveXpath(doc, new XPath(), query.createXpathQuery(msg));
    assertEquals("", result.getValue());
  }

  @Test
  public void testResolveXpath_Attribute() throws Exception {
    ConfiguredXpathQuery query = init(create(), "//@att");
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    MetadataElement result = query.resolveXpath(doc, new XPath(), query.createXpathQuery(msg));
  }

  @Test
  public void testResolveXpath_function() throws Exception {
    ConfiguredXpathQuery query = init(create(), "count(/message)");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    Document doc = XmlHelper.createDocument(XML);
    MetadataElement result = query.resolveXpath(doc, new XPath(), query.createXpathQuery(msg));
    assertEquals("1", result.getValue());
  }

  // Invalid test
  // Namedspaced document with non-namespace xpath never matches with SAXON
  // public void testResolveXpath_NamespaceNoNamespaceContext() throws Exception {
  // ConfiguredXpathQuery query = init(create(), "count(/schematron-output/failed-assert)");
  // Document doc = XmlHelper.createDocument(XML_WITH_NAMESPACE);
  // AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE, "UTF-8");
  //
  // MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
  // assertEquals("2", result.getValue());
  // }

  @Test
  public void testResolveXpath_NamespaceWithNamespaceContext() throws Exception {
    ConfiguredXpathQuery query = init(create(), "count(/svrl:schematron-output/svrl:failed-assert)");

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    StaticNamespaceContext ctx = new StaticNamespaceContext();
    Document doc = XmlHelper.createDocument(XML_WITH_NAMESPACE, ctx);
    MetadataElement result = query.resolveXpath(doc, new XPath(ctx), query.createXpathQuery(msg));
    assertEquals("2", result.getValue());
  }

  @Test
  public void testMessageResolveXpath() throws Exception {
    ConfiguredXpathQuery query = init(create(), "//message/extra[%message{which-extra}]");
    query.setAllowEmptyResults(Boolean.FALSE);
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);

    msg.addMetadata("which-extra", "1");
    MetadataElement result = query.resolveXpath(doc, new XPath(), query.createXpathQuery(msg));
    assertEquals("one", result.getValue());

    msg.addMetadata("which-extra", "2");
    result = query.resolveXpath(doc, new XPath(), query.createXpathQuery(msg));
    assertEquals("two", result.getValue());

    msg.addMetadata("which-extra", "3");
    result = query.resolveXpath(doc, new XPath(), query.createXpathQuery(msg));
    assertEquals("three", result.getValue());
  }

  @Test
  public void testResolveNodesAsString() throws Exception {
    ConfiguredXpathQuery query = new ConfiguredXpathQuery("result", "//source-id");
    query.setAsXmlString(true);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    MetadataElement result = query.resolveXpath(XmlHelper.createDocument(msg.getContent()), new XPath(), query.createXpathQuery(msg));
    assertEquals("<source-id>partnera</source-id>", result.getValue().strip());
  }
}
