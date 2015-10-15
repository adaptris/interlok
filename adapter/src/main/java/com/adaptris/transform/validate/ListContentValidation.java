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
import java.util.List;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Compare the content against some specified list.
 * 
 */
public abstract class ListContentValidation implements ContentValidation {

  @XStreamImplicit(itemFieldName = "list-entry")
  @NotNull
  @AutoPopulated
  private List<String> listEntries = new ArrayList<String>();

  public ListContentValidation() {

  }

  /** Add an entry to the list that will be used for validation.
   *
   * @param entry the entry.
   */
  public void addListEntry(String entry) {
    listEntries.add(entry);
  }

  /** Get the list of entries that will be used for validation.
   *
   * @return the list.
   */
  public List<String> getListEntries() {
    return listEntries;
  }

  /** Set the list of entries that will be used for validation.
   *
   * @param l the list.
   */
  public void setListEntries(List<String> l) {
    listEntries = l;
  }
}
