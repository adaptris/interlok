package com.adaptris.core.management.config;

import java.util.Arrays;
import java.util.List;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;
import com.adaptris.util.XmlUtils;

public class SharedConnectionConfigurationChecker implements ConfigurationChecker {
  
  private static final String FRIENDLY_NAME = "Shared connection check.";
    
  private static final String XPATH_AVAILABLE_CONNECTIONS = "//shared-components/connections/*/unique-id";
  
  private static final String XPATH_REFERENCED_CONNECTIONS = "//*[@class='shared-connection']/lookup-name";

  @Override
  public ConfigurationCheckReport performConfigCheck(BootstrapProperties bootProperties, UnifiedBootstrap bootstrap) {
    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(this.getFriendlyName());
    
    try {
      XmlUtils xmlUtils = new XmlUtils();
      xmlUtils.setSource(bootProperties.getConfigurationStream());
      
      List<String> availableConnections = Arrays.asList(xmlUtils.getMultipleTextItems(XPATH_AVAILABLE_CONNECTIONS));
      List<String> referencedConnections = Arrays.asList(xmlUtils.getMultipleTextItems(XPATH_REFERENCED_CONNECTIONS));
      
      // **********************************
      // Check all shared connections are used.
      availableConnections.forEach(connection -> {
        if(!referencedConnections.contains(connection))
          report.getWarnings().add("Shared connection unused: " + connection);
      });
      
      // **********************************
      // Check all referenced connections exist.
      referencedConnections.forEach(connection -> {
        if(!availableConnections.contains(connection))
          report.getFailureExceptions().add(new ConfigurationException("Shared connection does not exist in shared components: " + connection));
      });
      
    } catch (Exception ex) {
      report.getFailureExceptions().add(ex);
    }
    
    return report;
  }
  
  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

}
