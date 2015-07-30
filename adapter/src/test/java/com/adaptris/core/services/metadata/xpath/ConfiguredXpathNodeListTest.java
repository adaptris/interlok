package com.adaptris.core.services.metadata.xpath;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.XmlHelper;

public class ConfiguredXpathNodeListTest extends ConfiguredXpathQueryCase {

  public ConfiguredXpathNodeListTest(String testName) {
    super(testName);
  }

  @Override
  protected ConfiguredXpathNodeListQuery create() {
    return new ConfiguredXpathNodeListQuery();
  }

  private ConfiguredXpathNodeListQuery init(ConfiguredXpathNodeListQuery query, String xpath) throws CoreException {
    query.setMetadataKey("result");
    query.setXpathQuery(xpath);
    query.verify();
    return query;
  }

  // Saxon always returns a nodelist
  // public void testResolveXpath_EmptyResults_NotAllowed() throws Exception {
  // ConfiguredXpathNodeListQuery query = init(create(), "//@MissingAttribute");
  // Document doc = XmlHelper.createDocument(XML);
  // AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
  // try {
  // NodeList result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
  // fail();
  // }
  // catch (CoreException expected) {
  //
  // }
  // }
  //
  // public void testResolveXpath_EmptyResults_Allowed() throws Exception {
  // ConfiguredXpathNodeListQuery query = init(create(), "//@MissingAttribute");
  //
  // query.setAllowEmptyResults(Boolean.TRUE);
  // Document doc = XmlHelper.createDocument(XML);
  // AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
  // NodeList result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
  // assertNull(result);
  // }

  public void testResolveXpath() throws Exception {
    ConfiguredXpathNodeListQuery query = init(create(), "//extra[@att='multi']");
    Document doc = XmlHelper.createDocument(XML);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML);
    NodeList result = query.resolveXpath(doc, null, query.createXpathQuery(msg));
    assertNotNull(result);
  }

}
