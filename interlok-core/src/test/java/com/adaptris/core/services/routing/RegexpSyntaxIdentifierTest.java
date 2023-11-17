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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.adaptris.core.ServiceException;

public class RegexpSyntaxIdentifierTest extends SyntaxIdentifierCase {

  public static final String LINE = "The quick brown fox jumps over the lazy dog";
  public static final String MATCHING_1 = ".+dog";
  public static final String MATCHING_2 = ".+lazy.+";
  public static final String UNMATCHED_1 = ".+ZZZZ.+";
  public static final String UNMATCHED_2 = ".+YYYY.+";

  @Override
  public RegexpSyntaxIdentifier createIdentifier() {
    return new RegexpSyntaxIdentifier();
  }

  @Test
  public void testIllegalPattern() throws Exception {
    RegexpSyntaxIdentifier ident = createIdentifier();
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
    RegexpSyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    assertTrue(ident.isThisSyntax(LINE));
    assertTrue(ident.isThisSyntax(LINE));
  }

  @Test
  public void testMultipleMatchingRegexp() throws Exception {
    RegexpSyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    ident.addPattern(MATCHING_2);
    assertTrue(ident.isThisSyntax(LINE));
  }

  @Test
  public void testMatchingAndUnmatchedRegexp() throws Exception {
    RegexpSyntaxIdentifier ident = createIdentifier();
    ident.addPattern(MATCHING_1);
    ident.addPattern(UNMATCHED_1);
    assertTrue(!ident.isThisSyntax(LINE));
  }

  @Test
  public void testSingleUnMatchingRegexp() throws Exception {
    RegexpSyntaxIdentifier ident = createIdentifier();
    ident.addPattern(UNMATCHED_1);
    assertTrue(!ident.isThisSyntax(LINE));
  }

}
