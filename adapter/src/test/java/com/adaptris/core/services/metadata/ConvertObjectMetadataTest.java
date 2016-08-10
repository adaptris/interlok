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
import com.adaptris.core.util.LifecycleHelper;

public class ConvertObjectMetadataTest extends MetadataServiceExample {

  public ConvertObjectMetadataTest(String name) {
    super(name);
  }

  @Override
  public void setUp() {
  }

  public void testDoService() throws Exception {
    ConvertObjectMetadataService service = new ConvertObjectMetadataService("java.jms.Message.*");
    Object o1 = "java.jms.Message.JMSCorrelationID";
    Object o2 = new Object();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.getObjectHeaders().put(o1, o1);
    msg.getObjectHeaders().put(o2, o2);
    execute(service, msg);
    assertTrue(msg.containsKey(o1.toString()));
    assertEquals(o1.toString(), msg.getMetadataValue(o1.toString()));
  }

  public void testInit_NoRegexp() throws Exception {
    ConvertObjectMetadataService service = new ConvertObjectMetadataService();
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (Exception expected) {

    }
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new ConvertObjectMetadataService("Object_Metadata_key_Regexp");
  }

}
