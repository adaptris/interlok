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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.metadata.ElementKeyAndValueFormatter;

@SuppressWarnings("deprecation")
public class AddFormattedMetadataServiceTest extends MetadataServiceExample {

  private AddFormattedMetadataService service;
  private MetadataElement m1;
  private MetadataElement m2;


  @Test
  public void testService() throws Exception {
    AddFormattedMetadataService service = retrieveObjectForSampleConfig();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("MetadataKey1", "Hello");
    msg.addMetadata("MetadataKey2", "World");
    execute(service, msg);
    assertTrue(msg.containsKey("destinationMetadataKey"));
    assertEquals("SELECT Hello FROM SOME TABLE WHERE ID = World", msg.getMetadataValue("destinationMetadataKey"));
  }

  @Test
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
    return new AddFormattedMetadataService().withArgumentMetadataKeys("MetadataKey1", "MetadataKey2")
        .withMetadataKey("destinationMetadataKey").withFormatString("SELECT %s FROM SOME TABLE WHERE ID = %s");
  }


  @Test
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
