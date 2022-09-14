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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.adaptris.util.text.mime.ByteArrayDataSource;

public class ByteArrayDataSourceTest {

  private static final String EXAMPLE_XML = "<document>" + "<root>"
      + "<data>abcdefg</data>" + "</root>" + "</document>";

  @Test
  public void testInputStream() throws Exception {
    ByteArrayDataSource bads = new ByteArrayDataSource(EXAMPLE_XML.getBytes(),
        "text/xml", "name");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(bads.getBytes());
    out.flush();
    assertEquals("XML", out.toString(), EXAMPLE_XML);
  }

  @Test
  public void testContentType() throws Exception {
    ByteArrayDataSource bads = new ByteArrayDataSource(EXAMPLE_XML.getBytes(),
        "text/xml", "name");
    assertEquals("content type", "text/xml", bads.getContentType());
  }

  @Test
  public void testName() throws Exception {
    ByteArrayDataSource bads = new ByteArrayDataSource(EXAMPLE_XML.getBytes(),
        "text/xml", "name");
    assertEquals("name", "name", bads.getName());
  }

  @Test
  public void testOutputStream() throws Exception {
    ByteArrayDataSource bads = new ByteArrayDataSource(EXAMPLE_XML.getBytes(),
        "text/xml", "name");
    try {
      bads.getOutputStream();
      fail("getOutputStream should not be supported");
    }
    catch (Exception e) {
      ;
    }
  }

}
