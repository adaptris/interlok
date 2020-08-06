package com.adaptris.core.management.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.Channel;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.SharedConnection;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.management.BootstrapProperties;

public class SharedConnectionConfigurationCheckerTest {

  private SharedConnectionConfigurationChecker checker;

  @Before
  public void setUp() throws Exception {
    checker = new SharedConnectionConfigurationChecker();
  }

  @Test
  public void testCompleteFailureBadXml() throws Exception {
    BootstrapProperties mockBootProperties = new MockBootProperties("bad-data");
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
    assertTrue(report.getFailureExceptions().size() > 0);
  }

  @Test
  public void testNoConnections() throws Exception {
    BootstrapProperties mockBootProperties =
        new MockBootProperties(createAdapterConfig(null, null, null, null));
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertTrue(report.isCheckPassed());
  }

  @Test
  public void testSharedNotUsed() throws Exception {
    BootstrapProperties mockBootProperties =
        new MockBootProperties(
            createAdapterConfig(new NullConnection("SharedNullConnection"), null, null, null));
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
  }

  @Test
  public void testConsumeConnectionNoShared() throws Exception {
    BootstrapProperties mockBootProperties = new MockBootProperties(
        createAdapterConfig(null, new SharedConnection("DoesNotExist"), null, null));

    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
    assertTrue(report.getFailureExceptions().size() > 0);
  }

  @Test
  public void testProduceConnectionNoShared() throws Exception {
    BootstrapProperties mockBootProperties = new MockBootProperties(
        createAdapterConfig(null, null, new SharedConnection("DoesNotExist"), null));
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
    assertTrue(report.getFailureExceptions().size() > 0);
  }

  @Test
  public void testProduceAndConsumeConnectionsExist() throws Exception {
    BootstrapProperties mockBootProperties =
        new MockBootProperties(createAdapterConfig(new NullConnection("SharedNullConnection"),
            new SharedConnection("SharedNullConnection"),
            new SharedConnection("SharedNullConnection"), null));

    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertTrue(report.isCheckPassed());
    assertNotNull(report.toString());
  }

  @Test
  public void testServiceConnectionDoesNotExist() throws Exception {
    BootstrapProperties mockBootProperties =
        new MockBootProperties(createAdapterConfig(new NullConnection("SharedNullConnection"), null,
            null, new SharedConnection("DoesNotExist")));
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertFalse(report.isCheckPassed());
    assertTrue(report.getFailureExceptions().size() > 0);
    assertNotNull(report.toString());
  }

  private static String createAdapterConfig(AdaptrisConnection sharedComponent,
      AdaptrisConnection consumeConnection, AdaptrisConnection produceConnection,
      AdaptrisConnection serviceConnection) throws Exception {

    Adapter adapter = new Adapter();
    if(sharedComponent != null) {
      adapter.getSharedComponents().addConnection(sharedComponent);
    }

    Channel channel = new Channel();
    if(consumeConnection != null) {
      channel.setConsumeConnection(consumeConnection);
    }
    if(produceConnection != null) {
      channel.setProduceConnection(produceConnection);
    }
    if(serviceConnection != null) {
      StandardWorkflow standardWorkflow = new StandardWorkflow();
      StandaloneProducer sp = new StandaloneProducer(serviceConnection, new NullMessageProducer());
      standardWorkflow.getServiceCollection().add(sp);
      channel.getWorkflowList().add(standardWorkflow);

    }
    adapter.getChannelList().add(channel);
    return DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
  }

}
