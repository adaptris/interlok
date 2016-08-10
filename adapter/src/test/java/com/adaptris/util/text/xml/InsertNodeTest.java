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
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.XmlUtils;

@SuppressWarnings("deprecation")
public class InsertNodeTest {
  private static final String XPATH_NEW_NODE = "/Root/NewNode";
  private static final String XPATH_ORIGINAL_NODE = "/Root/OriginalNode";
  private static final String XPATH_ROOT = "/Root";
  private static String DATA_A = "This is Some Data";
  private static String ORIGINAL = "<Root><OriginalNode>" + DATA_A + "</OriginalNode></Root>";

  private static final String DATA_B = "This is Some Other Data";
  private static String REPLACEMENT = "<NewNode>" + DATA_B + "</NewNode>";

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testSetNamespaceContext() {
    InsertNode obj = new InsertNode();
    assertNull(obj.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    obj.setNamespaceContext(kvps);
    assertEquals(kvps, obj.getNamespaceContext());
    obj.setNamespaceContext(null);
    assertNull(obj.getNamespaceContext());
  }

  @Test
  public void testInsertNode() throws Exception {
    InsertNode rn = new InsertNode(XPATH_ROOT);
    Document merged = rn.merge(XmlHelper.createDocument(ORIGINAL), XmlHelper.createDocument(REPLACEMENT));

    XmlUtils xml = new XmlUtils();
    xml.setSource(merged);
    assertNotNull(xml.getSingleTextItem(XPATH_ORIGINAL_NODE));
    assertEquals(DATA_A, xml.getSingleTextItem(XPATH_ORIGINAL_NODE));
    assertEquals(DATA_B, xml.getSingleTextItem(XPATH_NEW_NODE));
  }

  @Test
  public void testInsertNode_NodeDoesNotExist() throws Exception {
    InsertNode rn = new InsertNode("/Root/AnotherNode");
    Document merged = rn.merge(XmlHelper.createDocument(ORIGINAL), XmlHelper.createDocument(REPLACEMENT));
    XmlUtils xml = new XmlUtils();
    xml.setSource(merged);
    assertNotNull(xml.getSingleTextItem(XPATH_ORIGINAL_NODE));
    assertEquals(DATA_A, xml.getSingleTextItem(XPATH_ORIGINAL_NODE));
    assertEquals(DATA_B, xml.getSingleTextItem("/Root/AnotherNode/NewNode"));
  }

  @Test
  public void testInsertNode_NodeIsRootElementAndDoesNotExist() throws Exception {
    InsertNode rn = new InsertNode("/TopLevelItem");
    try {
      Document merged = rn.merge(XmlHelper.createDocument(ORIGINAL), XmlHelper.createDocument(REPLACEMENT));
      fail("merge should fail with top level node that doesn't exist");
    }
    catch (Exception expected) {
      assertEquals("Failed to create node [/TopLevelItem]", expected.getMessage());
    }
  }

  @Test
  public void testInsertNode_NodeIs_Slash() throws Exception {
    InsertNode rn = new InsertNode("/");
    try {
      Document merged = rn.merge(XmlHelper.createDocument(ORIGINAL), XmlHelper.createDocument(REPLACEMENT));
      fail("merge should fail with top level node that doesn't exist");
    }
    catch (Exception expected) {
      assertEquals("Invalid xpath-to-parent-node [/]", expected.getMessage());
    }
  }

  @Test
  public void testInsertNode_NoXpath() throws Exception {
    InsertNode rn = new InsertNode();
    try {
      Document merged = rn.merge(XmlHelper.createDocument(ORIGINAL), XmlHelper.createDocument(REPLACEMENT));
      fail("merge should fail with top level node that doesn't exist");
    }
    catch (Exception expected) {
      assertEquals("No parent node configured", expected.getMessage());
    }
  }

}
