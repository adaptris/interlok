package com.adaptris.core.management.config;

import java.util.Arrays;

public class SharedServiceConfigurationChecker extends SharedComponentConfigurationChecker {

  private static final String FRIENDLY_NAME = "Shared service check";

  private static final String COMPONENT_TYPE = "service";

  private static final String AVAILABLE_SHARED_SERVICES = "//shared-components/services/*/unique-id";

  // service-list/shared-service
  private static final String REFERENCE_ELEMENT_XPATH = "//shared-service/lookup-name";

  // things where a shared-service might be used where <service class="shared-service">
  private static final String REFERENCE_CLASSNAME_XPATH = "//*[@class='shared-service']/lookup-name";

  public SharedServiceConfigurationChecker() {
    super(COMPONENT_TYPE, Arrays.asList(AVAILABLE_SHARED_SERVICES),
        Arrays.asList(REFERENCE_ELEMENT_XPATH, REFERENCE_CLASSNAME_XPATH));
  }

  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

}
