package com.adaptris.core.management.config;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import lombok.NoArgsConstructor;

/**
 * Reports on deprecated configuration based on a system property.
 * <p>
 * Note that this will only emit output if the system property {@value #SYSPROP_FILENAME} is set as
 * a system property. It is designed to mimic behaviour of the {@code InterlokVerifyReport} process
 * made available as part of the parent gradle.
 * </p>
 */
@NoArgsConstructor
public class DeprecatedConfigFileReporter implements ConfigurationReporter {

  public static final String SYSPROP_FILENAME = "interlok.verify.deprecated.filename";
  public static final String SYSPROP_CATEGORY = "interlok.verify.deprecated.category";
  public static final String SYSPROP_LEVEL = "interlok.verify.deprecated.level";

  public static final String CATEGORY_DEFAULT = "CODE_SMELL";
  public static final String LEVEL_DEFAULT = "INFO";
  private static final String CHECKER_CLASS =
      DeprecatedConfigurationChecker.class.getCanonicalName();

  @Override
  public boolean report(Collection<ConfigurationCheckReport> reports) {
    if (isEnabled()) {
      emitReport(find(reports));
    }
    return true;
  }

  private static void emitReport(ConfigurationCheckReport report) {
    String filename = System.getProperty(SYSPROP_FILENAME);
    String category = System.getProperty(SYSPROP_CATEGORY, CATEGORY_DEFAULT);
    String level = System.getProperty(SYSPROP_LEVEL, LEVEL_DEFAULT);
    try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
      for (String s : report.getWarnings()) {
        pw.println(String.format("%1$s,%2$s,%3$s", category, level, s));
      }
    } catch (Exception e) {
      // eat the exception...
    }
  }

  private static ConfigurationCheckReport find(Collection<ConfigurationCheckReport> reports) {
    return reports.stream().filter((report) -> CHECKER_CLASS.equals(report.getCheckClassName()))
        .findFirst().orElse(new ConfigurationCheckReport());
  }

  private static boolean isEnabled() {
    return StringUtils.isNotBlank(System.getProperty(SYSPROP_FILENAME));
  }
}
