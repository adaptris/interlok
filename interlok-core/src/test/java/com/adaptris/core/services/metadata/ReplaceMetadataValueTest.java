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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CloneMessageServiceList;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;

public class ReplaceMetadataValueTest extends MetadataServiceExample {

  private static final String VALUE = "The Quick Brown Fox Jumps Over The Lazy Dog";
  private static final String SEARCH_VALUE_RAW = "The";
  private static final String SEARCH_VALUE_REGEXP = "^(.*)Fox(.*)$";
  private static final String SEARCH_VALUE_REGEXP_MATCH_GROUP = "(.*) Jumps Over (.*)";

  private static final String REPLACEMENT_VALUE_RAW = "THE";
  private static final String REPLACEMENT_VALUE_REGEXP = "Quick zephyrs blow, vexing daft Jim";
  private static final String REPLACEMENT_MATCH_GROUP = "{2} Jumps Over {1}";
  private static final String JAVA_REGEX_REPLACEMENT_MATCH_GROUP = "$2 Jumps Over $1";


  private static final String EXPECTED_RAW_RESULT_ALL = "THE Quick Brown Fox Jumps Over THE Lazy Dog";
  private static final String EXPECTED_RAW_RESULT_FIRST = "THE Quick Brown Fox Jumps Over The Lazy Dog";

  private static final String EXPECTED_REGEXP_RESULT_ALL = "Quick zephyrs blow, vexing daft Jim";

  private static final String EXPECTED_REGEXP_RESULT_MG_FIRST = "The Lazy Dog Jumps Over The Quick Brown Fox";

  private static final String MATCHING_METADATA_KEY = "matchingMetadataKey.*";
  private static final String MATCHING_METADATA_KEY1 = "matchingMetadataKey1";
  private static final String MATCHING_METADATA_KEY2 = "matchingMetadataKey2";
  private static final String NON_MATCHING_KEY = "nonMatchingMetadataKey";


  private AdaptrisMessage createMessage() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
    m.addMetadata(new MetadataElement(MATCHING_METADATA_KEY1, VALUE));
    m.addMetadata(new MetadataElement(MATCHING_METADATA_KEY2, VALUE));
    m.addMetadata(new MetadataElement(NON_MATCHING_KEY, VALUE));
    return m;
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testInit() throws Exception {
    ReplaceMetadataValue service = new ReplaceMetadataValue();
    try {
      service.init();
      fail();
    }
    catch (CoreException expected) {
      ;
    }
    service.setReplacementValue(REPLACEMENT_VALUE_RAW);
    try {
      service.init();
      fail();
    }
    catch (CoreException expected) {
      ;
    }
    service.setSearchValue(SEARCH_VALUE_RAW);
    service.setReplacementValue(REPLACEMENT_VALUE_RAW);
    try {
      service.init();
    }
    catch (CoreException expected) {
      ;
    }
  }

  @Test
  public void testReplaceFirstRaw() throws Exception {
    AdaptrisMessage m = createMessage();
    ReplaceMetadataValue service = new ReplaceMetadataValue(MATCHING_METADATA_KEY, SEARCH_VALUE_RAW, false, REPLACEMENT_VALUE_RAW);
    execute(service, m);
    assertEquals(EXPECTED_RAW_RESULT_FIRST, m.getMetadataValue(MATCHING_METADATA_KEY1));
    assertEquals(EXPECTED_RAW_RESULT_FIRST, m.getMetadataValue(MATCHING_METADATA_KEY2));
    assertEquals(VALUE, m.getMetadataValue(NON_MATCHING_KEY));
  }

  @Test
  public void testReplaceAllRaw() throws Exception {
    AdaptrisMessage m = createMessage();
    ReplaceMetadataValue service = new ReplaceMetadataValue(MATCHING_METADATA_KEY, SEARCH_VALUE_RAW, true, REPLACEMENT_VALUE_RAW);
    execute(service, m);
    assertEquals(EXPECTED_RAW_RESULT_ALL, m.getMetadataValue(MATCHING_METADATA_KEY1));
    assertEquals(EXPECTED_RAW_RESULT_ALL, m.getMetadataValue(MATCHING_METADATA_KEY2));
    assertEquals(VALUE, m.getMetadataValue(NON_MATCHING_KEY));
  }

  @Test
  public void testReplaceAllRaw_Resolved() throws Exception {
    AdaptrisMessage m = createMessage();
    m.addMetadata("ResolvedKey", REPLACEMENT_VALUE_RAW);
    ReplaceMetadataValue service = new ReplaceMetadataValue(MATCHING_METADATA_KEY, SEARCH_VALUE_RAW, true, "%message{ResolvedKey}");
    execute(service, m);
    assertEquals(EXPECTED_RAW_RESULT_ALL, m.getMetadataValue(MATCHING_METADATA_KEY1));
    assertEquals(EXPECTED_RAW_RESULT_ALL, m.getMetadataValue(MATCHING_METADATA_KEY2));
    assertEquals(VALUE, m.getMetadataValue(NON_MATCHING_KEY));
  }

