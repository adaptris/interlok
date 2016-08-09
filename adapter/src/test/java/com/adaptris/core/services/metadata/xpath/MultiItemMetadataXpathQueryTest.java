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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.XmlHelper;

@SuppressWarnings("deprecation")
public class MultiItemMetadataXpathQueryTest extends MetadataXpathQueryCase {

  public MultiItemMetadataXpathQueryTest(String testName) {
    super(testName);
  }

  @Override
  protected MultiItemMetadataXpathQuery create() {
    return new MultiItemMetadataXpathQuery();
  }

  private MultiItemMetadataXpathQuery init(MultiItemMetadataXpathQuery query) throws CoreException {
    query.setMetadataKey("result");
    query.setXpathMetadataKey("xpathMetadataKey");
    query.verify();
    return query;
  }

  public void testSetSeparator() {
    MultiItemMetadataXpathQuery query = create();
    assertEquals("|", query.getSeparator());
    query.setSeparator(",");
    assertEquals(",", query.getSeparator());
    query.setSeparator("");
    assertEquals("", query.getSeparator());
    try {
      query.setSeparator(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals("", query.getSeparator());
  }

  public void testResolveXpath_EmptyResults_NotAllowed() throws Exception {
    MultiItemMetadataXpathQuery query = init(create());
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "//@MissingAttribute");
    try {
      MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testResolveXpath_EmptyResults_Allowed() throws Exception {
    MultiItemMetadataXpathQuery query = init(create());

    query.setAllowEmptyResults(Boolean.TRUE);
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "//@MissingAttribute");

    MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
    assertEquals("", result.getValue());
  }

  public void testResolveXpath() throws Exception {
    MultiItemMetadataXpathQuery query = init(create());
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "//extra[@att='multi']");
    MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
    assertEquals("two|three", result.getValue());
  }

}
