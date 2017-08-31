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

package com.adaptris.core.services.exception;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;

public class ThrowExceptionServiceTest extends ExceptionServiceExample {

  private static final String METADATA_VALUE = "exception message from metadata value";
  private static final String METADATA_KEY = "adp.exception";
  private static final String CONFIGURED = "configured exception message";

  private static enum ExceptionGeneratorFactory {
    LastKnownException {
      @Override
      ExceptionGenerator create() {
        return new LastKnownException();
      }
    },
    ConfiguredException {
      @Override
      ExceptionGenerator create() {
        return new ConfiguredException("The Exception Message");
      }
    },
    ExceptionFromMetadata {
      @Override
      ExceptionGenerator create() {
        return new ExceptionFromMetadata("Default Exception Message", "Metadata Override For Exception Message");
      }
    },
    PossibleExceptionFromMetadata {
      @Override
      ExceptionGenerator create() {
        return new PossibleExceptionFromMetadata("Metadata Key containing Exception Message");
      }
    };
    abstract ExceptionGenerator create();
  }

  public ThrowExceptionServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testDefault() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ThrowExceptionService service = new ThrowExceptionService();
    try {
      execute(service, msg);
      fail("Expected Exception");
    }
    catch (CoreException e) {
      // expected, as by default it shouldn't initialise.
    }
  }

  public void testDefaultExceptionFromMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ThrowExceptionService service = new ThrowExceptionService(new ExceptionFromMetadata());
    try {
      execute(service, msg);
      fail("Expected Exception");
    }
    catch (ServiceException e) {
      assertNull(e.getMessage());
    }
  }

  public void testPossibleExceptionFromMetadata_NoMetadataKey() throws Exception {
    ThrowExceptionService service = new ThrowExceptionService(new PossibleExceptionFromMetadata(METADATA_KEY));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      execute(service, msg);
    }
    catch (ServiceException e) {
      fail();
    }
    try {
      service.setExceptionGenerator(new PossibleExceptionFromMetadata());
      execute(service, msg);
    }
    catch (ServiceException e) {
      fail();
    }
  }

  public void testPossibleExceptionFromMetadata_HasMetadataKey() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    ThrowExceptionService service = new ThrowExceptionService(new PossibleExceptionFromMetadata(METADATA_KEY));
    try {
      execute(service, msg);
      fail("Expected Exception");
    }
    catch (ServiceException e) {
      assertEquals(METADATA_VALUE, e.getMessage());
    }
    msg.addMetadata(METADATA_KEY, "");
    try {
      execute(service, msg);
    }
    catch (ServiceException e) {
      fail();
    }
  }

  public void testExceptionFromMetadataConfiguredMessage() throws Exception {
    ThrowExceptionService service = new ThrowExceptionService(new ExceptionFromMetadata(CONFIGURED));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      execute(service, msg);
      fail("Expected Exception");
    }
    catch (ServiceException e) {
      assertEquals(CONFIGURED, e.getMessage());
    }
  }

  public void testExceptionFromMetadataNoConfiguredMessage() throws Exception {
    ExceptionFromMetadata em = new ExceptionFromMetadata();
    em.setExceptionMessageMetadataKey(METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);

    try {
      execute(new ThrowExceptionService(em), msg);
      fail("Expected Exception");
    }
    catch (ServiceException e) {
      assertEquals(METADATA_VALUE, e.getMessage());
    }
  }

  public void testExceptionFromMetadataMetadataOverride() throws Exception {
    ThrowExceptionService service = new ThrowExceptionService(new ExceptionFromMetadata(CONFIGURED, METADATA_KEY));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    try {
      execute(service, msg);
      fail("Expected Exception");
    }
    catch (ServiceException e) {
      assertEquals(METADATA_VALUE, e.getMessage());
    }
  }

  public void testExceptionFromMetadataNoOverride() throws Exception {
    ThrowExceptionService service = new ThrowExceptionService(new ExceptionFromMetadata(CONFIGURED, METADATA_KEY));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      execute(service, msg);
      fail("Expected Exception");
    }
    catch (ServiceException e) {
      assertEquals(CONFIGURED, e.getMessage());
    }
  }

  public void testExceptionFromMetadataNoOverrideFromMetadata() throws Exception {
    ThrowExceptionService service = new ThrowExceptionService(new ExceptionFromMetadata(CONFIGURED, METADATA_KEY));

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(METADATA_KEY, "");
    try {
      execute(service, msg);
      fail("Expected Exception");
    }
    catch (ServiceException e) {
      assertEquals(CONFIGURED, e.getMessage());
    }
  }

  public void testExceptionFromConfiguredException() throws Exception {
    ThrowExceptionService service = new ThrowExceptionService(new ConfiguredException(CONFIGURED));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      execute(service, msg);
      fail("Expected Exception");
    }
    catch (ServiceException e) {
      assertEquals(CONFIGURED, e.getMessage());
    }
  }

  public void testLastKnownExceptionNoAvailableException() throws Exception {
    ThrowExceptionService service = new ThrowExceptionService(new LastKnownException());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, null);
    execute(service, msg);
  }

  public void testLastKnownExceptionAvailableException() throws Exception {
    ThrowExceptionService service = new ThrowExceptionService(new LastKnownException());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, new ServiceException(CONFIGURED));
    try {
      execute(service, msg);
      fail("Expected Exception");
    }
    catch (ServiceException e) {
      assertEquals(CONFIGURED, e.getMessage());
    }
  }

  public void testLastKnownExceptionNonServiceException() throws Exception {
    ThrowExceptionService service = new ThrowExceptionService(new LastKnownException());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception(CONFIGURED));
    try {
      execute(service, msg);
      fail("Expected Exception");
    }
    catch (ServiceException e) {
      assertEquals(CONFIGURED, e.getCause().getMessage());
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    List result = new ArrayList();
    for (ExceptionGeneratorFactory e : ExceptionGeneratorFactory.values()) {
      result.add(new ThrowExceptionService(e.create()));
    }
    return result;
  }

  @Override
  protected String createBaseFileName(Object o) {
    return super.createBaseFileName(o) + "-" + ((ThrowExceptionService) o).getExceptionGenerator().getClass().getSimpleName();
  }
}
