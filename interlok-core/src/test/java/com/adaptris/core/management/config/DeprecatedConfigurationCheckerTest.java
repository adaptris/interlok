package com.adaptris.core.management.config;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.NullMessageConsumer;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.services.metadata.AddTimestampMetadataService;
import com.adaptris.validation.constraints.ConfigDeprecated;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.IterableUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeprecatedConfigurationCheckerTest {

  @Test
  public void testPerformCheck_Valid() throws Exception {
    DeprecatedConfigurationChecker checker = new DeprecatedConfigurationChecker();

    BootstrapProperties mockBootProperties =
        new MockBootProperties(toString(createAdapterConfig(true)));
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties);
    assertTrue(report.isCheckPassed());
  }

  @Test
  public void testValidate_Valid() throws Exception {
    DeprecatedConfigurationChecker checker = new DeprecatedConfigurationChecker();

    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(checker.getFriendlyName());
    checker.validate(createAdapterConfig(true), report);
    assertTrue(report.isCheckPassed());
    assertEquals(0, report.getFailureExceptions().size());
    assertEquals(0, report.getWarnings().size());
  }

  @Test
  public void testValidate_Invalid() throws Exception {
    DeprecatedConfigurationChecker checker = new DeprecatedConfigurationChecker();

    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(checker.getFriendlyName());
    checker.validate(createAdapterConfig(false), report);

    assertFalse(report.isCheckPassed());
    // Should be 2 warning, 1 for the deprecated class and 1 for the deprecated member
    assertEquals(2, report.getWarnings().size());
    assertTrue(violationsAsExpected(report.getWarnings(), "sharedComponents.services[1]",
        "sharedComponents.services[2]"));
    assertEquals(0, report.getFailureExceptions().size());
  }

  @Test
  public void testValidate_Invalid_WithChannels() throws Exception {
    DeprecatedConfigurationChecker checker = new DeprecatedConfigurationChecker();

    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(checker.getFriendlyName());
    checker.validate(createAdapterConfig(false, true), report);

    assertFalse(report.isCheckPassed());
    // Should be 4 warnings,
    // deprecated class, deprecated member, consumer destination, deprecated service inside a
    // service list.
    assertEquals(4, report.getWarnings().size());
    assertTrue(violationsAsExpected(report.getWarnings(), "sharedComponents.services[1]",
        "sharedComponents.services[2]",
        "channelList.channels[0].workflowList.workflows[0].serviceCollection.services[0]",
        "channelList.channels[0].workflowList.workflows[0].consumer.destination"));
    assertEquals(0, report.getFailureExceptions().size());
  }

  @Test
  public void testValidate_Null() throws Exception {
    DeprecatedConfigurationChecker checker = new DeprecatedConfigurationChecker();

    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(checker.getFriendlyName());
    checker.validate(null, report);
    assertFalse(report.isCheckPassed());
    assertEquals(1, report.getFailureExceptions().size());
  }

  private String toString(Adapter adapter) throws Exception {
    return DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
  }

  private boolean violationsAsExpected(List<String> violations, String... txt) {
    List<String> expected = Arrays.asList(txt);
    for (String v : violations) {
      if (!IterableUtils.matchesAny(expected, (match) -> v.contains(match))) {
        return false;
      }
    }
    return true;
  }


  private Adapter createAdapterConfig(boolean validates, boolean channels) throws Exception {

    Adapter adapter = new Adapter();

    // have a unique-id
    adapter.setUniqueId("MyAdapter");
    AddTimestampMetadataService atms = new AddTimestampMetadataService();
    atms.setUniqueId("valid-add-timestamp-service");
    adapter.getSharedComponents().addService(atms);

    if (!validates) {
      // Add a deprecated service
      DeprecatedService ds = new DeprecatedService();
      adapter.getSharedComponents().addService(ds);

      // Add a service with deprecated member
      DeprecatedMemberService dms = new DeprecatedMemberService();
      dms.setDeprecated("value");
      adapter.getSharedComponents().addService(dms);
    }
    if (channels) {
      Channel c = new Channel();
      StandardWorkflow w = new StandardWorkflow();
      if (!validates) {
        NullMessageConsumer consumer = new NullMessageConsumer();
        w.setConsumer(consumer);
        w.getServiceCollection().add(new DeprecatedService());
      }
      c.getWorkflowList().add(w);
      adapter.getChannelList().add(c);
    }
    return adapter;
  }

  private Adapter createAdapterConfig(boolean validates) throws Exception {
    return createAdapterConfig(validates, false);
  }

  @ConfigDeprecated(message = "It will be removed in a future version. No replacement.", groups = Deprecated.class)
  public static class DeprecatedService extends ServiceImp {

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
    }

    @Override
    public void prepare() throws CoreException {
    }

    @Override
    protected void initService() throws CoreException {
    }

    @Override
    protected void closeService() {
    }

  }

  public static class DeprecatedMemberService extends ServiceImp {

    @Getter
    @Setter
    @ConfigDeprecated(message = "It will be removed in a future version. No replacement.", groups = Deprecated.class)
    private String deprecated;

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
    }

    @Override
    public void prepare() throws CoreException {
    }

    @Override
    protected void initService() throws CoreException {
    }

    @Override
    protected void closeService() {
    }

  }
}
