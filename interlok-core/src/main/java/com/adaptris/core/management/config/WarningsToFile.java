package com.adaptris.core.management.config;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import lombok.NoArgsConstructor;

/**
 * Reports on warnings from various ConfigurationChecks based on a system property.
 * <p>
 * Note that this will only emit output if the system property {@value #SYSPROP_FILENAME} is set as
 * a system property. It is designed to mimic behaviour of the {@code InterlokVerifyReport} process
 * made available as part of the parent gradle.
 * </p>
 */
@NoArgsConstructor
public class WarningsToFile implements ConfigurationReporter {

  public static final String SYSPROP_FILENAME = "interlok.verify.warning.filename";
  public static final String SYSPROP_CATEGORY = "interlok.verify.warning.category";
  public static final String SYSPROP_LEVEL = "interlok.verify.warning.level";

  public static final String CATEGORY_DEFAULT = "CODE_SMELL";
  public static final String LEVEL_DEFAULT = "INFO";


  private static final List<String> SUPPORTED_CHECKERS =
      Arrays.asList(DeprecatedConfigurationChecker.class.getCanonicalName(),
          SharedConnectionConfigurationChecker.class.getCanonicalName(),
          SharedServiceConfigurationChecker.class.getCanonicalName());

  @Override
  public boolean report(Collection<ConfigurationCheckReport> reports) {
    if (isEnabled()) {
      FileUtils.deleteQuietly(new File(System.getProperty(SYSPROP_FILENAME)));
      find(reports).forEach((r) -> emitReport(r));
    }
    return true;
  }

  private static void emitReport(ConfigurationCheckReport report) {
    String filename = System.getProperty(SYSPROP_FILENAME);
    String category = System.getProperty(SYSPROP_CATEGORY, CATEGORY_DEFAULT);
    String level = System.getProperty(SYSPROP_LEVEL, LEVEL_DEFAULT);
    try (PrintWriter pw = new PrintWriter(new FileWriter(filename, true))) {
      for (String s : report.getWarnings()) {
        pw.println(String.format("%1$s,%2$s,%3$s", category, level, s));
      }
    } catch (Exception e) {
      // eat the exception...
    }
  }

  private static List<ConfigurationCheckReport> find(Collection<ConfigurationCheckReport> reports) {
    return reports.stream()
        .filter((report) -> SUPPORTED_CHECKERS.contains(report.getCheckClassName()))
        .collect(Collectors.toList());
  }

  private static boolean isEnabled() {
    return StringUtils.isNotBlank(System.getProperty(SYSPROP_FILENAME));
  }
}
