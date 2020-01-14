/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.services.routing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;

public class XpathNodeIdentifierTest extends SyntaxIdentifierCase {

  private static final String INPUT_FILE = "XpathSyntaxIdentifierTest.inputFile";
  private static final String NODELIST_MATCH = "/F4FInvoice/InvoiceHeader/TradingPartner";
  private static final String SINGLENODE_MATCH = "/F4FInvoice/InvoiceHeader/TradingPartner[@PartnerType='Supplier']";
  private static final String UNMATCHING_1 = "/*/*/FRED";
  private static final String BAD_EXPR = "/*/[@PartnerType='";

  @Override
  public XpathNodeIdentifier createIdentifier() {
    return new XpathNodeIdentifier();
  }

  @Test
  public void testSetResolveAsNodeset() {
    XpathNodeIdentifier ident = createIdentifier();
    assertFalse(ident.resolveAsNodeset());
    ident.setResolveAsNodeset(Boolean.TRUE);
    assertTrue(ident.resolveAsNodeset());
    ident.setResolveAsNodeset(Boolean.FALSE);
    assertFalse(ident.resolveAsNodeset());
    ident.setResolveAsNodeset(null);
    assertFalse(ident.resolveAsNodeset());
  }

  @Test
  public void testMatchSingleNode() throws Exception {
    XpathNodeIdentifier ident = createIdentifier();
    ident.addPattern(SINGLENODE_MATCH);
    assertTrue("Xpath matches", ident.isThisSyntax(readInput(INPUT_FILE)));
  }

  @Test
  public void testMatchSingleNode_WithDocumentBuilderFactory() throws Exception {
    XpathNodeIdentifier ident = createIdentifier();
    ident.setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder.newInstance());
    ident.addPattern(SINGLENODE_MATCH);
    assertTrue("Xpath matches", ident.isThisSyntax(readInput(INPUT_FILE)));
  }

  @Test
  public void testMatchSingleNode_asNodeSet() throws Exception {
    XpathNodeIdentifier ident = createIdentifier();
    ident.addPattern(SINGLENODE_MATCH);
    ident.setResolveAsNodeset(true);
    assertTrue("Xpath matches", ident.isThisSyntax(readInput(INPUT_FILE)));
  }

  @Test
  public void testMatchNodeset_asSingleNode() throws Exception {
    XpathNodeIdentifier ident = createIdentifier();
    ident.addPattern(NODELIST_MATCH);
    ident.setResolveAsNodeset(false);
    assertTrue("Xpath matches", ident.isThisSyntax(readInput(INPUT_FILE)));
  }

  @Test
  public void testMatchNodeset_asNodeset() throws Exception {
    XpathNodeIdentifier ident = createIdentifier();
    ident.addPattern(NODELIST_MATCH);
    ident.setResolveAsNodeset(true);
    assertTrue("Xpath matches", ident.isThisSyntax(readInput(INPUT_FILE)));
  }

  @Test
  public void testMatchingAndUnmatchedRegexp() throws Exception {
    XpathNodeIdentifier ident = createIdentifier();
    ident.addPattern(SINGLENODE_MATCH);
    ident.addPattern(UNMATCHING_1);
    assertTrue("Xpath does not match", !ident.isThisSyntax(readInput(INPUT_FILE)));
  }

  @Test
  public void testSingleUnMatchingRegexp() throws Exception {
    XpathNodeIdentifier ident = createIdentifier();
    ident.addPattern(UNMATCHING_1);
    assertTrue("Xpath does not match", !ident.isThisSyntax(readInput(INPUT_FILE)));
  }

  @Test
  public void testBadExpression() throws Exception {
    XpathNodeIdentifier ident = createIdentifier();
    ident.addPattern(BAD_EXPR);
    try {
      ident.isThisSyntax(readInput(INPUT_FILE));
      fail();
    }
    catch (ServiceException expected) {
    }

  }
}
