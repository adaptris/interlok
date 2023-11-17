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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.http.server.ConfiguredStatusProvider;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;

public class ConfiguredStatusProviderTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {

  }

  @Test
  public void testGetStatus() {
    ConfiguredStatusProvider prov = new ConfiguredStatusProvider(HttpStatus.OK_200);
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    assertEquals(200, prov.getStatus(msg).getCode());
    assertEquals("OK", prov.getStatus(msg).getText());
  }

  @Test
  public void testGetStatus_WithText() {
    ConfiguredStatusProvider prov = new ConfiguredStatusProvider(HttpStatus.OK_200);
    prov.setText("Really Not OK");
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage("");
    assertEquals(200, prov.getStatus(msg).getCode());
    assertNotSame("OK", prov.getStatus(msg).getText());
    assertEquals("Really Not OK", prov.getStatus(msg).getText());
  }


}
