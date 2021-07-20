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

package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.services.metadata.MetadataServiceExample;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

public class PayloadFromPayloadTest extends MetadataServiceExample
{

  private static final String PAYLOAD_EXP_NEWLINE_RESULT = "{ \n\"key\": \"Hello\"\n}";
  private static final String PAYLOAD_EXPR_RESULT = "{ \"key\": \"Hello\"}";
  private static final String PAYLOAD_TEMPLATE_EXP_NEWLINE = "{ \n\"key\": \"%message{helloMetadataKey}\"\n}";
  private static final String PAYLOAD_TEMPLATE_EXPR = "{ \"key\": \"%message{helloMetadataKey}\"}";
  private static final String DEFAULT_METADATA_KEY = "helloMetadataKey";


  private PayloadFromPayloadService createService() {
    Map<String, String> map = new HashMap<>();
    map.put(DEFAULT_METADATA_KEY, "__HELLO__");
    return new PayloadFromPayloadService().withQuoteReplacement(true).withMetadataTokens(map);
  }

  private AdaptrisMessage createMessage() {
    return createMessage("__HELLO__ World");
  }

  private AdaptrisMessage createMessage(String template) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(template);
    msg.addMetadata(DEFAULT_METADATA_KEY, "Hello");
    return msg;
  }

  @Test
  public void testValid() throws Exception {
    PayloadFromPayloadService s1 = createService().withQuietMode(true);
    PayloadFromPayloadService s2 = createService().withQuietMode(false);
    AdaptrisMessage msg = createMessage();
    execute(s1, msg);
    assertEquals("Hello", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals("Hello World", msg.getContent());
    execute(s2, msg);
    assertEquals("Hello", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals("Hello World", msg.getContent());
  }

  @Test
  public void testNoMetadataMatch() throws Exception {
    PayloadFromPayloadService s1 = createService().withQuietMode(false);
    PayloadFromPayloadService s2 = createService().withQuietMode(true);
    AdaptrisMessage msg = createMessage();
    msg.removeMetadata(new MetadataElement(DEFAULT_METADATA_KEY, ""));
    execute(s1, msg);
    assertNotSame("Hello World", msg.getContent());
    assertEquals("__HELLO__ World", msg.getContent());
    execute(s2, msg);
    assertNotSame("Hello World", msg.getContent());
    assertEquals("__HELLO__ World", msg.getContent());
  }

  @Test
  public void testService_MetadataValueHasSlash_CanEscape() throws Exception {
    PayloadFromPayloadService service = createService();
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(DEFAULT_METADATA_KEY, "\\tHello");
    execute(service, msg);
    assertEquals("\\tHello", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals("\\tHello World", msg.getContent());
  }

  @Test
  public void testService_MetadataValueHasSlash_NoEscape() throws Exception {
    PayloadFromPayloadService service = createService().withQuoteReplacement(false);
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(DEFAULT_METADATA_KEY, "\\tHello");
    execute(service, msg);
    assertEquals("\\tHello", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals("tHello World", msg.getContent());
  }

  @Test
  public void testService_MetadataValueHasDollar_NoEscape() throws Exception {
    PayloadFromPayloadService service = createService().withQuoteReplacement(false);
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(DEFAULT_METADATA_KEY, "Hello$");
    try {
      execute(service, msg);
      fail();
    }
    catch (IllegalArgumentException | StringIndexOutOfBoundsException expected) {
      // StringIndexOutOfBounds
    }
    msg.addMetadata(DEFAULT_METADATA_KEY, "Hel$lo");
    try {
      execute(service, msg);
      fail();
    }
    catch (IllegalArgumentException expected) {
      // Should fail with either an illegal group ref IllegalArgumentException
    }
    msg.addMetadata(DEFAULT_METADATA_KEY, "$Hello");
    try {
      execute(service, msg);
      fail();
    }
    catch (IllegalArgumentException expected) {
      // Should fail with either an illegal group ref IllegalArgumentException
    }
  }

  @Test
  public void testService_MetadataValueHasDollar_CanEscape() throws Exception {
    PayloadFromPayloadService service = createService().withQuoteReplacement(true);
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(DEFAULT_METADATA_KEY, "Hello$");
    execute(service, msg);
    assertEquals("Hello$", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals("Hello$ World", msg.getContent());
  }

  @Test
  public void testService_Expression() throws Exception {
    PayloadFromPayloadService service = createService().withQuietMode(false);
    AdaptrisMessage msg = createMessage(PAYLOAD_TEMPLATE_EXPR);
    execute(service, msg);
    assertEquals(PAYLOAD_EXPR_RESULT, msg.getContent());
  }

  @Test
  public void testService_Expression_WithNewLine() throws Exception {
    PayloadFromPayloadService s1 = new PayloadFromPayloadService().withMultiLineExpression(true);
    PayloadFromPayloadService s2 = new PayloadFromPayloadService().withMultiLineExpression(false);
    AdaptrisMessage msg = createMessage(PAYLOAD_TEMPLATE_EXP_NEWLINE);
    execute(s1, msg);
    assertEquals(PAYLOAD_EXP_NEWLINE_RESULT, msg.getContent());
    msg = createMessage(PAYLOAD_TEMPLATE_EXP_NEWLINE);
    execute(s2, msg);
    assertNotSame(PAYLOAD_EXP_NEWLINE_RESULT, msg.getContent());
    assertEquals(PAYLOAD_TEMPLATE_EXP_NEWLINE, msg.getContent());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new PayloadFromPayloadService().withMultiLineExpression(true);
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj)
        + "\n<!-- \n In this instance you have a metadata key 'helloMetadataKey' that has the contents 'Goodbye Cruel '"
        + "stored against it\n"
        + "We want to replace the existing payload with the contents of 'helloMetadataKey' along with the\n"
        + "static string 'World'. Since the template extends over multiple lines, we set multi-line-expression to\n"
        + "be true"
        + "\n-->\n";
  }

}
