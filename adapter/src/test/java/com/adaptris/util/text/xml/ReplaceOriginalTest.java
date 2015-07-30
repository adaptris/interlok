package com.adaptris.util.text.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;

public class ReplaceOriginalTest {
  private static final String XPATH_NEW_NODE = "/SomeOtherRoot/Node";
  private static final String XPATH_ORIGINAL_NODE = "/Root/OriginalNode";
  private static String DATA_A = "This is Some Data";
  private static String ORIGINAL = "<Root><OriginalNode>" + DATA_A + "</OriginalNode></Root>";

  private static final String DATA_B = "This is Some Other Data";
  private static String REPLACEMENT = "<SomeOtherRoot><Node>" + DATA_B + "</Node></SomeOtherRoot>";

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testReplaceOriginal() throws Exception {
    ReplaceOriginal rn = new ReplaceOriginal();
    Document merged = rn.merge(XmlHelper.createDocument(ORIGINAL), XmlHelper.createDocument(REPLACEMENT));

    XmlUtils xml = new XmlUtils();
    xml.setSource(merged);
    assertNull(xml.getSingleNode(XPATH_ORIGINAL_NODE));
    assertEquals(DATA_B, xml.getSingleTextItem(XPATH_NEW_NODE));
  }


}
