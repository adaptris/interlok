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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.XmlUtils;

@SuppressWarnings("deprecation")
public class ReplaceNodeTest {
  private static final String XPATH_ROOT_NODE = "/Root/Node";
  private static String DATA_A = "This is Some Data";
  private static String ORIGINAL = "<Root><Node>" + DATA_A + "</Node></Root>";

  private static final String DATA_B = "This is Some Other Data";
  private static String REPLACEMENT = "<Node>" + DATA_B + "</Node>";

  @BeforeEach
  public void setUp() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  public void testSetNamespaceContext() {
    ReplaceNode obj = new ReplaceNode();
    assertNull(obj.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    obj.setNamespaceContext(kvps);
    assertEquals(kvps, obj.getNamespaceContext());
    obj.setNamespaceContext(null);
    assertNull(obj.getNamespaceContext());
  }

  @Test
  public void testReplaceNode() throws Exception {
    ReplaceNode rn = new ReplaceNode(XPATH_ROOT_NODE);
    Document original = XmlHelper.createDocument(ORIGINAL);
    Document newDoc = XmlHelper.createDocument(REPLACEMENT);
    Document merged = rn.merge(original, newDoc);

    XmlUtils xml = new XmlUtils();
    xml.setSource(merged);
    assertNotNull(xml.getSingleTextItem(XPATH_ROOT_NODE));
    assertNotSame(DATA_A, xml.getSingleTextItem(XPATH_ROOT_NODE));
    assertEquals(DATA_B, xml.getSingleTextItem(XPATH_ROOT_NODE));
  }

  @Test
  public void testReplaceNodeDoesNotExist() throws Exception {
    ReplaceNode rn = new ReplaceNode("/Root/NonExistentNode");
    Document original = XmlHelper.createDocument(ORIGINAL);
    Document newDoc = XmlHelper.createDocument(REPLACEMENT);
    try {
      Document merged = rn.merge(original, newDoc);
      fail("merge should fail with non-existent node");
    }
    catch (Exception expected) {
      assertNotNull(expected.getMessage());
      assertTrue(expected.getMessage().matches(".*Failed to resolve.*"));
    }
  }
}
