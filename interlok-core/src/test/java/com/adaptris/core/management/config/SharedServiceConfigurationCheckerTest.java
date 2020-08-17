package com.adaptris.core.management.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.Adapter;
import com.adaptris.core.Channel;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceList;
import com.adaptris.core.SharedService;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.management.BootstrapProperties;

public class SharedServiceConfigurationCheckerTest {

  private SharedServiceConfigurationChecker checker;

  @Before
  public void setUp() throws Exception {
    checker = new SharedServiceConfigurationChecker();
  }

  @Test
  public void testCompleteFailureBadXml() throws Exception {
    BootstrapProperties mockBootProperties = new MockBootProperties("bad-data");
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties);
    assertFalse(report.isCheckPassed());
    assertTrue(report.getFailureExceptions().size() > 0);
  }

  @Test
  public void testNoService() throws Exception {
    BootstrapProperties mockBootProperties =
        new MockBootProperties(createAdapterConfig(null, null));
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties);
    assertTrue(report.isCheckPassed());
  }

  @Test
  public void testSharedNotUsed() throws Exception {
    BootstrapProperties mockBootProperties =
        new MockBootProperties(
            createAdapterConfig(new NullService("SharedNullService"), null));
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties);
    assertFalse(report.isCheckPassed());
  }

  @Test
  public void testServiceNoShared() throws Exception {
    BootstrapProperties mockBootProperties = new MockBootProperties(
        createAdapterConfig(null, new SharedService("DoesNotExist")));

    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties);
    assertFalse(report.isCheckPassed());
    assertTrue(report.getFailureExceptions().size() > 0);
  }

  @Test
  public void testServiceExist() throws Exception {
    BootstrapProperties mockBootProperties =
        new MockBootProperties(createAdapterConfig(new NullService("SharedNullService"),
            new SharedService("SharedNullService")));

    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties);
    assertTrue(report.isCheckPassed());
    assertNotNull(report.toString());
  }

  @Test
  public void testServiceCollectionExist() throws Exception {
    ServiceList sharedComponent = new ServiceList();
    sharedComponent.setUniqueId("SharedNullService");
    BootstrapProperties mockBootProperties = new MockBootProperties(
        createAdapterConfig(sharedComponent, new SharedService("SharedNullService")));

    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties);
    assertTrue(report.isCheckPassed());
    assertNotNull(report.toString());
  }

  @Test
  public void testServiceInServiceCollectionExist() throws Exception {
    BootstrapProperties mockBootProperties = new MockBootProperties(
        createAdapterConfig(new NullService("SharedNullService"), new ServiceList(new SharedService("SharedNullService"))));

    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties);
    assertTrue(report.isCheckPassed());
    assertNotNull(report.toString());
  }

  @Test
  public void testServiceConnectionDoesNotExist() throws Exception {
    BootstrapProperties mockBootProperties =
        new MockBootProperties(createAdapterConfig(new NullService("SharedNullConnection"), new SharedService("DoesNotExist")));
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties);
    assertFalse(report.isCheckPassed());
    assertTrue(report.getFailureExceptions().size() > 0);
    assertNotNull(report.toString());
  }

  private static String createAdapterConfig(Service sharedComponent, Service service) throws Exception {
    Adapter adapter = new Adapter();
    if(sharedComponent != null) {
      adapter.getSharedComponents().addService(sharedComponent);
    }

    Channel channel = new Channel();
    StandardWorkflow standardWorkflow = new StandardWorkflow();
    channel.getWorkflowList().add(standardWorkflow);
    adapter.getChannelList().add(channel);

    if (service != null) {
      standardWorkflow.getServiceCollection().add(service);
    }
    return DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
  }

}
