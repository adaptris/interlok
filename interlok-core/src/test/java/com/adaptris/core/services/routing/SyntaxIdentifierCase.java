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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.Test;
import com.adaptris.core.BaseCase;
import com.adaptris.util.stream.StreamUtil;

public abstract class SyntaxIdentifierCase extends BaseCase {


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  public abstract <T extends SyntaxIdentifierImpl> T createIdentifier();

  @Test
  public void testSetPatterns() throws Exception {
    SyntaxIdentifierImpl si = createIdentifier();
    si.addPattern("ABC");
    si.addPattern("DEF");
    assertEquals(2, si.getPatterns().size());
    assertEquals("ABC", si.getPatterns().get(0));
    try {
      si.addPattern(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    assertEquals(2, si.getPatterns().size());
    assertEquals("ABC", si.getPatterns().get(0));
    try {
      si.setPatterns(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    assertEquals(2, si.getPatterns().size());
    assertEquals("ABC", si.getPatterns().get(0));
  }

  @Test
  public void testSetDestination() throws Exception {
    SyntaxIdentifierImpl si = createIdentifier();
    si.setDestination("ABC");
    assertEquals("ABC", si.getDestination());
    try {
      si.setDestination(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
    assertEquals("ABC", si.getDestination());
  }

  protected String readInput(String propertyKey) throws IOException {
    String inputFile = PROPERTIES.getProperty(propertyKey);
    StringWriter out = new StringWriter();
    StreamUtil.copyAndClose(new FileInputStream(inputFile), out);
    return out.toString();
  }
}
