package com.adaptris.taglet;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

/**
 * @author mwarman
 */
public abstract class AbstractTaglet implements Taglet {

  @Override
  public abstract String getName();

  public abstract String getStart();

  public abstract String getEnd();

  @Override
  public boolean inField() {
    return false;
  }

  @Override
  public boolean inConstructor() {
    return true;
  }

  @Override
  public boolean inMethod() {
    return true;
  }

  @Override
  public boolean inOverview() {
    return true;
  }

  @Override
  public boolean inPackage() {
    return true;
  }

  @Override
  public boolean inType() {
    return true;
  }

  @Override
  public boolean isInlineTag() {
    return false;
  }

  @Override
  public String toString(Tag tag) {
    return getStart() + tag.text() + getEnd();
  }

  @Override
  public String toString(Tag[] tags) {
    if (tags.length == 0) {
      return null;
    }
    String result = getStart();
    for (int i = 0; i < tags.length; i++) {
      if (i > 0) {
        result += ", ";
      }
      result += tags[i].text();
    }
    result += getEnd();
    return result;
  }
}
