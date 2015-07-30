/*
 * $RCSfile: RegexpSyntaxIdentifierTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/10/04 23:40:34 $
 * $Author: hfraser $
 */
package com.adaptris.core.services.routing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.ServiceException;

public class RegexpSyntaxIdentifierTest extends SyntaxIdentifierCase {

  public static final String LINE = "The quick brown fox jumps over the lazy dog";
  public static final String MATCHING_1 = ".+dog";
  public static final String MATCHING_2 = ".+lazy.+";
  public static final String UNMATCHED_1 = ".+ZZZZ.+";
  public static final String UNMATCHED_2 = ".+YYYY.+";

  private static Log logR = LogFactory.getLog(RegexpSyntaxIdentifierTest.class);

  public RegexpSyntaxIdentifierTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  public RegexpSyntaxIdentifier createIdentifier() {
    return new RegexpSyntaxIdentifier();
  }

  public void testIllegalPattern() throws Exception {
    SyntaxIdentifier ident = createIdentifier();
    ident.addPattern("\\");
    try {
      ident.isThisSyntax(LINE);
      fail();
    }
    catch (ServiceException e) {

    }
  }

  public void testSingleMatchingRegexp() throws Exception {
    SyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    assertTrue("Matches regexp", ident.isThisSyntax(LINE));
    assertTrue("Matches regexp", ident.isThisSyntax(LINE));
  }

  public void testMultipleMatchingRegexp() throws Exception {
    SyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    ident.addPattern(MATCHING_2);
    assertTrue("Matches regexp", ident.isThisSyntax(LINE));
  }

  public void testMatchingAndUnmatchedRegexp() throws Exception {
    SyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    ident.addPattern(UNMATCHED_1);
    assertTrue("Does not match regexp", !ident.isThisSyntax(LINE));
  }

  public void testSingleUnMatchingRegexp() throws Exception {
    SyntaxIdentifier ident = createIdentifier();
    ident.addPattern(UNMATCHED_1);
    assertTrue("Does not match regexp", !ident.isThisSyntax(LINE));
  }

}
