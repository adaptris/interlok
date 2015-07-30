package com.adaptris.transform.validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Compare the content against some specified list.
 * 
 * @config xml-content-in-list
 * 
 * @author sellidge
 * @author $Author: phigginson $
 */
@XStreamAlias("xml-content-in-list")
public class SimpleListContentValidation extends ListContentValidation {

  public SimpleListContentValidation() {

  }

  public SimpleListContentValidation(String... contents) {
    this(new ArrayList<String>(Arrays.asList(contents)));
  }

  public SimpleListContentValidation(List<String> contents) {
    this();
    setListEntries(contents);
  }

  public boolean isValid(String content) {
    return getListEntries().contains(content);
  }

  /**
   *  @see ContentValidation#getMessage()
   */
  public String getMessage() {
    return "Element contents do not match an entry in the specified list";
  }
}