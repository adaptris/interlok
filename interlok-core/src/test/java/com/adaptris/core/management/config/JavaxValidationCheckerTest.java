package com.adaptris.core.management.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.adaptris.core.Adapter;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.services.metadata.AddTimestampMetadataService;
import com.adaptris.core.services.metadata.PayloadHashingService;

public class JavaxValidationCheckerTest {

  @Test
  public void testPerformCheck_Valid() throws Exception {
    JavaxValidationChecker checker = new JavaxValidationChecker();

    BootstrapProperties mockBootProperties =
        new MockBootProperties(toString(createAdapterConfig(true)));
    ConfigurationCheckReport report = checker.performConfigCheck(mockBootProperties, null);
    assertTrue(report.isCheckPassed());
  }

  @Test
  public void testValidate_Valid() throws Exception {
    JavaxValidationChecker checker = new JavaxValidationChecker();
    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(checker.getFriendlyName());
    checker.validate(createAdapterConfig(true), report);
    assertTrue(report.isCheckPassed());
    assertEquals(0, report.getFailureExceptions().size());
    assertEquals(0, report.getWarnings().size());
  }

  @Test
  public void testValidate_Invalid() throws Exception {
    JavaxValidationChecker checker = new JavaxValidationChecker();
    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(checker.getFriendlyName());
    checker.validate(createAdapterConfig(false), report);

    assertFalse(report.isCheckPassed());
    // Should be 4 exceptions, 1x adapter-unique-id, 3 from the shared-services
    assertEquals(0, report.getWarnings().size());
    assertEquals(4, report.getFailureExceptions().size());
  }

  @Test
  public void testValidate_Null() throws Exception {
    JavaxValidationChecker checker = new JavaxValidationChecker();

    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(checker.getFriendlyName());
    checker.validate(null, report);
    assertFalse(report.isCheckPassed());
    assertEquals(1, report.getFailureExceptions().size());
  }

  private String toString(Adapter adapter) throws Exception {
    return DefaultMarshaller.getDefaultMarshaller().marshal(adapter);
  }

  private Adapter createAdapterConfig(boolean validates) throws Exception {

    Adapter adapter = new Adapter();
    if (validates) {
      // have a unique-id
      adapter.setUniqueId("MyAdapter");
      AddTimestampMetadataService ts = new AddTimestampMetadataService();
      ts.setUniqueId("valid-add-timestamp-service");
      PayloadHashingService ph = new PayloadHashingService();
      ph.setUniqueId("valid-payload-hasher");
      ph.setMetadataKey("payloadHash");
      ph.setHashAlgorithm("SHA-256");

      adapter.getSharedComponents().addService(ts);
      adapter.getSharedComponents().addService(ph);
    } else {
      // no adapter unique-id
      // explicitly set add timestamp to have an empty key, thankfully
      // no checks on the setter...
      AddTimestampMetadataService ts = new AddTimestampMetadataService();
      ts.setUniqueId("invalid-add-timestamp-service");
      ts.setMetadataKey("");

      // don't specify the hash algo or the metadata-key
      PayloadHashingService ph = new PayloadHashingService();
      ph.setUniqueId("invalid-payload-hasher");

      adapter.getSharedComponents().addService(ts);
      adapter.getSharedComponents().addService(ph);
    }
    return adapter;
  }
}
