package com.adaptris.core.management.config;

import java.util.Map;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.UnifiedBootstrap;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;

public class ClasspathDupConfigurationChecker implements ConfigurationChecker {
  
  private static final String FRIENDLY_NAME = "Classpath duplication check.";
  
  private static final String DESCRIPTION = "This test will scan your libraries checking and reporting on any duplicates found.";

  @SuppressWarnings("resource")
  @Override
  public ConfigurationCheckReport performConfigCheck(BootstrapProperties bootProperties, UnifiedBootstrap bootstrapProperties) {
    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(this.getFriendlyName());
    report.setCheckPassed(true);
    
    boolean passed = true;
    try (ScanResult result = new ClassGraph().scan()) {
      for (Map.Entry<String, ResourceList> dup : result.getAllResources().classFilesOnly().findDuplicatePaths()) {
        if (dup.getKey().equalsIgnoreCase("module-info.class")) {
          continue;
        }
        passed = false;
        System.err.println(String.format("%s has possible duplicates", dup.getKey()));
        for (Resource res : dup.getValue()) {
          System.err.println(String.format(" -> Found in %s", res.getURI()));
        }
      }
    }
    if(!passed) {
      report.setCheckPassed(false);
      report.setFailureException(new ConfigurationException("Possible duplicates found."));
    }
    
    return report;
  }
  
  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

}
