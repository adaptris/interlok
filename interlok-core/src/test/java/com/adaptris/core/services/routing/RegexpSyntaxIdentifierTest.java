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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import com.adaptris.core.ServiceException;

public class RegexpSyntaxIdentifierTest extends SyntaxIdentifierCase {

  public static final String LINE = "The quick brown fox jumps over the lazy dog";
  public static final String MATCHING_1 = ".+dog";
  public static final String MATCHING_2 = ".+lazy.+";
  public static final String UNMATCHED_1 = ".+ZZZZ.+";
  public static final String UNMATCHED_2 = ".+YYYY.+";

  private static Log logR = LogFactory.getLog(RegexpSyntaxIdentifierTest.class);


  @Override
  public RegexpSyntaxIdentifier createIdentifier() {
    return new RegexpSyntaxIdentifier();
  }

  @Test
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

  @Test
  public void testSingleMatchingRegexp() throws Exception {
    SyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    assertTrue("Matches regexp", ident.isThisSyntax(LINE));
    assertTrue("Matches regexp", ident.isThisSyntax(LINE));
  }

  @Test
  public void testMultipleMatchingRegexp() throws Exception {
    SyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    ident.addPattern(MATCHING_2);
    assertTrue("Matches regexp", ident.isThisSyntax(LINE));
  }

  @Test
  public void testMatchingAndUnmatchedRegexp() throws Exception {
    SyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    ident.addPattern(UNMATCHED_1);
    assertTrue("Does not match regexp", !ident.isThisSyntax(LINE));
  }

  @Test
  public void testSingleUnMatchingRegexp() throws Exception {
    SyntaxIdentifier ident = createIdentifier();
    ident.addPattern(UNMATCHED_1);
    assertTrue("Does not match regexp", !ident.isThisSyntax(LINE));
  }

}
