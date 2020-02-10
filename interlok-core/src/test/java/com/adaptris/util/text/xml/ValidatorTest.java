/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.util.text.xml;

import java.io.InputStream;
import java.io.Reader;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BaseCase;
import com.adaptris.core.stubs.MessageHelper;
import com.adaptris.core.transform.XmlValidationServiceTest;

public class ValidatorTest extends BaseCase {


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  
  @Test
  public void testValidatorString() throws Exception {
    Validator v = createValidator(null);
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_INPUT_FILE));
    try (InputStream in = msg.getInputStream()) {
      Document doc = v.parse(in);
    }
    try (Reader in = msg.getReader()) {
      Document doc = v.parse(in);
    }
  }

  @Test
  public void testValidatorStringEntityResolver() throws Exception {
    Validator v = createValidator(new Resolver());
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_INPUT_FILE));
    try (InputStream in = msg.getInputStream()) {
      Document doc = v.parse(in);
    }
    try (Reader in = msg.getReader()) {
      Document doc = v.parse(in);
    }
  }

  private Validator createValidator(EntityResolver resolver) throws Exception {
    Validator result = null;
    if (resolver == null) {
      result = new Validator(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_WILL_VALIDATE_SCHEMA));
    }
    else {
      result = new Validator(PROPERTIES.getProperty(XmlValidationServiceTest.KEY_WILL_VALIDATE_SCHEMA), resolver);
    }
    return result;
  }
}
