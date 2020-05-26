/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core.transform.schema;

import static com.adaptris.core.BaseCase.PROPERTIES;
import static com.adaptris.core.ServiceCase.execute;
import static com.adaptris.core.transform.XmlValidationServiceTest.KEY_INPUT_FILE;
import static com.adaptris.core.transform.XmlValidationServiceTest.KEY_WILL_NOT_VALIDATE;
import static com.adaptris.core.transform.XmlValidationServiceTest.KEY_WILL_VALIDATE_SCHEMA;
import static org.junit.Assert.*;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.core.stubs.MessageHelper;
import com.adaptris.core.transform.XmlValidationService;

public class ExtendedSchemaValidatorTest {

  @Test
  public void testExtendedValidator_Valid() throws Exception {
    String schemaUrl = PROPERTIES.getProperty(KEY_WILL_VALIDATE_SCHEMA);
    ExtendedXmlSchemaValidator validator = new ExtendedXmlSchemaValidator()
        .withSchemaViolationHandler(new ViolationsAsMetadata()).withSchema(schemaUrl);
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_INPUT_FILE));
    XmlValidationService service = new XmlValidationService(validator);
    execute(service, msg);
    assertFalse(msg.headersContainsKey(ViolationHandlerImpl.DEFAULT_KEY));
  }

  @Test
  public void testExtendedValidator_Invalid() throws Exception {
    String schemaUrl = PROPERTIES.getProperty(KEY_WILL_NOT_VALIDATE);
    ExtendedXmlSchemaValidator validator = new ExtendedXmlSchemaValidator()
        .withSchemaViolationHandler(new ViolationsAsMetadata()).withSchema(schemaUrl);
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_INPUT_FILE));
    XmlValidationService service = new XmlValidationService(validator);
    execute(service, msg);
    assertTrue(msg.headersContainsKey(ViolationHandlerImpl.DEFAULT_KEY));
    SchemaViolations v = (SchemaViolations) new XStreamMarshaller()
        .unmarshal(msg.getMetadataValue(ViolationHandlerImpl.DEFAULT_KEY));
    assertEquals(2, v.getViolations().size());
  }

}
