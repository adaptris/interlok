package com.adaptris.core.services.routing;

import com.adaptris.core.ServiceException;

public class XpathSyntaxIdentifierTest extends SyntaxIdentifierCase {

  private static final String INPUT_FILE = "XpathSyntaxIdentifierTest.inputFile";
  private static final String MATCHING_1 = "/*/*/TradingPartner/PartnerID";
  private static final String MATCHING_2 = "/*/*/TradingPartner[@PartnerType="
      + "'Supplier']/PartnerID[@PartnerIDType='Assigned by F4F']";
  private static final String UNMATCHING_1 = "/*/*/FRED";
  private static final String BAD_EXPR = "/*/[@PartnerType='";

  private static final String NON_TEXT_NODE = "/*/BESTELLUNG";

  private static final String NON_TEXT_NODE_INPUT = "XpathSyntaxIdentifierTest.non_text_node_input";

  public XpathSyntaxIdentifierTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  public XpathSyntaxIdentifier createIdentifier() {
    return new XpathSyntaxIdentifier();
  }

  public void testSingleMatchingRegexp() throws Exception {
    XpathSyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    assertTrue("Xpath matches", ident.isThisSyntax(readInput(INPUT_FILE)));
  }

  public void testMultipleMatchingRegexp() throws Exception {
    XpathSyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    ident.addPattern(MATCHING_2);
    assertTrue("Xpath matches", ident.isThisSyntax(readInput(INPUT_FILE)));
  }

  public void testMatchingAndUnmatchedRegexp() throws Exception {
    XpathSyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    ident.addPattern(UNMATCHING_1);
    assertTrue("Xpath does not match", !ident.isThisSyntax(readInput(INPUT_FILE)));
  }

  public void testSingleUnMatchingRegexp() throws Exception {
    XpathSyntaxIdentifier ident = createIdentifier();
    ident.addPattern(UNMATCHING_1);
    assertTrue("Xpath does not match", !ident.isThisSyntax(readInput(INPUT_FILE)));
  }

  public void testRedmineIssue1020() throws Exception {
    testNonTextNode();
  }

  public void testNonTextNode() throws Exception {
    XpathSyntaxIdentifier ident = createIdentifier();
    ident.addPattern(NON_TEXT_NODE);
    assertTrue("Xpath matches", ident.isThisSyntax(readInput(NON_TEXT_NODE_INPUT)));
  }

  public void testBadExpression() throws Exception {
    XpathSyntaxIdentifier ident = createIdentifier();
    ident.addPattern(BAD_EXPR);
    try {
      ident.isThisSyntax(readInput(INPUT_FILE));
      fail();
    }
    catch (ServiceException expected) {
    }

  }
}
