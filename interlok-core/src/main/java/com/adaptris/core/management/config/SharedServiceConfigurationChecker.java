package com.adaptris.core.management.config;

public class SharedServiceConfigurationChecker extends SharedComponentConfigurationChecker {

  private static final String FRIENDLY_NAME = "Shared service check";

  private static final String COMPONENT_TYPE = "service";

  private static final String XPATH_AVAILABLE_CONNECTIONS = "//shared-components/services/*/unique-id";

  private static final String XPATH_REFERENCED_CONNECTIONS = "//shared-service/lookup-name";

  public SharedServiceConfigurationChecker() {
    super(COMPONENT_TYPE, XPATH_AVAILABLE_CONNECTIONS, XPATH_REFERENCED_CONNECTIONS);
  }

  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

}
