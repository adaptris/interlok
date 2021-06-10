package com.adaptris.core.management.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.adaptris.core.Adapter;
import com.adaptris.core.management.config.ConfigurationCheckReport;
import com.adaptris.core.management.config.NashornChecker;
import com.adaptris.core.services.EmbeddedScriptingService;
import com.adaptris.util.GuidGenerator;

public class NashornCheckerTest {

  private static final GuidGenerator GUID = new GuidGenerator();

  @Test
  public void testNashorn() throws Exception {
    NashornChecker checker = new NashornChecker();

    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(checker.getFriendlyName());
    checker.validate(createAdapterConfig(true), report);
    assertFalse(report.isCheckPassed());
    assertEquals(0, report.getFailureExceptions().size());
    assertEquals(1, report.getWarnings().size());
    assertTrue(report.getWarnings().get(0).contains("explicit_nashorn"));
  }

  @Test
  public void testNoNashorn() throws Exception {
    NashornChecker checker = new NashornChecker();

    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(checker.getFriendlyName());
    checker.validate(createAdapterConfig(false), report);
    assertTrue(report.isCheckPassed());
    assertEquals(0, report.getFailureExceptions().size());
    assertEquals(0, report.getWarnings().size());
  }

  private Adapter createAdapterConfig(boolean hasNashorn) {
    Adapter result = new Adapter();
    result.setUniqueId(GUID.safeUUID());
    result.getSharedComponents()
        .addService(new EmbeddedScriptingService(GUID.safeUUID()).withScript("jruby", ""));
    result.getSharedComponents()
        .addService(new EmbeddedScriptingService(GUID.safeUUID()).withScript("jruby", ""));
    result.getSharedComponents()
        .addService(new EmbeddedScriptingService(GUID.safeUUID()).withScript("jruby", ""));
    if (hasNashorn) {
      result.getSharedComponents()
          .addService(new EmbeddedScriptingService("explicit_nashorn").withScript("nashorn", ""));
    }
    return result;
  }

}
