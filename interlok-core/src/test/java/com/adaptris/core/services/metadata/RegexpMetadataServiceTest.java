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
import com.adaptris.core.ServiceException;

@SuppressWarnings("deprecation")
public class RegexpMetadataServiceTest extends MetadataServiceExample {

  public static final String PAYLOAD = "Address: Adaptris Limited, 120 Bath Road, " + "Heathrow, Middlesex, UB3 5AN";
  public static final String ALTERNATE_PAYLOAD = "The quick brown fox jumps over the lazy dog";
  public RegexpMetadataServiceTest(String name) {
    super(name);
  }

  private RegexpMetadataService createService() {
    RegexpMetadataQuery query1 = new RegexpMetadataQuery();
    query1.setQueryExpression(".*[ ]([A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][A-Z-[CIKMOV]]{2}).*");
    query1.setMetadataKey("postcode");
    RegexpMetadataService service = new RegexpMetadataService(new ArrayList<RegexpMetadataQuery>(Arrays
        .asList(new RegexpMetadataQuery[]
    {
      query1
    })));
    return service;
  }

  public void testService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    RegexpMetadataService service = createService();
    execute(service, msg);
    assertTrue("message contains key1", msg.containsKey("postcode"));
    assertEquals("Found the post-code", "UB3 5AN", msg.getMetadataValue("postcode"));
  }

  public void testServiceOverwritesMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    msg.addMetadata("postcode", "GU34 1ET");
    RegexpMetadataService service = createService();
    execute(service, msg);
    assertTrue("message contains key1", msg.containsKey("postcode"));
    assertEquals("Found the post-code", "UB3 5AN", msg.getMetadataValue("postcode"));
  }

  public void testServiceNotAllowNulls() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
    RegexpMetadataService service = createService();
    execute(service, msg);
    try {
      msg.setContent(ALTERNATE_PAYLOAD, msg.getContentEncoding());
      execute(service, msg);
      fail("Expected serviceException");
    }
    catch (ServiceException expected) {
      ;
    }
    assertTrue("message contains key1", msg.containsKey("postcode"));
    assertEquals("Found the post-code", "UB3 5AN", msg.getMetadataValue("postcode"));
  }

  public void testServiceAllowNulls() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ALTERNATE_PAYLOAD);
    RegexpMetadataService service = createService();
    service.setAddNullValues(true);
    RegexpMetadataQuery q = service.getRegexpMetadataQueries().get(0);
    q.setAllowNulls(true);
    execute(service, msg);
    assertTrue("message contains key1", msg.containsKey("postcode"));
    assertEquals("Found the post-code", "", msg.getMetadataValue("postcode"));
  }

  public void testServiceAllowNulls_DoNotAddNullValues() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ALTERNATE_PAYLOAD);
    RegexpMetadataService service = createService();
    service.setAddNullValues(false);
    RegexpMetadataQuery q = service.getRegexpMetadataQueries().get(0);
    q.setAllowNulls(true);
    execute(service, msg);
    assertFalse(msg.containsKey("postcode"));
  }


  public void testSetters() throws Exception {
    RegexpMetadataQuery query1 = new RegexpMetadataQuery();
    try {
      query1.setQueryExpression("");
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException expected) {
      ;
    }
    try {
      query1.setQueryExpression(null);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException expected) {
      ;
    }
    try {
      query1.setMetadataKey("");
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException expected) {
      ;
    }
    try {
      query1.setMetadataKey(null);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException expected) {
      ;
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    RegexpMetadataService service = createService();
    service.addRegexpMetadataQuery(new RegexpMetadataQuery("key2", ".*(query2).*"));
    return service;
  }

}
