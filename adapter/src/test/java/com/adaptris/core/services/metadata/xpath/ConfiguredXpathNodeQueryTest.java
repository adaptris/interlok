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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.XmlHelper;

@SuppressWarnings("deprecation")
public class ConfiguredXpathNodeQueryTest extends ConfiguredXpathQueryCase {

  public ConfiguredXpathNodeQueryTest(String testName) {
    super(testName);
  }

  @Override
  protected ConfiguredXpathNodeQuery create() {
    return new ConfiguredXpathNodeQuery();
  }

  private ConfiguredXpathNodeQuery init(ConfiguredXpathNodeQuery query, String xpathQuery) throws CoreException {
    query.setMetadataKey("result");
    query.setXpathQuery(xpathQuery);
    query.verify();
    return query;
  }

  public void testResolveXpath_EmptyResults_NotAllowed() throws Exception {
    ConfiguredXpathNodeQuery query = init(create(), "//@MissingAttribute");
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    try {
      Node result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testResolveXpath_EmptyResults_Allowed() throws Exception {
    ConfiguredXpathNodeQuery query = init(create(), "//@MissingAttribute");
    query.setAllowEmptyResults(Boolean.TRUE);
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    Node result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
    assertNull(result);
  }

  public void testResolveXpath_Attribute() throws Exception {
    ConfiguredXpathNodeQuery query = init(create(), "//@att");
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    Node result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
    assertNotNull(result);
  }


  public void testResolveXpath_NamespaceWithNamespaceContext() throws Exception {
    ConfiguredXpathNodeQuery query = init(create(), "/svrl:schematron-output/svrl:failed-assert");

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    StaticNamespaceContext ctx = new StaticNamespaceContext();
    Document doc = XmlHelper.createDocument(XML_WITH_NAMESPACE, ctx);
    Node result = query.resolveXpath(doc, ctx, query.createXpathQuery(msg));
    assertNotNull(result);
  }

}
