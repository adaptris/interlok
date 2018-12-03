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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.BranchingServiceExample;
import com.adaptris.core.services.LogMessageService;

public class SyntaxBranchingServiceTest extends BranchingServiceExample {

  private static final String POSTCODE_REGEXP_2 = "[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][A-Z-[CIKMOV]]{2}";
  private static final String POSTCODE_REGEXP_1 = "[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][A-Z]{2}";

  public SyntaxBranchingServiceTest(String arg0) {
    super(arg0);
  }

  @Override
  protected String createBaseFileName(Object object) {
    return SyntaxBranchingService.class.getName();
  }

  public void testSetters() throws Exception {
    SyntaxBranchingService sbs = new SyntaxBranchingService();
    sbs.addSyntaxIdentifier(new RegexpSyntaxIdentifier(Arrays.asList(new String[]
    {
        POSTCODE_REGEXP_1, POSTCODE_REGEXP_2
    }), "isPostcode"));
    assertEquals(1, sbs.getSyntaxIdentifiers().size());
    try {
      sbs.addSyntaxIdentifier(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(1, sbs.getSyntaxIdentifiers().size());
    try {
      sbs.setSyntaxIdentifiers(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(1, sbs.getSyntaxIdentifiers().size());
  }

  public void testDoServiceFirstMatch() throws Exception {
    SyntaxBranchingService sbs = new SyntaxBranchingService();
    sbs.setSyntaxIdentifiers(createStandardIdentifiers());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("GU34 1ET");
    execute(sbs, msg);
    assertEquals("isPostcode", msg.getNextServiceId());
  }

  public void testDoServiceSecondMatch() throws Exception {
    SyntaxBranchingService sbs = new SyntaxBranchingService();
    sbs.setSyntaxIdentifiers(createStandardIdentifiers());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(
        "<document><envelope>ENVELOPE</envelope><content>CONTENT</content></document>");
    execute(sbs, msg);
    assertEquals("isXml", msg.getNextServiceId());
  }

  public void testDoServiceNoMatch() throws Exception {
    SyntaxBranchingService sbs = new SyntaxBranchingService();
    sbs.setSyntaxIdentifiers(createStandardIdentifiers());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("The quick brown fox");
    try {
      execute(sbs, msg);
      fail();
    }
    catch (ServiceException e) {
      assertEquals("Unable to identify the message syntax to branch on", e.getMessage());
    }
  }

  private List<SyntaxIdentifier> createStandardIdentifiers() {
    return new ArrayList<SyntaxIdentifier>(Arrays.asList(new SyntaxIdentifier[]
    {
        new RegexpSyntaxIdentifier(new ArrayList<String>(Arrays.asList(new String[]
        {
            POSTCODE_REGEXP_1, POSTCODE_REGEXP_2
        })), "isPostcode"), new XpathSyntaxIdentifier(new ArrayList<String>(Arrays.asList(new String[]
        {
            "/document/envelope", "/document/content"
        })), "isXml"), new XpathNodeIdentifier(new ArrayList<String>(Arrays.asList(new String[]
        {
          "/document/content/content-list"
        })), "isXml")
    }));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    BranchingServiceCollection sl = new BranchingServiceCollection();
    SyntaxBranchingService sbs = new SyntaxBranchingService();
    sbs.setUniqueId("determineDocType");
    sbs.setSyntaxIdentifiers(createStandardIdentifiers());
    sbs.addSyntaxIdentifier(new AlwaysMatchSyntaxIdentifier("alwaysMatches"));
    sl.addService(sbs);
    sl.setFirstServiceId(sbs.getUniqueId());
    sl.addService(new LogMessageService("isPostcode"));
    sl.addService(new LogMessageService("isXml"));
    sl.addService(new LogMessageService("alwaysMatches"));
    return sl;
  }
}
