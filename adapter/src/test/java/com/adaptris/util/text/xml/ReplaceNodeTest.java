package com.adaptris.util.text.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.XmlUtils;

public class ReplaceNodeTest {
  private static final String XPATH_ROOT_NODE = "/Root/Node";
  private static String DATA_A = "This is Some Data";
  private static String ORIGINAL = "<Root><Node>" + DATA_A + "</Node></Root>";

  private static final String DATA_B = "This is Some Other Data";
  private static String REPLACEMENT = "<Node>" + DATA_B + "</Node>";

  @Before
  public void setUp() throws Exception {
  }

  @After
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
