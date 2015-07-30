package com.adaptris.core.services.exception;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.ServiceList;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.util.license.License;

public class ExceptionHandlingServiceWrapperTest extends ExceptionServiceExample {

  public ExceptionHandlingServiceWrapperTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testWithExceptionFromWrappedServices() throws Exception {
    ExceptionHandlingServiceWrapper service = create();
    service.setService(new ServiceList(new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement("servicesComplete", "true")
        }))
    }));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertEquals("true", msg.getMetadataValue("exceptionServiceTriggered"));
    assertNull(msg.getMetadataValue("servicesComplete"));
  }

  public void testNoExceptionFromWrappedServices() throws Exception {
    AddMetadataService s1 = new AddMetadataService();
    s1.addMetadataElement("servicesComplete", "true");
    ExceptionHandlingServiceWrapper service = create();
    service.setService(s1);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);

    assertEquals("true", msg.getMetadataValue("servicesComplete"));
    assertNull(msg.getMetadataValue("exceptionServiceTriggered"));
  }

  public void testBug2431() throws Exception {
    ExceptionHandlingServiceWrapper service = create();
    Bug2431 s = new Bug2431();
    Bug2431 s2 = new Bug2431();
    service.setService(s);
    service.setExceptionHandlingService(s2);
    LicenseStub license = new LicenseStub();
    assertTrue(service.isEnabled(license));
    assertEquals(license, s.registeredLicense);
    assertEquals(license, s2.registeredLicense);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    ExceptionHandlingServiceWrapper service = create();
    service.setService(new ServiceList(new Service[]
    {
        new ThrowExceptionService(new ConfiguredException("Fail")), new AddMetadataService(Arrays.asList(new MetadataElement[]
        {
          new MetadataElement("servicesComplete", "true")
        }))
    }));
    return service;
  }

  private ExceptionHandlingServiceWrapper create() {
    ExceptionHandlingServiceWrapper service = new ExceptionHandlingServiceWrapper();
    AddMetadataService fail = new AddMetadataService();
    fail.addMetadataElement("exceptionServiceTriggered", "true");
    service.setExceptionHandlingService(fail);
    return service;
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + "<!--\n"
        + "\nThis example explicitly is configured to fail. Even so, no exception will be thrown\n"
        + "However the metadata key 'exceptionServiceTriggered' will be set to 'true'\n" + "'servicesComplete' is never set"
        + "\n-->\n";
  }

  private class Bug2431 extends ServiceImp {

    private License registeredLicense;
    @Override
    public void doService(AdaptrisMessage msg) {
    }

    @Override
    public void init() throws CoreException {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isEnabled(License l) throws CoreException {
      registeredLicense = l;
      return true;
    }
  }
}
