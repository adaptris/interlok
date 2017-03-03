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
import com.adaptris.core.FormattedMetadataDestination;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.metadata.ElementFormatter;
import com.adaptris.core.metadata.ElementKeyAndValueFormatter;

public class AddFormattedMetadataServiceTest extends MetadataServiceExample {

  private AddFormattedMetadataService service;
  private MetadataElement m1;
  private MetadataElement m2;

  public AddFormattedMetadataServiceTest(String name) {
    super(name);
  }

  public void testService() throws Exception {
    AddFormattedMetadataService service = retrieveObjectForSampleConfig();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("MetadataKey1", "Hello");
    msg.addMetadata("MetadataKey2", "World");
    execute(service, msg);
    assertTrue(msg.containsKey("destinationMetadataKey"));
    assertEquals("SELECT Hello FROM SOME TABLE WHERE ID = World", msg.getMetadataValue("destinationMetadataKey"));
  }

  public void testService_MissingMetadata() throws Exception {
    AddFormattedMetadataService service = retrieveObjectForSampleConfig();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("MetadataKey1", "Hello");
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }


  @Override
  protected AddFormattedMetadataService retrieveObjectForSampleConfig() {
    AddFormattedMetadataService service = new AddFormattedMetadataService();
    service.setFormatString("SELECT %s FROM SOME TABLE WHERE ID = %s");
    service.getArgumentMetadataKeys().add("MetadataKey1");
    service.getArgumentMetadataKeys().add("MetadataKey2");
    service.setMetadataKey("destinationMetadataKey");
    return service;
  }

  /**
   * Test the default behaviour of the ElementKeyAndValueFormatter.
   * 
   * @throws Exception If there is a problem during the test.
   */
  public void testElementFormatter() throws Exception {
    AddFormattedMetadataService service = retrieveObjectForSampleConfig();
    service.setFormatString("%s ; %s");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    msg.addMetadata("MetadataKey1", "Hello");
    msg.addMetadata("MetadataKey2", "World");
    service.setElementFormatter(new ElementKeyAndValueFormatter());

    service.doService(msg);
    // {destinationMetadataKey=[MetadataKey1=Hello] = [MetadataKey2=World], MetadataKey1=Hello, MetadataKey2=World}
    assertEquals("MetadataKey1=Hello ; MetadataKey2=World", msg.getMessageHeaders().get("destinationMetadataKey"));
  }

}
