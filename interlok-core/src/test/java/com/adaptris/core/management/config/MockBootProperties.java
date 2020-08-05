package com.adaptris.core.management.config;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.input.ReaderInputStream;
import com.adaptris.core.management.BootstrapProperties;

public class MockBootProperties extends BootstrapProperties {
  private static final long serialVersionUID = 2020080401L;

  private transient String xml;

  public MockBootProperties(String config) {
    super();
    xml = config;
  }

  @Override
  public InputStream getConfigurationStream() throws Exception {
    return new ReaderInputStream(new StringReader(xml), StandardCharsets.UTF_8);
  }

}
