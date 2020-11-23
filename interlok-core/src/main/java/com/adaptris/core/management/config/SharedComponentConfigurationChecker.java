package com.adaptris.core.management.config;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.util.XmlUtils;

public abstract class SharedComponentConfigurationChecker implements ConfigurationChecker {

  private String componentType;
  private Collection<String> xpathAvailableComponents;
  private Collection<String> xpathReferencedComponents;

  protected SharedComponentConfigurationChecker(String type, String avail, String refs) {
    this(type, Arrays.asList(avail), Arrays.asList(refs));
  }

  protected SharedComponentConfigurationChecker(String type, Collection<String> avail,
      Collection<String> refs) {
    componentType = type;
    xpathAvailableComponents = avail;
    xpathReferencedComponents = refs;
  }

  @Override
  public ConfigurationCheckReport performConfigCheck(ConfigurationCheckReport report, BootstrapProperties bootProperties) {
    try {
      String xml = CachingConfigLoader.loadInterlokConfig(bootProperties);
      try (InputStream in = IOUtils.toInputStream(xml, Charset.defaultCharset())) {
        XmlUtils xmlUtils = new XmlUtils();
        xmlUtils.setSource(in);

        Set<String> availableComponents = evaluate(xmlUtils, xpathAvailableComponents);
        Set<String> referencedComponents = evaluate(xmlUtils, xpathReferencedComponents);

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
          if (!availableComponents.contains(component)) {
            report.getFailureExceptions().add(new ConfigurationException(
                "Shared " + componentType + " does not exist in shared components: " + component));
          }
        });
      }
    } catch (Exception ex) {
      report.getFailureExceptions().add(ex);
    }

    return report;
  }

  private static Set<String> evaluate(XmlUtils xmlUtils, Collection<String> xpaths)
      throws Exception {
    LinkedHashSet<String> result = new LinkedHashSet<>();
    for (String xpath : xpaths) {
      result.addAll(Arrays.asList(xmlUtils.getMultipleTextItems(xpath)));
    }
    return result;
  }
}
