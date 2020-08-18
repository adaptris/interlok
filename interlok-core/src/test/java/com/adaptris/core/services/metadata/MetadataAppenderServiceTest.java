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

package com.adaptris.core.services.metadata;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

public class MetadataAppenderServiceTest extends MetadataServiceExample {

  private MetadataAppenderService service;
  private String resultKey;
  private AdaptrisMessage msg;


  @Before
  public void setUp() throws Exception {
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key1", "val1");
    msg.addMetadata("key2", "val2");
    msg.addMetadata("key3", "val3");

    resultKey = "result";

    service = new MetadataAppenderService();
    service.setResultKey(resultKey);
  }

  @Test
  public void testSetEmptyResultKey() throws Exception {
    MetadataAppenderService service = new MetadataAppenderService();
    try {
      service.setResultKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }

  }

  @Test
  public void testSetNullResultKey() throws Exception {
    MetadataAppenderService service = new MetadataAppenderService();
    try {
      service.setResultKey(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }

  }

  @Test
  public void testTwoKeys() throws CoreException {
    service.addAppendKey("key1");
    service.addAppendKey("key3");

    execute(service, msg);
    assertTrue("val1val3".equals(msg.getMetadataValue(resultKey)));
  }

  @Test
  public void testTwoReferencedKeys() throws CoreException {
    msg.addMessageHeader("RefKey1", "key1");
    msg.addMessageHeader("RefKey3", "key3");
    
    service.addAppendKey("$$RefKey1");
    service.addAppendKey("$$RefKey3");

    execute(service, msg);
    assertTrue("val1val3".equals(msg.getMetadataValue(resultKey)));
  }

  @Test
  public void testTwoKeysOneNotSet() throws CoreException {
    service.addAppendKey("key1");
    service.addAppendKey("key4");

    execute(service, msg);
    assertTrue("val1".equals(msg.getMetadataValue(resultKey)));
  }

  @Test
  public void testNullKey() throws CoreException {
    try {
      service.addAppendKey(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      // okay
    }
  }

  @Test
  public void testEmptyKey() throws CoreException {
    try {
      service.addAppendKey("");
      fail();
    }
    catch (IllegalArgumentException e) {
      // okay
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    service.addAppendKey("key1");
    service.addAppendKey("key2");

    return service;
  }
}
