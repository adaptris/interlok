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

package com.adaptris.core.http;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.http.client.MetadataRequestMethodProvider;
import com.adaptris.core.http.client.RequestMethodProvider.RequestMethod;

public class MetadataRequestMethodProviderTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testDefaultMethod() {
    MetadataRequestMethodProvider prov = new MetadataRequestMethodProvider("httpMethod");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    assertEquals(RequestMethod.POST, prov.getMethod(msg));
  }


  @Test
  public void testGetMethod_WithMetadata() {
    MetadataRequestMethodProvider prov = new MetadataRequestMethodProvider("httpMethod");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    msg.addMetadata("httpMethod", "GET");

    assertEquals(RequestMethod.GET, prov.getMethod(msg));
  }


}
