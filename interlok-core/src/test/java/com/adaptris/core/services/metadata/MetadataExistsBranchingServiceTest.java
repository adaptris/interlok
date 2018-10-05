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
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.services.BranchingServiceExample;
import com.adaptris.core.services.LogMessageService;

public class MetadataExistsBranchingServiceTest extends BranchingServiceExample {

  private MetadataExistsBranchingService service;
  private AdaptrisMessage msg;

  public MetadataExistsBranchingServiceTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    service = new MetadataExistsBranchingService();
    service.addMetadataKey("key1");
    service.setDefaultServiceId("default");
    service.setMetadataExistsServiceId("exists");

    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("xxxxzzzz");
  }

  public void testSetters() throws Exception {
    MetadataExistsBranchingService service = new MetadataExistsBranchingService();
    try {
      service.addMetadataKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      service.addMetadataKey(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      service.setMetadataExistsServiceId(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      service.setMetadataExistsServiceId("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
  }

  public void testMetadataExists() throws Exception {
    msg.addMetadata("key1", "val1");
    execute(service, msg);
    assertTrue("exists".equals(msg.getNextServiceId()));
  }

  public void testMetadataExistsButIsEmpty() throws Exception {
    msg.addMetadata("key1", "");
    execute(service, msg);
    assertTrue("default".equals(msg.getNextServiceId()));
  }

  public void testMetadataDoesntExist() throws Exception {
    execute(service, msg);

    assertTrue("default".equals(msg.getNextServiceId()));
  }

  public void testMultipleKeysExists() throws Exception {
    msg.clearMetadata();
    msg.addMetadata("key2", "val2");

    service.addMetadataKey("key2");
    execute(service, msg);

    assertTrue("exists".equals(msg.getNextServiceId()));
  }

  public void testMultipleKeysDoesntExist() throws Exception {
    msg.clearMetadata();

    service.addMetadataKey("key2");
    execute(service, msg);

    assertTrue("default".equals(msg.getNextServiceId()));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    BranchingServiceCollection sl = new BranchingServiceCollection();
    service.setUniqueId("CheckMetadataExists");
    sl.addService(service);
    sl.setFirstServiceId(service.getUniqueId());
    sl.addService(new LogMessageService("exists"));
    sl.addService(new LogMessageService("default"));
    return sl;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return MetadataExistsBranchingService.class.getName();
  }
}
