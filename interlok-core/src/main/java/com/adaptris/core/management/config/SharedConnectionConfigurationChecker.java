package com.adaptris.core.management.config;

public class SharedConnectionConfigurationChecker extends SharedComponentConfigurationChecker {

  private static final String FRIENDLY_NAME = "Shared connection check";

  private static final String COMPONENT_TYPE = "connection";

  private static final String XPATH_AVAILABLE_CONNECTIONS = "//shared-components/connections/*/unique-id";

  private static final String XPATH_REFERENCED_CONNECTIONS = "//*[@class='shared-connection']/lookup-name";

  public SharedConnectionConfigurationChecker() {
    super(COMPONENT_TYPE, XPATH_AVAILABLE_CONNECTIONS, XPATH_REFERENCED_CONNECTIONS);
  }

  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

}
