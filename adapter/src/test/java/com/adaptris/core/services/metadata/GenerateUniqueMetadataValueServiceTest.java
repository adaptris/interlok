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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.PlainIdGenerator;


public class GenerateUniqueMetadataValueServiceTest extends MetadataServiceExample {

  public GenerateUniqueMetadataValueServiceTest(String name) {
    super(name);
  }

  public void testSetGenerator() throws Exception {
    GenerateUniqueMetadataValueService service = new GenerateUniqueMetadataValueService();
    assertNotNull(service.getGenerator());
    assertEquals(GuidGenerator.class, service.getGenerator().getClass());

    PlainIdGenerator gen = new PlainIdGenerator();
    service.setGenerator(gen);
    assertEquals(gen, service.getGenerator());

    try {
      service.setGenerator(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(gen, service.getGenerator());
  }

  public void testSetMetadataKey() throws Exception {
    GenerateUniqueMetadataValueService service = new GenerateUniqueMetadataValueService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    assertNull(service.getMetadataKey());

    String s = service.metadataKey(msg);
    assertNotNull(s);
    assertNotSame("", s);
    assertNotSame(s, service.metadataKey(msg));

    service.setMetadataKey("key");
    assertEquals("key", service.getMetadataKey());
    assertEquals("key", service.metadataKey(msg));

    service.setMetadataKey(null);
    assertNull(service.getMetadataKey());
    assertNotNull(service.metadataKey(msg));

    service.setMetadataKey("");
    assertEquals("", service.getMetadataKey());
    assertNotSame("", service.metadataKey(msg));
  }

  public void testDoService() throws Exception {
    GenerateUniqueMetadataValueService service = new GenerateUniqueMetadataValueService("key");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertTrue(msg.containsKey("key"));
    assertNotNull(msg.getMetadataValue("key"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new GenerateUniqueMetadataValueService("The_Metadata_Key_For_The_Unique_Value");
  }

}
