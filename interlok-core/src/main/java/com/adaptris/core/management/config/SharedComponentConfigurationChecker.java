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

      List<String> availableComponents = Arrays.asList(xmlUtils.getMultipleTextItems(xpathAvailableComponents));
      List<String> referencedComponents = Arrays.asList(xmlUtils.getMultipleTextItems(xpathReferencedComponents));

      // **********************************
      // Check all shared components are used.
      availableComponents.forEach(component -> {
        if (!referencedComponents.contains(component)) {
          report.getWarnings().add("Shared " + componentType + " unused: " + component);
        }
      });

      // **********************************
      // Check all referenced components exist.
      referencedComponents.forEach(component -> {
        if(!availableComponents.contains(component)) {
          report.getFailureExceptions()
          .add(new ConfigurationException("Shared " + componentType + " does not exist in shared components: " + component));
        }
      });

    } catch (Exception ex) {
      report.getFailureExceptions().add(ex);
    }

    return report;
  }

}
