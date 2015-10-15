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

package com.adaptris.core.fs;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Used by {@link MarshallingItemCache} to persist {@link ProcessedItem} entries to disk.
 * 
 * @config processed-item-list
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("processed-item-list")
public class ProcessedItemList {

  private List<ProcessedItem> processedItems;

  public ProcessedItemList() {
    setProcessedItems(new ArrayList<ProcessedItem>());
  }

  public ProcessedItemList(List<ProcessedItem> list) {
    this();
    setProcessedItems(list);
  }

  /**
   * @return the list
   */
  public List<ProcessedItem> getProcessedItems() {
    return processedItems;
  }

  /**
   * @param list the list to set
   */
  public void setProcessedItems(List<ProcessedItem> list) {
    processedItems = list;
  }

  public void removeProcessedItem(ProcessedItem item) {
    processedItems.remove(item);
  }

  public void addProcessedItem(ProcessedItem item) {
    processedItems.add(item);
  }
}
