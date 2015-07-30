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
