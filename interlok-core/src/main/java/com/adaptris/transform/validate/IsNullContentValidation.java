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

package com.adaptris.transform.validate;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Check the content and ensure it is not null.
 * 
 * @config xml-content-is-null
 * 
 */
@XStreamAlias("xml-content-is-null")
public class IsNullContentValidation implements ContentValidation {

  public boolean isValid(String content) {
    return isBlank(content);
  }

  public String getMessage() {
    return "Data element cannot be empty";
  }
}
