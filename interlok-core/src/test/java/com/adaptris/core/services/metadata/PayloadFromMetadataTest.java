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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class PayloadFromMetadataTest extends MetadataServiceExample {

  private static final String PAYLOAD_EXP_NEWLINE_RESULT = "{ \n\"key\": \"Hello\"\n}";
  private static final String PAYLOAD_EXPR_RESULT = "{ \"key\": \"Hello\"}";
  private static final String PAYLOAD_TEMPLATE_EXP_NEWLINE = "{ \n\"key\": \"%message{helloMetadataKey}\"\n}";
  private static final String PAYLOAD_TEMPLATE_EXPR = "{ \"key\": \"%message{helloMetadataKey}\"}";
  private static final String DEFAULT_METADATA_KEY = "helloMetadataKey";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  private PayloadFromMetadataService createService() {
    KeyValuePair k = new KeyValuePair();
    k.setKey(DEFAULT_METADATA_KEY);
    k.setValue("__HELLO__");
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.addKeyValuePair(k);
    PayloadFromMetadataService service = new PayloadFromMetadataService("__HELLO__ World");
    service.setEscapeBackslash(true);
    service.setMetadataTokens(kvps);
    return service;
  }

  private AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("zzzzzzzz");
    msg.addMetadata(DEFAULT_METADATA_KEY, "Hello");
    return msg;
  }

  @Test
  public void testValid() throws Exception {
    PayloadFromMetadataService s1 = createService().withQuietMode(true);
    PayloadFromMetadataService s2 = createService().withQuietMode(false);
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
    PayloadFromMetadataService s1 = createService().withQuietMode(false);
    PayloadFromMetadataService s2 = createService().withQuietMode(true);
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
    PayloadFromMetadataService service = createService();
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(DEFAULT_METADATA_KEY, "\\tHello");
    execute(service, msg);
    assertEquals("\\tHello", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals("\\tHello World", msg.getContent());
  }

  @Test
  public void testService_MetadataValueHasSlash_NoEscape() throws Exception {
    PayloadFromMetadataService service = createService().withEscapeBackslash(false);
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(DEFAULT_METADATA_KEY, "\\tHello");
    execute(service, msg);
    assertEquals("\\tHello", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals("tHello World", msg.getContent());
  }

  @Test
  public void testService_MetadataValueHasDollar_NoEscape() throws Exception {
    PayloadFromMetadataService service = createService().withEscapeBackslash(false);
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
    PayloadFromMetadataService service = createService().withEscapeBackslash(true);
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(DEFAULT_METADATA_KEY, "Hello$");
    execute(service, msg);
    assertEquals("Hello$", msg.getMetadataValue(DEFAULT_METADATA_KEY));
    assertEquals("Hello$ World", msg.getContent());
  }

  @Test
  public void testService_Expression() throws Exception {
    PayloadFromMetadataService service = new PayloadFromMetadataService(PAYLOAD_TEMPLATE_EXPR).withQuietMode(false);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertEquals(PAYLOAD_EXPR_RESULT,msg.getContent());
  }

  @Test
  public void testService_Expression_WithNewLine() throws Exception {
    PayloadFromMetadataService s1 = new PayloadFromMetadataService(PAYLOAD_TEMPLATE_EXP_NEWLINE).withMultiLineExpression(true);
    PayloadFromMetadataService s2 = new PayloadFromMetadataService(PAYLOAD_TEMPLATE_EXP_NEWLINE).withMultiLineExpression(false);
    AdaptrisMessage msg = createMessage();
    execute(s1, msg);
    assertEquals(PAYLOAD_EXP_NEWLINE_RESULT,msg.getContent());
    execute(s2, msg);
    assertNotSame(PAYLOAD_EXP_NEWLINE_RESULT,msg.getContent());
    assertEquals(PAYLOAD_TEMPLATE_EXP_NEWLINE,msg.getContent());
    
  }  
  
  private KeyValuePairSet KVPS(KeyValuePair... kvps) {
    KeyValuePairSet res = new KeyValuePairSet();
    for(KeyValuePair kvp: kvps) {
      res.add(kvp);
    }
    return res;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createService();
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj)
        + "\n<!-- \n In this instance you have a metadata key 'helloMetadataKey' that has the contents 'Goodbye Cruel '"
        + "stored against it\n"
        + "We want to replace the existing payload with the contents of 'helloMetadataKey' along with the\n"
        + "static string 'World'\n"
        + "We configure the template with an arbitary token (bearing in mind possible REGEXP replacement issues);\n"
        + "so we have a template of __HELLO__World, where we intend to replace __HELLO__ with the contents of \n"
        + "'helloMetadataKey'. We subsequently configure the metadata-tokens so that each metadata-token contains\n"
        + "the 'metadata key we wish to use' and the value contains 'the token we wish to replace in the template'\n"
        + "\nAfter executing this service, the contents of the message will be 'Goodbye Cruel World'" + "\n"
        + "\nIf you have special characters stored in the metadata key such as '\' or '$' then escape-backslash should"
        + "\nbe set to true so that they are not treated as special characters during the replacement phase." + "\n-->\n";
  }

}
