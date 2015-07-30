package com.adaptris.core.services.metadata.xpath;

import org.w3c.dom.Document;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.XmlHelper;

public class MetadataXpathQueryTest extends MetadataXpathQueryCase {

  public MetadataXpathQueryTest(String testName) {
    super(testName);
  }

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

  public void testResolveXpath_EmptyResults_NotAllowed() throws Exception {
    MetadataXpathQuery query = init(create());
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
    MetadataXpathQuery query = init(create());
    query.setAllowEmptyResults(Boolean.TRUE);

    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "//@MissingAttribute");
    MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
    assertEquals("", result.getValue());
  }

  public void testResolveXpath_Attribute() throws Exception {
    MetadataXpathQuery query = init(create());

    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "//@att");
    MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
  }

  public void testResolveXpath_function() throws Exception {
    MetadataXpathQuery query = init(create());

    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    msg.addMetadata("xpathMetadataKey", "count(/message)");
    MetadataElement result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
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

  public void testResolveXpath_NamespaceWithNamespaceContext() throws Exception {
    MetadataXpathQuery query = init(create());

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    msg.addMetadata("xpathMetadataKey", "count(/svrl:schematron-output/svrl:failed-assert)");

    StaticNamespaceContext ctx = new StaticNamespaceContext();
    Document doc = XmlHelper.createDocument(XML_WITH_NAMESPACE, ctx);
    MetadataElement result = query.resolveXpath(doc, ctx, query.createXpathQuery(msg));
    assertEquals("2", result.getValue());
  }

}
