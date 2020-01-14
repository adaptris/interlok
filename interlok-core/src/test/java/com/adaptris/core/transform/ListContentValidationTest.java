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

package com.adaptris.core.transform;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import com.adaptris.core.BaseCase;
import com.adaptris.transform.validate.NotInListContentValidation;
import com.adaptris.transform.validate.SimpleListContentValidation;

public class ListContentValidationTest extends BaseCase {

  private static String entry1 = "ENTRY_1";
  private static String entry2 = "ENTRY_2";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testSimpleListContentValidation_AddListEntry() {
    SimpleListContentValidation cv = new SimpleListContentValidation();
    assertNotNull(cv.getListEntries());
    assertEquals(0, cv.getListEntries().size());
    cv.addListEntry(entry1);
    cv.addListEntry(entry2);
    assertEquals(2, cv.getListEntries().size());
    assertEquals(new ArrayList<String>(Arrays.asList(entry1, entry2)), cv.getListEntries());
  }

  @Test
  public void testSimpleListContentValidation_SetListEntries() {
    SimpleListContentValidation cv = new SimpleListContentValidation();
    assertNotNull(cv.getListEntries());
    assertEquals(0, cv.getListEntries().size());
    cv.setListEntries(new ArrayList<String>(Arrays.asList(entry1, entry2)));
    assertEquals(2, cv.getListEntries().size());
    assertEquals(new ArrayList<String>(Arrays.asList(entry1, entry2)), cv.getListEntries());
  }

  @Test
  public void testNotInListContentValidation_AddListEntry() {
    NotInListContentValidation cv = new NotInListContentValidation();
    assertNotNull(cv.getListEntries());
    assertEquals(0, cv.getListEntries().size());
    cv.addListEntry(entry1);
    cv.addListEntry(entry2);
    assertEquals(2, cv.getListEntries().size());
    assertEquals(new ArrayList<String>(Arrays.asList(entry1, entry2)), cv.getListEntries());
  }

  @Test
  public void testNotInListContentValidation_SetListEntries() {
    NotInListContentValidation cv = new NotInListContentValidation(entry2, entry1);
    cv.setListEntries(new ArrayList<String>(Arrays.asList(entry1, entry2)));
    assertEquals(2, cv.getListEntries().size());
    assertEquals(new ArrayList<String>(Arrays.asList(entry1, entry2)), cv.getListEntries());
  }
}
