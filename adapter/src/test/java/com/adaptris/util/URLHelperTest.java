/*
 * Copyright 2018 Adaptris Ltd.
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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.adaptris.core.BaseCase;

public class URLHelperTest extends URLHelper {

  private static final String KEY_LOCAL = "urlhelper.local";
  private static final String KEY_REMOTE = "urlhelper.remote";
  private static final String KEY_CLASSPATH = "urlhelper.classpath";

  @Test
  public void testConnectLocal_URLString() throws Exception {
    String loc = BaseCase.PROPERTIES.getProperty(KEY_LOCAL);
    try (InputStream in = connect(new URLString(loc))) {
      assertNotNull(in);
    }
  }

  @Test
  public void testConnectLocalResource_URLString() throws Exception {
    String loc = BaseCase.PROPERTIES.getProperty(KEY_CLASSPATH);
    try (InputStream in = connect(new URLString(loc))) {
      assertNotNull(in);
    }
  }

  @Test
  public void testConnectRemote_URLString() throws Exception {
    String loc = BaseCase.PROPERTIES.getProperty(KEY_REMOTE);
    try (InputStream in = connect(new URLString(loc))) {
      assertNotNull(in);
    }
  }

  @Test(expected = IOException.class)
  public void testConnectLocal_String_DoesNotExist() throws Exception {
    GuidGenerator g = new GuidGenerator();
    // if this is successful I think we have a different class of problems...
    connect("file://localhost/c:/" + g.safeUUID() + "/" + g.safeUUID());
  }

  @Test
  public void testConnectLocal_String() throws Exception {
    String loc = BaseCase.PROPERTIES.getProperty(KEY_LOCAL);
    try (InputStream in = connect(loc)) {
      assertNotNull(in);
    }
  }

  @Test
  public void testConnectLocalResource_String() throws Exception {
    String loc = BaseCase.PROPERTIES.getProperty(KEY_CLASSPATH);
    try (InputStream in = connect(loc)) {
      assertNotNull(in);
    }
  }

  @Test
  public void testConnectRemote_String() throws Exception {
    String loc = BaseCase.PROPERTIES.getProperty(KEY_REMOTE);
    try (InputStream in = connect(loc)) {
      assertNotNull(in);
    }
  }
}
