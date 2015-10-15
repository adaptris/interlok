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

package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;

public class MetadataFilterServiceTest extends MetadataServiceExample {

  private static final String MATCH_ANY = ".*";
  private static final String JMS_KEY_3 = "JMSTimestamp";
  private static final String JMS_KEY_2 = "JMS_Mabc";
  private static final String JMS_KEY_1 = "JMSXabc";
  private static final String JMS_MESSAGE_ID = "JMSMessageId";
  private static final String DEF_VALUE = "123";
  private static final String REG_EXP = "JMS[^M]\\w+";

  public MetadataFilterServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }


  public void testSetFilter() {
    MetadataFilterService service = new MetadataFilterService();
    assertEquals(NoOpMetadataFilter.class, service.getFilter().getClass());
    service.setFilter(new RegexMetadataFilter());
    assertEquals(RegexMetadataFilter.class, service.getFilter().getClass());
    try {
      service.setFilter(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(RegexMetadataFilter.class, service.getFilter().getClass());

  }

  public void testServiceNoOp() throws Exception {
    AdaptrisMessage msg = createMessage();
    MetadataFilterService service = new MetadataFilterService();
    execute(service, msg);
    assertEquals(DEF_VALUE, msg.getMetadataValue(JMS_MESSAGE_ID));
    assertEquals(DEF_VALUE, msg.getMetadataValue(JMS_KEY_1));
    assertEquals(DEF_VALUE, msg.getMetadataValue(JMS_KEY_2));
    assertEquals(DEF_VALUE, msg.getMetadataValue(JMS_KEY_3));
  }

  public void testServiceExclude() throws Exception {

    AdaptrisMessage msg = createMessage();
    MetadataFilterService service = new MetadataFilterService();
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addExcludePattern(REG_EXP);
    service.setFilter(filter);

    execute(service, msg);
    assertEquals(DEF_VALUE, msg.getMetadataValue(JMS_MESSAGE_ID));
    assertFalse(msg.containsKey(JMS_KEY_1));
    assertFalse(msg.containsKey(JMS_KEY_2));
    assertFalse(msg.containsKey(JMS_KEY_3));
  }

  public void testServiceInclude() throws Exception {
    AdaptrisMessage msg = createMessage();
    MetadataFilterService service = new MetadataFilterService();
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addIncludePattern(REG_EXP);
    service.setFilter(filter);

    execute(service, msg);
    assertFalse(msg.containsKey(JMS_MESSAGE_ID));
    assertEquals(DEF_VALUE, msg.getMetadataValue(JMS_KEY_1));
    assertEquals(DEF_VALUE, msg.getMetadataValue(JMS_KEY_2));
    assertEquals(DEF_VALUE, msg.getMetadataValue(JMS_KEY_3));
  }


  public void testServiceIncludesAndExcludes() throws Exception {
    AdaptrisMessage msg = createMessage();
    MetadataFilterService service = new MetadataFilterService();
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addExcludePattern(REG_EXP);
    filter.addIncludePattern(MATCH_ANY);
    service.setFilter(filter);

    execute(service, msg);
    assertEquals(DEF_VALUE, msg.getMetadataValue(JMS_MESSAGE_ID));
    assertFalse(msg.containsKey(JMS_KEY_1));
    assertFalse(msg.containsKey(JMS_KEY_2));
    assertFalse(msg.containsKey(JMS_KEY_3));
  }

  private AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("zzzzzzzz");
    msg.addMetadata(JMS_KEY_1, DEF_VALUE);
    msg.addMetadata(JMS_MESSAGE_ID, DEF_VALUE);
    msg.addMetadata(JMS_KEY_2, DEF_VALUE);
    msg.addMetadata(JMS_KEY_3, DEF_VALUE);
    return msg;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    MetadataFilterService service = new MetadataFilterService();
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addExcludePattern(REG_EXP);
    filter.addIncludePattern(MATCH_ANY);
    filter.addExcludePattern("ANOTHER_REGULAR_EXPRESSION");
    service.setFilter(filter);
    return service;
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!--"
        + "\nAlthough the filter specified here has an explict <include> element; it is not required."
        + "\nIf you do not configure an <include> element then implicitly all metadata elements"
        + "\nare included (which matches the regular expression in the actual <include> element)"
        + "\n\nThe sequence of operations for the service is to"
        + "\na) Generate a subset of all metadata elements where the metadata key matches any of <include> elements."
        + "\nb) Take this subset and remove all keys that match any of the <exclude> elements." + "\n-->\n";
  }

}
