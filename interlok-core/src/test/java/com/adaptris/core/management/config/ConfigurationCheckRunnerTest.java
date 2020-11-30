package com.adaptris.core.management.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.Channel;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.NullConnection;
import com.adaptris.core.SharedConnection;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;
import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.interlok.util.Closer;

public class ConfigurationCheckRunnerTest {

  private static final String SHARED_CONN_TEST_FRIENDLY_NAME = "Shared connection check";

  private static ServiceLoader<ConfigurationChecker> configurationCheckers = ServiceLoader.load(ConfigurationChecker.class);

  private ConfigurationCheckRunner checkRunner;

  @Mock private AdapterManagerMBean mockAdapterMBean;

  @Mock private UnifiedBootstrap mockUnifiedBootstrap;

  private AutoCloseable openMocks;

  @Before
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);
    checkRunner = new ConfigurationCheckRunner();

    when(mockUnifiedBootstrap.createAdapter())
    .thenReturn(mockAdapterMBean);

    when(mockAdapterMBean.getConfiguration())
    .thenReturn(createAdapterConfig());

    System.setProperty("interlok.bootstrap.debug", "true");
  }

  @After
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testSuccessRunner() throws Exception {
    String xml = this.createAdapterConfig(
        new NullConnection("SharedNullConnection"), new SharedConnection("SharedNullConnection"),
        new SharedConnection("SharedNullConnection"));
    BootstrapProperties mockProp = new MockBootProperties(xml);
    List<ConfigurationCheckReport> reports =
        checkRunner.runChecks(mockProp, mockUnifiedBootstrap);
    assertEquals(configurationCheckersCount(), reports.size());
    for (ConfigurationCheckReport report : reports) {
      if (report.getCheckName().equals(SHARED_CONN_TEST_FRIENDLY_NAME)) {
        assertTrue(report.isCheckPassed());
        assertEquals(0, report.getFailureExceptions().size());
      }
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testFailureRunner() throws Exception {
    String xml =
        this.createAdapterConfig(new NullConnection("SharedNullConnection"), null, null);
    BootstrapProperties mockProp = new MockBootProperties(xml);
    List<ConfigurationCheckReport> reports = checkRunner.runChecks(mockProp, mockUnifiedBootstrap);
    assertEquals(configurationCheckersCount(), reports.size());

    for (ConfigurationCheckReport report : reports) {
      if(report.getCheckName().equals(SHARED_CONN_TEST_FRIENDLY_NAME)) {
        assertFalse(report.isCheckPassed());
        assertTrue(report.getWarnings().size() > 0);
        assertNotNull(report.toString());
      }
    }
  }

  private String createAdapterConfig(AdaptrisConnection sharedComponent,
      AdaptrisConnection consumeConnection, AdaptrisConnection produceConnection) throws Exception {
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

    adapter.getChannelList().add(channel);
    return DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
  }

  private String createAdapterConfig() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("MyAdapter");
    return DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
  }

  public long configurationCheckersCount() {
    return StreamSupport.stream(configurationCheckers.spliterator(), false).count();
  }
}
