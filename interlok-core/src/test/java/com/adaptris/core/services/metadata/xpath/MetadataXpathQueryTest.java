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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.w3c.dom.Document;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;

@SuppressWarnings("deprecation")
public class MetadataXpathQueryTest extends MetadataXpathQueryCase {


  @Override
  protected MetadataXpathQuery create() {
    return new MetadataXpathQuery();
  }

  private MetadataXpathQuery init(MetadataXpathQuery query) throws CoreException {
    query.setMetadataKey("result");
    query.setXpathMetadataKey("xpathMetadataKey");
    query.verify();
    return query;
  }

  @Test
  public void testResolveXpath_EmptyResults_NotAllowed() throws Exception {
    MetadataXpathQuery query = init(create());
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "//@MissingAttribute");
    try {
      MetadataElement result = query.resolveXpath(doc, new XPath(), query.createXpathQuery(msg));
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testResolveXpath_EmptyResults_Allowed() throws Exception {
    MetadataXpathQuery query = init(create());
    query.setAllowEmptyResults(Boolean.TRUE);

    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "//@MissingAttribute");
    MetadataElement result = query.resolveXpath(doc, new XPath(), query.createXpathQuery(msg));
    assertEquals("", result.getValue());
  }

  @Test
  public void testResolveXpath_Attribute() throws Exception {
    MetadataXpathQuery query = init(create());

    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "//@att");
    MetadataElement result = query.resolveXpath(doc, new XPath(), query.createXpathQuery(msg));
  }

  @Test
  public void testResolveXpath_function() throws Exception {
    MetadataXpathQuery query = init(create());

    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "count(/message)");
    MetadataElement result = query.resolveXpath(doc, new XPath(), query.createXpathQuery(msg));
    assertEquals("1", result.getValue());
  }

  // Invalid test
  // Namedspaced document with non-namespace xpath never matches with SAXON
  // public void testResolveXpath_NamespaceNoNamespaceContext() throws Exception {
  // MetadataXpathQuery query = init(create());
  // Document doc = XmlHelper.createDocument(XML_WITH_NAMESPACE);
  // AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
  // msg.addMetadata("xpathMetadataKey", "count(/schematron-output/failed-assert)");
  // MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
  // assertEquals("2", result.getValue());
  // }

  @Test
  public void testResolveXpath_NamespaceWithNamespaceContext() throws Exception {
    MetadataXpathQuery query = init(create());

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    msg.addMetadata("xpathMetadataKey", "count(/svrl:schematron-output/svrl:failed-assert)");

    StaticNamespaceContext ctx = new StaticNamespaceContext();
    Document doc = XmlHelper.createDocument(XML_WITH_NAMESPACE, ctx);
    MetadataElement result = query.resolveXpath(doc, new XPath(ctx), query.createXpathQuery(msg));
    assertEquals("2", result.getValue());
  }

}
