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
import com.adaptris.core.ServiceException;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;

public class FormDataFromMetadataTest extends MetadataServiceExample {


  private FormDataFromMetadata createService() {
    FormDataFromMetadata svc = new FormDataFromMetadata();
    svc.setMetadataFilter(new RegexMetadataFilter().withIncludePatterns("param1", "param2", "param3"));
    return svc;
  }

  @Test
  public void testService_SimpleQueryString() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("param1", "one");
    msg.addMetadata("param2", "two");
    msg.addMetadata("param3", "three");
    execute(createService(), msg);
    assertTrue(msg.getContent().contains("param2=two"));
    assertTrue(msg.getContent().contains("param1=one"));
    assertTrue(msg.getContent().contains("param3=three"));
  }

  @Test
  public void testService_NoOutput() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("Hello World");
    execute(createService(), msg);
    assertEquals("", msg.getContent());
  }

  @Test
  public void testService_ComplexQueryString() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("param1", "this is a field");
    msg.addMetadata("param3", "was it clear (already)?");
    execute(createService(), msg);
    assertTrue(msg.getContent().contains("param1=this+is+a+field"));
    assertTrue(msg.getContent().contains("param3=was+it+clear+%28already%29%3F"));
  }

  @Test
  public void testService_Failure() throws Exception {
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.METADATA_GET).newMessage();
    msg.addMetadata("param1", "this is a field");
    msg.addMetadata("param3", "was it clear (already)?");
    FormDataToMetadata service = new FormDataToMetadata();
    try {
      execute(createService(), msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Override
  protected FormDataFromMetadata retrieveObjectForSampleConfig() {
    return createService();
  }

}
