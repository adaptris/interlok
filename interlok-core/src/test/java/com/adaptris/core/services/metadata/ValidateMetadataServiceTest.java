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

import static org.junit.Assert.fail;
import java.util.Arrays;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;

public class ValidateMetadataServiceTest extends MetadataServiceExample {
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testSetters() throws Exception {
    ValidateMetadataService service = new ValidateMetadataService();
    try {
      service.addRequiredKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {
      ;
    }
    try {
      service.addRequiredKey(null);
      fail();
    }
    catch (IllegalArgumentException expected) {
      ;
    }
  }

  @Test
  public void testNoRequirements() throws Exception {
    ValidateMetadataService service = new ValidateMetadataService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk");
    msg.addMetadata("key", "val");
    try {
      execute(service, msg);
    }
    catch (ServiceException e) {
      fail();
    }
  }

  @Test
  public void testOneRequirementMet() throws Exception {
    ValidateMetadataService service = new ValidateMetadataService(Arrays.asList(new String[]
    {
      "key"
    }));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk");
    msg.addMetadata("key", "val");

    try {
      execute(service, msg);
    }
    catch (ServiceException e) {
      fail();
    }
  }

  @Test
  public void testOneRequirementEmpty() throws Exception {
    ValidateMetadataService service = new ValidateMetadataService(Arrays.asList(new String[]
    {
      "key"
    }));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk");
    msg.addMetadata("key", "");

    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException e) {
      // expected
    }
  }

  @Test
  public void testOneRequirementNotPresent() throws Exception {
    ValidateMetadataService service = new ValidateMetadataService(Arrays.asList(new String[]
    {
      "key"
    }));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk");

    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException e) {
      // expected
    }
  }

  @Test
  public void testtwoRequirementsNonePresent() throws Exception {
    ValidateMetadataService service = new ValidateMetadataService(Arrays.asList(new String[]
    {
        "key", "key2"
    }));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk");

    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException e) {
      // expected
    }
  }

  @Test
  public void testtwoRequirementsOnePresent() throws Exception {
    ValidateMetadataService service = new ValidateMetadataService(Arrays.asList(new String[]
    {
        "key", "key2"
    }));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk");

    msg.addMetadata("key", "val");

    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException e) {
      // expected
    }
  }

  @Test
  public void testtwoRequirementsTwoPresent() throws Exception {
    ValidateMetadataService service = new ValidateMetadataService(Arrays.asList(new String[]
    {
        "key", "key2"
    }));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("asdfghjk");

    msg.addMetadata("key", "val");
    msg.addMetadata("key2", "val");

    try {
      execute(service, msg);
    }
    catch (ServiceException e) {
      fail();
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    ValidateMetadataService service = new ValidateMetadataService();
    service.addRequiredKey("a-required-metadata-key");
    service.addRequiredKey("another-required-metadata-key");
    service.addRequiredKey("yet-another-required-metadata-key");
    return service;
  }
}
