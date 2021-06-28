package com.adaptris.core.services.metadata.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;

public class XpathQueryHelperTest {

  @Test
  public void testResolveSingleTextItem() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc =
        XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);

    assertNotNull(XpathQueryHelper.resolveSingleTextItem(doc, xpathToUse, "//source-id", false));
    assertEquals("partnera",
        XpathQueryHelper.resolveSingleTextItem(doc, xpathToUse, "//source-id", false));
  }

  @Test(expected = CoreException.class)
  public void testResolveSingleTextItem_Missing_AllowEmptyFalse() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc = XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);
    XpathQueryHelper.resolveSingleTextItem(doc, xpathToUse, "//@MissingAttribute", false);
  }

  @Test
  public void testResolveSingleTextItem_Missing_AllowEmptyTrue() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc = XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);
    assertEquals("",
        XpathQueryHelper.resolveSingleTextItem(doc, xpathToUse, "//@MissingAttribute", true));
  }

  @Test
  public void testResolveMultipleTextItems() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc = XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);

    assertNotNull(
        XpathQueryHelper.resolveMultipleTextItems(doc, xpathToUse, "//extra", false, "|"));
    assertEquals("one|two|three",
        XpathQueryHelper.resolveMultipleTextItems(doc, xpathToUse, "//extra", false, "|"));
  }

  @Test(expected = CoreException.class)
  public void testResolveMultipleTextItems_Missing_AllowEmptyFalse() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc = XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);
    XpathQueryHelper.resolveMultipleTextItems(doc, xpathToUse, "//@MissingAttribute", false, "|");
  }

  @Test
  public void resolveMultipleTextItems_Missing_AllowEmptyTrue() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc = XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);
    assertEquals("",
        XpathQueryHelper.resolveMultipleTextItems(doc, xpathToUse, "//@MissingAttribute", true,
            "|"));
  }


  @Test
  public void testResolveSingleNode() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc = XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);

    assertNotNull(XpathQueryHelper.resolveSingleNode(doc, xpathToUse, "//source-id", false));
  }

  @Test(expected = CoreException.class)
  public void testResolveSingleNode_Missing_AllowEmptyFalse() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc = XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);
    XpathQueryHelper.resolveSingleNode(doc, xpathToUse, "//@MissingAttribute", false);
  }

  @Test
  public void resolveSingleNode_Missing_AllowEmptyTrue() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc = XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);
    assertNull(XpathQueryHelper.resolveSingleNode(doc, xpathToUse, "//@MissingAttribute", true));
  }


  @Test
  public void testResolveMultipleNode() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc = XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);

    NodeList nodelist = XpathQueryHelper.resolveNodeList(doc, xpathToUse, "//extra", false);
    assertNotNull(nodelist);
    assertEquals(3, nodelist.getLength());
  }

  @Test(expected = CoreException.class)
  public void testResolveMultipleNode_Missing_AllowEmptyFalse() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc = XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);
    XpathQueryHelper.resolveNodeList(doc, xpathToUse, "//@MissingAttribute", false);
  }

  @Test
  public void testResolveMultipleNode_Missing_AllowEmptyTrue() throws Exception {
    DocumentBuilderFactoryBuilder builder = DocumentBuilderFactoryBuilder.newInstance();
    Document doc = XmlHelper.createDocument(XpathQueryCase.XML, builder);
    XPath xpathToUse = XPath.newXPathInstance(builder, null);
    NodeList nodelist =
        XpathQueryHelper.resolveNodeList(doc, xpathToUse, "//@MissingAttribute", true);
    assertNotNull(nodelist);
    assertEquals(0, nodelist.getLength());
  }

  @Test
  public void testIsEmpty_NodeList() throws Exception {
    NodeList listWithData = Mockito.mock(NodeList.class);
    NodeList emptyList = Mockito.mock(NodeList.class);
    Mockito.when(listWithData.getLength()).thenReturn(1);
    Mockito.when(emptyList.getLength()).thenReturn(0);
    assertTrue(XpathQueryHelper.isEmpty(null));
    assertTrue(XpathQueryHelper.isEmpty(emptyList));
    assertFalse(XpathQueryHelper.isEmpty(listWithData));
  }
}
