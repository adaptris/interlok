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
import static com.adaptris.core.transform.schema.XmlSchemaValidatorImpl.*;
import static org.junit.Assert.*;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.core.stubs.MessageHelper;
import com.adaptris.core.transform.XmlValidationService;
import com.adaptris.core.util.LifecycleHelper;

public class BasicSchemaValidationTest {
  
  @Test
  public void testBasicSchemaValidator() throws Exception {
    String schemaUrl = PROPERTIES.getProperty(KEY_WILL_VALIDATE_SCHEMA);
    BasicXmlSchemaValidator validator = new BasicXmlSchemaValidator().withSchema(schemaUrl)
        .withSchemaCache(new CacheConnection().withCacheInstance(new ExpiringMapCache()));
    XmlValidationService service = new XmlValidationService(validator);
    try {
      LifecycleHelper.initAndStart(service);
      AdaptrisMessage m1 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_INPUT_FILE));
      AdaptrisMessage m2 = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_INPUT_FILE));
      service.doService(m1);
      service.doService(m2);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }
  

  @Test(expected=ServiceException.class)
  public void testBasicSchemaValidator_Exception() throws Exception {
    String schemaUrl = PROPERTIES.getProperty(KEY_WILL_VALIDATE_SCHEMA);
    BasicXmlSchemaValidator validator = new BasicXmlSchemaValidator().withSchema(schemaUrl)
        .withSchemaCache(new CacheConnection().withCacheInstance(new ExpiringMapCache()));
    XmlValidationService service = new XmlValidationService(validator);
    try {
      LifecycleHelper.initAndStart(service);
      AdaptrisMessage m1 = new DefectiveMessageFactory(WhenToBreak.BOTH).newMessage();
      service.doService(m1);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }
  
  @Test(expected=ServiceException.class)
  public void testBasicSchemaValidator_Invalid() throws Exception {
    String schemaUrl = PROPERTIES.getProperty(KEY_WILL_NOT_VALIDATE);
    BasicXmlSchemaValidator validator = new BasicXmlSchemaValidator().withSchema(schemaUrl);
    AdaptrisMessage msg = MessageHelper.createMessage(PROPERTIES.getProperty(KEY_INPUT_FILE));
    XmlValidationService service = new XmlValidationService(validator);
    execute(service, msg);
  }

}
