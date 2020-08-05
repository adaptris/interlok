package com.adaptris.core.management.config;

import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.Constants;
import com.adaptris.core.management.UnifiedBootstrap;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;

public class ClasspathDupConfigurationChecker implements ConfigurationChecker {

  private static final String FRIENDLY_NAME = "Classpath duplication check.";

  private boolean debug;

  @SuppressWarnings("resource")
  @Override
  public ConfigurationCheckReport performConfigCheck(BootstrapProperties bootProperties, UnifiedBootstrap bootstrapProperties) {
    ConfigurationCheckReport report = new ConfigurationCheckReport();
    report.setCheckName(getFriendlyName());

    boolean passed = true;
    try (ScanResult result = new ClassGraph().scan()) {
      for (Map.Entry<String, ResourceList> dup : result.getAllResources().classFilesOnly().findDuplicatePaths()) {
        if (dup.getKey().equalsIgnoreCase("module-info.class")) {
          continue;
        }
        passed = false;
        if (debugMode()) {
          System.err.println(String.format("%s has possible duplicates", dup.getKey()));
          for (Resource res : dup.getValue()) {
            System.err.println(String.format(" -> Found in %s", res.getURI()));
          }
        }
      }
    }
    if(!passed) {
      if (debugMode())
        report.getWarnings().add("Possible duplicates found.  Details above.");
      else
        report.getWarnings().add("Possible duplicates found.  Re-run with '-Dinterlok.bootstrap.debug=true' for more details.");
    }

    return report;
  }

  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

  private boolean debugMode() {
    return BooleanUtils.or(new boolean[] {Constants.DBG, isDebug()});
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

}
