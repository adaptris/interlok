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

import java.util.ArrayList;
import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class CreateQueryStringFromMetadataTest extends MetadataServiceExample {

  public CreateQueryStringFromMetadataTest(String name) {
    super(name);
  }

  public CreateQueryStringFromMetadata createService() {
    CreateQueryStringFromMetadata svc = new CreateQueryStringFromMetadata();
    svc.setResultKey("resultKey");
    svc.setMetadataKeys(new ArrayList(Arrays.asList(new String[] {
        "param1", "param2", "param3"
    })));
    return svc;
  }

  public void testAddMetadataKey() throws Exception {
    CreateQueryStringFromMetadata service = new CreateQueryStringFromMetadata();
    assertEquals(0, service.getMetadataKeys().size());
    service.addMetadataKey("1");
    assertEquals(1, service.getMetadataKeys().size());
    assertEquals("1", service.getMetadataKeys().get(0));
    service.setMetadataKeys(new ArrayList<String>());
    assertEquals(0, service.getMetadataKeys().size());

    try {
      service.setMetadataKeys(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }

    try {
      service.addMetadataKey(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
  }

  public void testQuerySeparator() throws Exception {
    CreateQueryStringFromMetadata service = new CreateQueryStringFromMetadata();
    assertNull(service.getQuerySeparator());
    assertEquals("&", service.querySeparator());
    service.setQuerySeparator(",");
    assertEquals(",", service.querySeparator());
    assertEquals(",", service.getQuerySeparator());

    service.setQuerySeparator(null);
    assertEquals("&", service.querySeparator());
    service.setQuerySeparator(",");
  }

  public void testService_SimpleQueryString() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("param1", "one");
    msg.addMetadata("param2", "two");
    msg.addMetadata("param3", "three");
    execute(createService(), msg);
    assertEquals("?param1=one&param2=two&param3=three", msg.getMetadataValue("resultKey"));
  }

  public void testService_NoOutput() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(createService(), msg);
    assertEquals("", msg.getMetadataValue("resultKey"));
  }

  public void testService_ComplexQueryString() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("param1", "this is a field");
    msg.addMetadata("param3", "was it clear (already)?");
    execute(createService(), msg);
    assertEquals("?param1=this+is+a+field&param3=was+it+clear+%28already%29%3F", msg.getMetadataValue("resultKey"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createService();
  }

}
