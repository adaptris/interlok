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

import java.io.Serializable;
import java.util.List;

/**
 * Wrapper class around the {@link java.net.HttpURLConnection#getHeaderFields()} for insertion into object metadata.
 * 
 * @author lchan
 *
 */
public final class URLConnectionHeaderField implements Serializable {

  private static final long serialVersionUID = 2015092501L;
  private String key;
  private List<String> values;

  public URLConnectionHeaderField() {

  }

  public URLConnectionHeaderField(String key, List<String> values) {
    this();
    setKey(key);
    setValues(values);
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

}
