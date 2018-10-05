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

package com.adaptris.mail;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MatchProxyFactoryTest extends MatchProxyFactory {

  private static final String[][] VALID_PATTERNS = {
      new String[] { "Regex", ".*"},
      new String[] { "java.util.regex.Pattern", ".*"},
      new String[] { "Awk", "Awk*"},
      new String[] { "org.apache.oro.text.awk.AwkCompiler", "Awk*"},
      new String[] { "Glob", "Glob*"},
      new String[] { "org.apache.oro.text.GlobCompiler", "Glob*"},
      new String[] { "Perl5", ".*"},
      new String[] { "org.apache.oro.text.regex.Perl5Compiler", ".*"}
  };

  @Test
  public void testBuild() throws Exception {
    for (String[] p : VALID_PATTERNS) {
      assertTrue(MatchProxy.class.isAssignableFrom(create(p[0], p[1]).getClass()));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuildInvalid() throws Exception {
    create("hello", "world");
  }

}