  @Test
  public void testReplaceRegexp() throws Exception {
    AdaptrisMessage m = createMessage();
    ReplaceMetadataValue service = new ReplaceMetadataValue(MATCHING_METADATA_KEY, SEARCH_VALUE_REGEXP, true,
        REPLACEMENT_VALUE_REGEXP);
    execute(service, m);
    assertEquals(EXPECTED_REGEXP_RESULT_ALL, m.getMetadataValue(MATCHING_METADATA_KEY1));
    assertEquals(EXPECTED_REGEXP_RESULT_ALL, m.getMetadataValue(MATCHING_METADATA_KEY2));
    assertEquals(VALUE, m.getMetadataValue(NON_MATCHING_KEY));
  }

  @Test
  public void testReplaceRegexpMatchGroup() throws Exception {
    AdaptrisMessage m = createMessage();
    log.warn(m);
    ReplaceMetadataValue service = new ReplaceMetadataValue(MATCHING_METADATA_KEY, SEARCH_VALUE_REGEXP_MATCH_GROUP, false,
        REPLACEMENT_MATCH_GROUP);
    execute(service, m);
    assertEquals(EXPECTED_REGEXP_RESULT_MG_FIRST, m.getMetadataValue(MATCHING_METADATA_KEY1));
    assertEquals(EXPECTED_REGEXP_RESULT_MG_FIRST, m.getMetadataValue(MATCHING_METADATA_KEY2));
    assertEquals(VALUE, m.getMetadataValue(NON_MATCHING_KEY));
  }

  @Test
  public void testReplaceRegexpMatchGroup_JavaRegxp() throws Exception {
    AdaptrisMessage m = createMessage();
    log.warn(m);
    ReplaceMetadataValue service = new ReplaceMetadataValue(MATCHING_METADATA_KEY, SEARCH_VALUE_REGEXP_MATCH_GROUP, false,
        JAVA_REGEX_REPLACEMENT_MATCH_GROUP);
    execute(service, m);
    assertEquals(EXPECTED_REGEXP_RESULT_MG_FIRST, m.getMetadataValue(MATCHING_METADATA_KEY1));
    assertEquals(EXPECTED_REGEXP_RESULT_MG_FIRST, m.getMetadataValue(MATCHING_METADATA_KEY2));
    assertEquals(VALUE, m.getMetadataValue(NON_MATCHING_KEY));
  }

  @Test
  public void testReplaceRegexpMatchGroupNoMatch() throws Exception {
    AdaptrisMessage m = createMessage();
    ReplaceMetadataValue service = new ReplaceMetadataValue(MATCHING_METADATA_KEY, "ABCDEFG", true,
        REPLACEMENT_MATCH_GROUP);
    execute(service, m);
    assertEquals(VALUE, m.getMetadataValue(MATCHING_METADATA_KEY1));
    assertEquals(VALUE, m.getMetadataValue(MATCHING_METADATA_KEY2));
    assertEquals(VALUE, m.getMetadataValue(NON_MATCHING_KEY));
  }

  @Test
  public void testReplaceSearchValueNoMatch() throws Exception {
    AdaptrisMessage m = createMessage();
    ReplaceMetadataValue service = new ReplaceMetadataValue(MATCHING_METADATA_KEY, "ABCDEFG", true, REPLACEMENT_VALUE_RAW);
    execute(service, m);
    assertEquals(VALUE, m.getMetadataValue(MATCHING_METADATA_KEY1));
    assertEquals(VALUE, m.getMetadataValue(MATCHING_METADATA_KEY2));
    assertEquals(VALUE, m.getMetadataValue(NON_MATCHING_KEY));
  }

  // This is Interlok-2129
  @Test
  public void testReplaceValue_InsideClonedService() throws Exception {
    AdaptrisMessage m = createMessage();
    ReplaceMetadataValue nested = new ReplaceMetadataValue(MATCHING_METADATA_KEY, SEARCH_VALUE_REGEXP_MATCH_GROUP, false,
        JAVA_REGEX_REPLACEMENT_MATCH_GROUP);
    CloneMessageServiceList service = new CloneMessageServiceList(nested);
    execute(service, m);
    // At this point we expected the matching keys to still be "value".
    assertEquals(VALUE, m.getMetadataValue(MATCHING_METADATA_KEY1));
    assertEquals(VALUE, m.getMetadataValue(MATCHING_METADATA_KEY2));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    ReplaceMetadataValue s = new ReplaceMetadataValue("MetadataKeysWhoseValuesYouWishToReplace.*", SEARCH_VALUE_REGEXP, false,
        "{1}Aardvark{2}");
    return s;
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!-- This can be used to perform simple find and replace operations on metadata \n"
        + "or you can use the match group functionality for additional complexity\n"
        + "For instance. If the metadata value is 'The Quick Brown Fox Jumps Over The Lazy Dog' and you wanted to\n"
        + "make it 'The Lazy Dog Jumps Over The Quick Brown Fox' then the\n" + "search value should be '(.*) Jumps Over (.*)'\n"
        + "and the replacement value could be '{2} Jumps Over {1}'\n"
        + "Of course, that is a fairly meaningless example, simple example of the use of match groups.\n" + "-->\n";
  }

}
