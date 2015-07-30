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