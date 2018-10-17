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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Makes sure that the content is not in the specified list.
 * 
 * @config "xml-content-not-in-list
 */
@XStreamAlias("xml-content-not-in-list")
public class NotInListContentValidation extends ListContentValidation {

  public NotInListContentValidation() {

  }

  public NotInListContentValidation(String... contents) {
    this(new ArrayList<String>(Arrays.asList(contents)));
  }

  public NotInListContentValidation(List<String> contents) {
    this();
    setListEntries(contents);
  }

  public boolean isValid(String content) {
    return !getListEntries().contains(content);
  }

  /**
   *  @see ContentValidation#getMessage()
   */
  public String getMessage() {
    return "Element contents matches entry in the specified list";
  }
}
