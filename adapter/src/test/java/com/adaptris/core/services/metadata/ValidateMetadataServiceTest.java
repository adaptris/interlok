/*
 * $RCSfile: ValidateMetadataServiceTest.java,v $
 * $Revision: 1.6 $
 * $Date: 2008/08/13 13:28:42 $
 * $Author: lchan $
 */
package com.adaptris.core.services.metadata;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;

public class ValidateMetadataServiceTest extends MetadataServiceExample {

  public ValidateMetadataServiceTest(String name) {
    super(name);
  }

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
