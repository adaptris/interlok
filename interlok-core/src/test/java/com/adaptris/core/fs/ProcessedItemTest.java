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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.BaseCase;

public class ProcessedItemTest extends BaseCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testAddRemoveProcessedItem() {
    ProcessedItemList list = new ProcessedItemList();
    list.addProcessedItem(new ProcessedItem("ABC", 0, 0));
    assertEquals(1, list.getProcessedItems().size());
    assertTrue(list.getProcessedItems().contains(new ProcessedItem("ABC", 0, 0)));
    list.removeProcessedItem(new ProcessedItem("ABC", 0, 0));
    assertEquals(0, list.getProcessedItems().size());
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testModifiedTime() {
    ProcessedItem item = new ProcessedItem();
    item.setLastModified(-1);
    assertEquals(-1, item.getLastModified());
  }

  @Test
  public void testItemFilesize() {
    ProcessedItem item = new ProcessedItem();
    item.setFilesize(-1);
    assertEquals(-1, item.getFilesize());
  }

}
