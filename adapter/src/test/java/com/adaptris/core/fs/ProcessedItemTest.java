package com.adaptris.core.fs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adaptris.core.BaseCase;

public class ProcessedItemTest extends BaseCase {

  public ProcessedItemTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testAddRemoveProcessedItem() {
    ProcessedItemList list = new ProcessedItemList();
    list.addProcessedItem(new ProcessedItem("ABC", 0, 0));
    assertEquals(1, list.getProcessedItems().size());
    assertTrue(list.getProcessedItems().contains(new ProcessedItem("ABC", 0, 0)));
    list.removeProcessedItem(new ProcessedItem("ABC", 0, 0));
    assertEquals(0, list.getProcessedItems().size());
  }

  public void testGetSet() {
    ProcessedItemList pil = new ProcessedItemList();
    List<ProcessedItem> list = new ArrayList(Arrays.asList(new ProcessedItem[] {
        new ProcessedItem("ABC", 0, 0), new ProcessedItem("DEF", 0, 0)
    }));
    pil.setProcessedItems(list);
    assertEquals(2, pil.getProcessedItems().size());
    assertTrue(pil.getProcessedItems().contains(new ProcessedItem("ABC", 0, 0)));
    assertEquals(list, pil.getProcessedItems());
  }

  public void testItemEquality() {
    ProcessedItem item = new ProcessedItem("ABC", 0, 0);
    ProcessedItem item2 = new ProcessedItem("ABC", 1, 1);
    ProcessedItem item3 = new ProcessedItem("DEF", 0, 0);
    assertEquals(item, item);
    assertEquals(item, item2);
    assertEquals(item.hashCode(), item2.hashCode());
    assertNotSame(item, item3);
    assertFalse(item.equals(new Object()));
    assertFalse(item.equals(null));
  }

  public void testItemAbsolutePath() {
    ProcessedItem item = new ProcessedItem();
    try {
      item.setAbsolutePath(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    item.setAbsolutePath("ABC");
    assertEquals("ABC", item.getAbsolutePath());
    item.setAbsolutePath("//ABC");
    assertNotSame("//ABC", item.getAbsolutePath());
    assertEquals("/ABC", item.getAbsolutePath());
  }

  public void testModifiedTime() {
    ProcessedItem item = new ProcessedItem();
    item.setLastModified(-1);
    assertEquals(-1, item.getLastModified());
  }

  public void testItemFilesize() {
    ProcessedItem item = new ProcessedItem();
    item.setFilesize(-1);
    assertEquals(-1, item.getFilesize());
  }

}