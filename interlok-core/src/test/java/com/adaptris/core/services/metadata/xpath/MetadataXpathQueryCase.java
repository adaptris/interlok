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
