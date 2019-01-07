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
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreateMetadataFromQueryStringTest {

  private static CreateMetadataFromQueryString service;
  private static final String QUERY_STRING = "param1=one&param2=two&param3=This+is+a+3rd+param";

  @BeforeClass
  public static void setup() {
    service = new CreateMetadataFromQueryString();
    service.setMetadataKey("query_string");
  }

  @Test
  public void metadata_fromURL_noQueryPrefix() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("query_string", "http://localhost:80/api?" + QUERY_STRING);

    service.setIncludeQueryPrefix(Boolean.FALSE);

    service.doService(msg);
    assertNewMetadata(msg);
  }

  @Test
  public void metadata_fromQueryString_usingQueryPrefix() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("query_string", QUERY_STRING);

    service.setIncludeQueryPrefix(Boolean.TRUE);

    service.doService(msg);
    assertNewMetadata(msg);
  }

  @Test
  public void noMetadata_fromQueryString_noQueryPrefix() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("query_string", "param1=one&param2=two&param3=This+is+a+3rd+param");

    service.setIncludeQueryPrefix(Boolean.FALSE);

    service.doService(msg);
    assertEquals(null, msg.getMetadataValue("param1"));
    assertEquals(null, msg.getMetadataValue("param2"));
    assertEquals(null, msg.getMetadataValue("param3"));
  }

  private void assertNewMetadata(AdaptrisMessage msg) {
    assertEquals("one", msg.getMetadataValue("param1"));
    assertEquals("two", msg.getMetadataValue("param2"));
    assertEquals("This is a 3rd param", msg.getMetadataValue("param3"));
  }
}
