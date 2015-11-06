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

package com.adaptris.core.services.exception;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceList;
import com.adaptris.core.services.metadata.AddMetadataService;

public class ExceptionHandlingServiceWrapperTest extends ExceptionServiceExample {

  public ExceptionHandlingServiceWrapperTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testWithExceptionFromWrappedServices() throws Exception {
    ExceptionHandlingServiceWrapper service = create();
    service.setService(new ServiceList(new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement("servicesComplete", "true")
        }))
    }));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals("true", msg.getMetadataValue("exceptionServiceTriggered"));
    assertNull(msg.getMetadataValue("servicesComplete"));
  }

  public void testNoExceptionFromWrappedServices() throws Exception {
    AddMetadataService s1 = new AddMetadataService();
    s1.addMetadataElement("servicesComplete", "true");
    ExceptionHandlingServiceWrapper service = create();
    service.setService(s1);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);

    assertEquals("true", msg.getMetadataValue("servicesComplete"));
    assertNull(msg.getMetadataValue("exceptionServiceTriggered"));
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    ExceptionHandlingServiceWrapper service = create();
    service.setService(new ServiceList(new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement("servicesComplete", "true")
        }))
    }));
    return service;
  }

  private ExceptionHandlingServiceWrapper create() {
    ExceptionHandlingServiceWrapper service = new ExceptionHandlingServiceWrapper();
    AddMetadataService fail = new AddMetadataService();
    fail.addMetadataElement("exceptionServiceTriggered", "true");
    service.setExceptionHandlingService(fail);
    return service;
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + "<!--\n"
        + "\nThis example explicitly is configured to fail. Even so, no exception will be thrown\n"
        + "However the metadata key 'exceptionServiceTriggered' will be set to 'true'\n" + "'servicesComplete' is never set"
        + "\n-->\n";
  }

}
