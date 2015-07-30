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