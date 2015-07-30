package com.adaptris.core.services.routing;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Always match.
 * <p>
 * Always returns true when isThisSyntax() is used.
 * </p>
 * 
 * @config routing-always-match-syntax-identifier
 */
@XStreamAlias("routing-always-match-syntax-identifier")
public class AlwaysMatchSyntaxIdentifier extends SyntaxIdentifierImpl {

  public AlwaysMatchSyntaxIdentifier() {
    super();
  }

  public AlwaysMatchSyntaxIdentifier(String dest) {
    this();
    setDestination(dest);
  }


  /**
   *  @see SyntaxIdentifier#isThisSyntax(java.lang.String)
   */
  @Override
  public boolean isThisSyntax(String message) {
    return true;
  }
}
