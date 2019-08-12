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
import com.adaptris.core.metadata.RegexMetadataFilter;

public class CreateQueryStringFromMetadataTest extends MetadataServiceExample {

  public CreateQueryStringFromMetadataTest(String name) {
    super(name);
  }

  public CreateQueryStringFromMetadata createService() {
    CreateQueryStringFromMetadata svc = new CreateQueryStringFromMetadata();
    svc.setResultKey("resultKey");
    svc.setMetadataFilter(new RegexMetadataFilter().withIncludePatterns("param1", "param2", "param3"));
    return svc;
  }


  @SuppressWarnings("deprecation")
  public void testQuerySeparator() throws Exception {
    CreateQueryStringFromMetadata service = new CreateQueryStringFromMetadata();
    assertNull(service.getSeparator());
    assertEquals("&", service.separator());
    service.setSeparator(",");
    assertEquals(",", service.separator());
    assertEquals(",", service.getSeparator());

    service.setSeparator(null);
    service.setQuerySeparator(",");

    assertEquals(",", service.separator());
    service.setQuerySeparator(null);
    assertEquals("&", service.separator());
  }

  public void testService_SimpleQueryString() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("param1", "one");
    msg.addMetadata("param2", "two");
    msg.addMetadata("param3", "three");
    execute(createService(), msg);
    assertTrue(msg.getMetadataValue("resultKey").startsWith("?"));
    assertTrue(msg.getMetadataValue("resultKey").contains("param2=two"));
    assertTrue(msg.getMetadataValue("resultKey").contains("param1=one"));
    assertTrue(msg.getMetadataValue("resultKey").contains("param3=three"));
  }


  public void testService_SimpleQueryString_NoQueryPrefix() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    CreateQueryStringFromMetadata service = createService();
    service.setIncludeQueryPrefix(false);
    msg.addMetadata("param1", "one");
    msg.addMetadata("param2", "two");
    msg.addMetadata("param3", "three");
    execute(service, msg);
    assertTrue(msg.getMetadataValue("resultKey").contains("param2=two"));
    assertTrue(msg.getMetadataValue("resultKey").contains("param1=one"));
    assertTrue(msg.getMetadataValue("resultKey").contains("param3=three"));
    assertFalse(msg.getMetadataValue("resultKey").startsWith("?"));
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
    assertTrue(msg.getMetadataValue("resultKey").startsWith("?"));
    assertTrue(msg.getMetadataValue("resultKey").contains("param1=this+is+a+field"));
    assertTrue(msg.getMetadataValue("resultKey").contains("param3=was+it+clear+%28already%29%3F"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createService();
  }

}
