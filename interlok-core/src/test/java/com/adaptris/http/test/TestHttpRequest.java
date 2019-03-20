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

package com.adaptris.http.test;

import java.util.HashMap;
import java.util.Map;

import com.adaptris.http.HttpRequest;

import junit.framework.TestCase;

public class TestHttpRequest extends TestCase {

  public TestHttpRequest(String name) {
    super(name);
  }
  
  private HttpRequest request;
  
  // changing order effects testGetParameters
  private static final String[] URIS_TO_TEST = { 
      "/path", 
      "/path?a=b",
      "/path?a=b&c=d",
      "/path?a=b#ref",
      "/path?",
      "/path?a=b&",

      "/path?a",
      "/path?=",
      "/path?b=",
      "/path?=b",
      "/path?bb=",
      "/path?=bb",
      "/path?bbb=",
      "/path?=bbb",
      "/path?a=b&c",
      "/path?a=b&=",
      "/path?a=b&c=",
      "/path?a=b&=d",
      "/path#ref"
  };
  
  
  private static final String FILE = "/path";

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    
    request = new HttpRequest();
  }
  
  /**
   * <p>
   * Tests that the file element is correctly returned for any given URI.
   * </p>
   */
  public void testGetFile() {
    for (int i = 0; i < URIS_TO_TEST.length; i++) {
      request.setURI(URIS_TO_TEST[i]);
      assertTrue(request.getFile().equals(FILE));
    }
  }
  
  public void testGetParameters() {
    
    request.setURI(URIS_TO_TEST[0]);
    Map result0 = new HashMap();
    assertTrue(request.getParameters().equals(result0));
    
    request.setURI(URIS_TO_TEST[1]);
    Map result1 = new HashMap();
    result1.put("a", "b");
    assertTrue(request.getParameters().equals(result1));
    
    request.setURI(URIS_TO_TEST[2]);
    Map result2 = new HashMap();
    result2.put("a", "b");
    result2.put("c", "d");
    assertTrue(request.getParameters().equals(result2));
    
    request.setURI(URIS_TO_TEST[3]);
    Map result3 = new HashMap();
    result3.put("a", "b");
    assertTrue(request.getParameters().equals(result3));
    
    request.setURI(URIS_TO_TEST[4]);
    Map result4 = new HashMap();
    assertTrue(request.getParameters().equals(result4));
    
    request.setURI(URIS_TO_TEST[5]);
    Map result5 = new HashMap();
    result5.put("a", "b");
    assertTrue(request.getParameters().equals(result5));
    
    // for all malformed parameter string and empty Map is returned.
    for (int i = 6; i < URIS_TO_TEST.length; i++) {
      request.setURI(URIS_TO_TEST[i]);
      assertTrue(request.getParameters().equals(new HashMap()));
    }
  }
}
