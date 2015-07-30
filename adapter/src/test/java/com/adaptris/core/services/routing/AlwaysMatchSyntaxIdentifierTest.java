/*
 * $RCSfile: RegexpSyntaxIdentifierTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/10/04 23:40:34 $
 * $Author: hfraser $
 */
package com.adaptris.core.services.routing;


public class AlwaysMatchSyntaxIdentifierTest extends SyntaxIdentifierCase {

  public static final String LINE = "The quick brown fox jumps over the lazy dog";

  public AlwaysMatchSyntaxIdentifierTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  public AlwaysMatchSyntaxIdentifier createIdentifier() {
    return new AlwaysMatchSyntaxIdentifier();
  }

  public void testMatch() throws Exception {
    AlwaysMatchSyntaxIdentifier ident = createIdentifier();
    assertTrue("Matches regexp", ident.isThisSyntax(LINE));
  }

}
