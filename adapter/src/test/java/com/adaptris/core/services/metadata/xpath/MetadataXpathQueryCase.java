package com.adaptris.core.services.metadata.xpath;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

public abstract class MetadataXpathQueryCase extends XpathQueryCase {

  public MetadataXpathQueryCase(String testName) {
    super(testName);
  }

  public void testSetXpathQuery() throws Exception {
    MetadataXpathQueryImpl query = (MetadataXpathQueryImpl) create();
    assertNull(query.getXpathMetadataKey());
    query.setXpathMetadataKey("xpathMetadataKey");
    assertEquals("xpathMetadataKey", query.getXpathMetadataKey());
    try {
      query.setXpathMetadataKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals("xpathMetadataKey", query.getXpathMetadataKey());
    try {
      query.setXpathMetadataKey(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals("xpathMetadataKey", query.getXpathMetadataKey());
  }

  public void testInit_NoXpathMetadataKey() throws Exception {
    MetadataXpathQueryImpl query = (MetadataXpathQueryImpl) create();
    query.setMetadataKey("key");
    assertNull(query.getXpathMetadataKey());
    // fails because there's no xpathquery
    try {
      query.verify();
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testCreateXpath() throws Exception {
    MetadataXpathQueryImpl query = (MetadataXpathQueryImpl) create();
    query.setXpathMetadataKey("xpathMetadataKey");
    try {
      String xpath = query.createXpathQuery(null);
      fail();
    }
    catch (NullPointerException expected) {

    }
    try {
      String xpath = query.createXpathQuery(AdaptrisMessageFactory.getDefaultInstance().newMessage());
      fail();
    }
    catch (CoreException expected) {

    }
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("xpathMetadataKey", "//root");
    assertEquals("//root", query.createXpathQuery(msg));
  }
}
