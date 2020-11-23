package com.adaptris.core.management.config;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.input.ReaderInputStream;
import com.adaptris.core.management.BootstrapProperties;

public class MockBootProperties extends BootstrapProperties {
  private static final long serialVersionUID = 2020080401L;

  private transient String xml;
  private transient List<String> adapterResourceNames = Collections.EMPTY_LIST;

  public MockBootProperties(String config) {
    super();
    xml = config;
  }

  public MockBootProperties(String config, List<String> adapterResources) {
    this(config);
    adapterResourceNames = Optional.ofNullable(adapterResources).orElse(Collections.EMPTY_LIST);
  }

  @Override
  public InputStream getConfigurationStream() throws Exception {
    return new ReaderInputStream(new StringReader(xml), StandardCharsets.UTF_8);
  }

  @Override
  public String findAdapterResource() {
    return adapterResourceNames.stream().findFirst().orElseGet(() -> super.findAdapterResource());
  }
}
