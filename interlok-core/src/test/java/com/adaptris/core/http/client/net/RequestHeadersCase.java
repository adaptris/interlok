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

package com.adaptris.core.http.client.net;

import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.TestName;

import com.adaptris.core.util.Args;

public abstract class RequestHeadersCase {

  @Rule
  public TestName testName = new TestName();


  protected static boolean contains(URLConnection request, String headerKey, String headerValue) {
    boolean matched = false;
    String compareKey = Args.notEmpty(headerKey, "key");
    String compareValue = Args.notEmpty(headerValue, "value");
    Map<String, List<String>> headers = request.getRequestProperties();
    for (String h : headers.keySet()) {
      if (h.equals(compareKey)) {
        List<String> values = headers.get(h);
        for (String v : values) {
          if (v.equals(compareValue)) {
            matched = true;
            break;
          }
        }
      }
    }
    return matched;
  }

}
