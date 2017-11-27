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
public class MultiItemConfiguredXpathQueryTest extends ConfiguredXpathQueryCase {

  private static final String XML_WITH_EMPTY_NODES = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" + 
      "<root>\n" + 
      "  <segment_PIX>\n" + 
      "    <segment_Contents>\n" + 
      "      <record_>\n" + 
      "        <PXREF1/>\n" + 
      "      </record_>\n" + 
      "      <record_>\n" + 
      "        <PXREF1/>\n" + 
      "      </record_>\n" + 
      "      <record_>\n" + 
      "        <PXREF1/>\n" + 
      "      </record_>\n" + 
      "      <record_>\n" + 
      "        <PXREF1>91/01</PXREF1>\n" + 
      "      </record_>\n" + 
      "      <record_>\n" + 
      "        <PXREF1>91/01</PXREF1>\n" + 
      "      </record_>\n" + 
      "      <record_>\n" + 
      "        <PXREF1>91/01</PXREF1>\n" + 
      "      </record_>\n" + 
      "    </segment_Contents>\n" + 
      "  </segment_PIX>\n" + 
      "</root>";
  
  private static final String XPATH_EMPTY_NODES = "/root/segment_PIX/segment_Contents/record_/PXREF1";

  public MultiItemConfiguredXpathQueryTest(String testName) {
    super(testName);
  }

  @Override
  protected MultiItemConfiguredXpathQuery create() {
    return new MultiItemConfiguredXpathQuery();
  }

  private MultiItemConfiguredXpathQuery init(MultiItemConfiguredXpathQuery query, String xpath) throws CoreException {
    query.setMetadataKey("result");
    query.setXpathQuery(xpath);
    query.verify();
    return query;
  }

  public void testSetSeparator() {
    MultiItemConfiguredXpathQuery query = create();
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
    MultiItemConfiguredXpathQuery query = init(create(), "//@MissingAttribute");
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    try {
      MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testResolveXpath_EmptyResults_Allowed() throws Exception {
    MultiItemConfiguredXpathQuery query = init(create(), "//@MissingAttribute");

    query.setAllowEmptyResults(Boolean.TRUE);
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
    assertEquals("", result.getValue());
  }

  public void testResolveXpath_EmptyResults_Allowed_EmptyValues() throws Exception {
    MultiItemConfiguredXpathQuery query = init(create(), XPATH_EMPTY_NODES);

    query.setAllowEmptyResults(Boolean.TRUE);
    Document doc = XmlHelper.createDocument(XML_WITH_EMPTY_NODES);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_EMPTY_NODES);
    MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
    assertEquals("|||91/01|91/01|91/01", result.getValue());
  }


  public void testResolveXpath() throws Exception {
    MultiItemConfiguredXpathQuery query = init(create(), "//extra[@att='multi']");
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
    assertEquals("two|three", result.getValue());
  }

}
