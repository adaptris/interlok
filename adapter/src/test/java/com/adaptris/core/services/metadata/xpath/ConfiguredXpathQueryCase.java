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
