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
public class MetadataXpathNodeQueryTest extends MetadataXpathQueryCase {

  public MetadataXpathNodeQueryTest(String testName) {
    super(testName);
  }

  @Override
  protected MetadataXpathNodeQuery create() {
    return new MetadataXpathNodeQuery();
  }

  private MetadataXpathNodeQuery init(MetadataXpathNodeQuery query) throws CoreException {
    query.setMetadataKey("result");
    query.setXpathMetadataKey("xpathMetadataKey");
    query.verify();
    return query;
  }

  public void testResolveXpath_EmptyResults_NotAllowed() throws Exception {
    MetadataXpathNodeQuery query = init(create());
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "//@MissingAttribute");
    try {
      Node result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testResolveXpath_EmptyResults_Allowed() throws Exception {
    MetadataXpathNodeQuery query = init(create());
    query.setAllowEmptyResults(Boolean.TRUE);

    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "//@MissingAttribute");
    Node result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
    assertNull(result);
  }

  public void testResolveXpath_Attribute() throws Exception {
    MetadataXpathNodeQuery query = init(create());

    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "//@att");
    Node result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
  }

  public void testResolveXpath_NamespaceWithNamespaceContext() throws Exception {
    MetadataXpathNodeQuery query = init(create());

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    msg.addMetadata("xpathMetadataKey", "/svrl:schematron-output/svrl:failed-assert");

    StaticNamespaceContext ctx = new StaticNamespaceContext();
    Document doc = XmlHelper.createDocument(XML_WITH_NAMESPACE, ctx);
    Node result = query.resolveXpath(doc, ctx, query.createXpathQuery(msg));
    assertNotNull(result);
  }

}
