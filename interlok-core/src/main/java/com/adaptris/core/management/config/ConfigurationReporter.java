package com.adaptris.core.management.config;

import java.util.Collection;
import java.util.ServiceLoader;

/**
 * {@link ServiceLoader} interface that allows for config checks to be reported on.
 *
 *
 */
public interface ConfigurationReporter {

  /**
   * Report on the checks that were performed; indicating overall success or failure.
   * 
   * @param reports the list of reports
   * @return a whether the reporting was considered successful. If false then this will lead to a
   *         non-zero exit code.
   */
  boolean report(Collection<ConfigurationCheckReport> reports);

}
