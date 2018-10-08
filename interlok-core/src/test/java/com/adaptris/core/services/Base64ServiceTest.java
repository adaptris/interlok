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

package com.adaptris.core.services;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.GeneralServiceExample;

public class Base64ServiceTest extends GeneralServiceExample {

  public static final String LINE = "The quick brown fox jumps over the lazy dog";

  public Base64ServiceTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testBase64Service() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(
        LINE.getBytes());
    execute(new Base64EncodeService(), msg);
    execute(new Base64DecodeService(), msg);

    assertEquals("base64 then debase64 gives same result", LINE, msg
        .getContent());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null; // over-ride retrieveServices below
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    List result = new ArrayList();
    result.add(new Base64EncodeService());
    result.add(new Base64DecodeService());
    return result;
  }
}
