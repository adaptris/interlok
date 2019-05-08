package com.adaptris.taglet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

import com.sun.source.doctree.DocTree;

import jdk.javadoc.doclet.Taglet;

/**
 * @author mwarman
 */
public abstract class AbstractTaglet implements Taglet {

  @Override
  public abstract String getName();

  public abstract String getStart();

  public abstract String getEnd();

  @Override
  public Set<Taglet.Location> getAllowedLocations() {
    Set<Taglet.Location> locs = new HashSet<>();
    locs.add(Taglet.Location.CONSTRUCTOR);
    locs.add(Taglet.Location.METHOD);
    locs.add(Taglet.Location.OVERVIEW);
    locs.add(Taglet.Location.PACKAGE);
    locs.add(Taglet.Location.TYPE);
    return locs;
  }

  @Override
  public boolean isInlineTag() {
    return false;
  }

  @Override
  public String toString(List<? extends DocTree> tags, Element element) {
    if (tags.isEmpty()) {
      return null;
    }
    StringBuffer sb = new StringBuffer(getStart());
    boolean c = false;
    for (DocTree tag : tags) {
      if (c) {
        sb.append(", ");
      }
      sb.append(tag.toString());
      c = true;
    }
    return sb.append(getEnd()).toString();
  }
}
