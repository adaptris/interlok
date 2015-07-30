package com.adaptris.core.transform;

import java.util.ArrayList;
import java.util.Arrays;

import com.adaptris.core.BaseCase;
import com.adaptris.transform.validate.NotInListContentValidation;
import com.adaptris.transform.validate.SimpleListContentValidation;

public class ListContentValidationTest extends BaseCase {

  private static String entry1 = "ENTRY_1";
  private static String entry2 = "ENTRY_2";

  public ListContentValidationTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testSimpleListContentValidation_AddListEntry() {
    SimpleListContentValidation cv = new SimpleListContentValidation();
    assertNotNull(cv.getListEntries());
    assertEquals(0, cv.getListEntries().size());
    cv.addListEntry(entry1);
    cv.addListEntry(entry2);
    assertEquals(2, cv.getListEntries().size());
    assertEquals(new ArrayList<String>(Arrays.asList(entry1, entry2)), cv.getListEntries());
  }

  public void testSimpleListContentValidation_SetListEntries() {
    SimpleListContentValidation cv = new SimpleListContentValidation();
    assertNotNull(cv.getListEntries());
    assertEquals(0, cv.getListEntries().size());
    cv.setListEntries(new ArrayList<String>(Arrays.asList(entry1, entry2)));
    assertEquals(2, cv.getListEntries().size());
    assertEquals(new ArrayList<String>(Arrays.asList(entry1, entry2)), cv.getListEntries());
  }

  public void testNotInListContentValidation_AddListEntry() {
    NotInListContentValidation cv = new NotInListContentValidation();
    assertNotNull(cv.getListEntries());
    assertEquals(0, cv.getListEntries().size());
    cv.addListEntry(entry1);
    cv.addListEntry(entry2);
    assertEquals(2, cv.getListEntries().size());
    assertEquals(new ArrayList<String>(Arrays.asList(entry1, entry2)), cv.getListEntries());
  }

  public void testNotInListContentValidation_SetListEntries() {
    NotInListContentValidation cv = new NotInListContentValidation(entry2, entry1);
    cv.setListEntries(new ArrayList<String>(Arrays.asList(entry1, entry2)));
    assertEquals(2, cv.getListEntries().size());
    assertEquals(new ArrayList<String>(Arrays.asList(entry1, entry2)), cv.getListEntries());
  }
}
