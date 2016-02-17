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

import java.util.HashSet;
import java.util.Set;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;

public class AddMetadataServiceTest extends MetadataServiceExample {

  private AddMetadataService service;
  private MetadataElement m1;
  private MetadataElement m2;

  public AddMetadataServiceTest(String name) {
    super(name);
  }

  @Override
  public void setUp() {
    m1 = new MetadataElement("key1", "val1");
    m2 = new MetadataElement("key2", "val2");

    service = new AddMetadataService();
    service.addMetadataElement(m1);
    service.addMetadataElement(m2);
  }

  public void testAddMetadataElement() {
    AddMetadataService s = new AddMetadataService();
    MetadataElement me = new MetadataElement("key3", "val3");
    s.addMetadataElement(me);
    assertTrue(s.getMetadataElements().contains(me));
    try {
      s.addMetadataElement(null, null);
      fail("expected IllegalArgumnetException on service.addMetadataElement(null, null)");
    }
    catch (IllegalArgumentException e) {
    }
      s.addMetadataElement(m1.getKey(), m1.getValue());
    assertTrue(s.getMetadataElements().contains(m1));
  }

  public void testGetMetadataElements() {
    Set metadata = new HashSet();
    metadata.add(m1);
    metadata.add(m2);

    assertTrue(service.getMetadataElements().equals(metadata));
  }

  public void testDoService() throws CoreException {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage();
    execute(service, msg);

    assertTrue(msg.getMetadataValue("key1").equals("val1"));
    assertTrue(msg.getMetadataValue("key2").equals("val2"));
    assertTrue(msg.getMetadataValue("key3") == null);
  }

  public void testDoServiceWithReferencedKey() throws CoreException {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage();
    
    msg.addMessageHeader("key999", "key666");
    
    m1 = new MetadataElement("$$key999", "value666");
    service = new AddMetadataService();
    service.addMetadataElement(m1);
    
    execute(service, msg);
    
    assertEquals("value666", msg.getMetadataValue("key666"));
  }

  public void testUniqueIdMetadata() throws CoreException {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage();
    MetadataElement m3 = new MetadataElement("key3", "$UNIQUE_ID$");
    service.addMetadataElement(m3);
    execute(service, msg);

    assertTrue(msg.getMetadataValue("key1").equals("val1"));
    assertTrue(msg.getMetadataValue("key2").equals("val2"));
    assertEquals(msg.getUniqueId(), msg.getMetadataValue("key3"));
  }


  public void testFilesizeMetadata() throws CoreException {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("The Quick Brown Fox Jumps Over The Lazy Dog");
    MetadataElement m3 = new MetadataElement("key3", "$MSG_SIZE$");
    service.addMetadataElement(m3);
    execute(service, msg);

    assertTrue(msg.getMetadataValue("key1").equals("val1"));
    assertTrue(msg.getMetadataValue("key2").equals("val2"));
    assertEquals(String.valueOf(msg.getSize()), msg.getMetadataValue("key3"));
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return service;
  }

}
