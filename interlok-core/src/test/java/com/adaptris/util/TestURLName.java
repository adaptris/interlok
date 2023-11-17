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

package com.adaptris.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.mail.URLName;

import org.junit.jupiter.api.Test;

public class TestURLName {

  private static String testUrl = "smtp://user%40btinternet.com:password@mail.btinternet.com/";
  private static String username = "user@btinternet.com";
  private static String password = "password";
  private static String host = "mail.btinternet.com";
  private static String protocol = "smtp";

  @Test
  public void testUrl() throws Exception {
    URLName url = new URLName(testUrl);
    assertEquals(protocol, url.getProtocol());
    assertEquals(username, url.getUsername());
    assertEquals(password, url.getPassword());
    assertEquals(host, url.getHost());
    assertEquals(testUrl, url.toString());
  }

}
