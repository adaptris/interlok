package com.adaptris.core.services.routing;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public abstract class SyntaxIdentifierImpl implements SyntaxIdentifier {
  private String destination = null;
  @XStreamImplicit(itemFieldName = "pattern")
  private List<String> patterns = null;

  public SyntaxIdentifierImpl() {
    patterns = new ArrayList<String>();
  }

  /**
   *  @see SyntaxIdentifier#setDestination(java.lang.String)
   */
  @Override
  public void setDestination(String dest) {
    if (dest == null) {
      throw new IllegalArgumentException("dest is null");
    }
    destination = dest;
  }

  /**
   *  @see SyntaxIdentifier#getDestination()
   */
  @Override
  public String getDestination() {
    return destination;
  }

  /**
   *  @see SyntaxIdentifier#addPattern(java.lang.String)
   */
  @Override
  public void addPattern(String pattern) {
    if (pattern == null) {
      throw new IllegalArgumentException("pattern is null");
    }
    patterns.add(pattern);
  }

  /**
   *  @see SyntaxIdentifier#getPatterns()
   */
  @Override
  public List<String> getPatterns() {
    return patterns;
  }

  /**
   *  @see SyntaxIdentifier#getPatterns()
   */
  @Override
  public void setPatterns(List<String> l) {
    if (l == null) {
      throw new IllegalArgumentException("list is null");
    }
    patterns = l;
  }
}
