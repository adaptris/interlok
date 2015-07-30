package com.adaptris.core.services.metadata.xpath;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

public abstract class ConfiguredXpathQueryCase extends XpathQueryCase {

  public ConfiguredXpathQueryCase(String testName) {
    super(testName);
  }


  public void testSetXpathQuery() throws Exception {
    ConfiguredXpathQueryImpl query = (ConfiguredXpathQueryImpl) create();
    assertNull(query.getXpathQuery());
    query.setXpathQuery("//root");
    assertEquals("//root", query.getXpathQuery());
    try {
      query.setXpathQuery("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals("//root", query.getXpathQuery());
    try {
      query.setXpathQuery(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals("//root", query.getXpathQuery());
  }

  public void testInit_NoXpathQuery() throws Exception {
    ConfiguredXpathQueryImpl query = (ConfiguredXpathQueryImpl) create();
    query.setMetadataKey("key");
    // fails because there's no xpathquery
    try {
      query.verify();
      fail();
    }
    catch (CoreException expected) {

    }
  }

  public void testCreateXpath() throws Exception {
    ConfiguredXpathQueryImpl query = (ConfiguredXpathQueryImpl) create();
    query.setXpathQuery("//root");
    assertEquals("//root", query.createXpathQuery(null));
    assertEquals("//root", query.createXpathQuery(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
  }

}
