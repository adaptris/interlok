package com.adaptris.core.management.config;

import java.util.Arrays;
import java.util.List;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.util.XmlUtils;

public abstract class SharedComponentConfigurationChecker implements ConfigurationChecker {

  private String componentType;
  private String xpathAvailableComponents;
  private String xpathReferencedComponents;

  public SharedComponentConfigurationChecker(String componentType, String xpathAvailableComponents, String xpathReferencedComponents) {
    this.componentType = componentType;
    this.xpathAvailableComponents = xpathAvailableComponents;
    this.xpathReferencedComponents = xpathReferencedComponents;
  }

  @Override
  public ConfigurationCheckReport performConfigCheck(ConfigurationCheckReport report, BootstrapProperties bootProperties) {
    try {
      XmlUtils xmlUtils = new XmlUtils();
      xmlUtils.setSource(bootProperties.getConfigurationStream());

      List<String> availableConnections = Arrays.asList(xmlUtils.getMultipleTextItems(xpathAvailableComponents));
      List<String> referencedConnections = Arrays.asList(xmlUtils.getMultipleTextItems(xpathReferencedComponents));

      // **********************************
      // Check all shared connections are used.
      availableConnections.forEach(connection -> {
        if(!referencedConnections.contains(connection)) {
          report.getWarnings().add("Shared " + componentType + " unused: " + connection);
        }
      });

      // **********************************
      // Check all referenced connections exist.
      referencedConnections.forEach(connection -> {
        if(!availableConnections.contains(connection)) {
          report.getFailureExceptions()
              .add(new ConfigurationException("Shared " + componentType + " does not exist in shared components: " + connection));
        }
      });

    } catch (Exception ex) {
      report.getFailureExceptions().add(ex);
    }

    return report;
  }

}
