package com.adaptris.core.management;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import com.adaptris.core.util.LoggingHelper;

/**
 * Entry point into an adapter from the commandline.
 * <p>
 * Ensures that certain elements are included in the classpath prior booting the adapter.
 * </p>
 *
 * @author gcsiki
 */
public class StandardBootstrap extends CmdLineBootstrap {

  private static final String[] ARG_ADAPTER_CLASSPATH = new String[]
  {
      "-adapterClasspath", "--adapterClasspath"
  };

  private static final String[] ARG_IGNORE_SUBDIRS = new String[]
  {
      "-ignoreSubDirs", "--ignoreSubDirs"
  };

  public StandardBootstrap(String[] argv) throws Exception {
    super(argv);
  }

  @Override
  public void boot() throws Exception {
    initClasspath();
    logVersionInformation();
    standardBoot();
  }

  private void initClasspath() throws Exception {
    boolean loadSubDirs = !getCommandlineArguments().hasArgument(ARG_IGNORE_SUBDIRS);
    Collection<String> adapterClasspath = null;
    if (getCommandlineArguments().hasArgument(ARG_ADAPTER_CLASSPATH)) {
      String paramValue = getCommandlineArguments().getArgument(ARG_ADAPTER_CLASSPATH);
      adapterClasspath = Arrays.asList(paramValue.split(File.pathSeparator));
    }
    ClasspathInitialiser.init(adapterClasspath, loadSubDirs);
  }

  protected void standardBoot() throws Exception {
    // Copied from BootstrapLoader and moved log4jAvailable() to LoggingHelper
    if (LoggingHelper.log4jAvailable()) {
      Log4jInit.configure();
    }
    BootstrapProperties bootProperties = new BootstrapProperties(getBootstrapResource());
    SystemPropertiesUtil.addSystemProperties(bootProperties);
    SystemPropertiesUtil.addJndiProperties(bootProperties);
    ProxyAuthenticator.register(bootProperties);
    super.startAdapter(bootProperties);
  }

  /**
   * <p>
   * Entry point to program.
   * </p>
   *
   * @param argv - Command line arguments
   *
   * @throws Exception upon some unrecoverable error.
   */
  public static void main(String[] argv) throws Exception {
    new StandardBootstrap(argv).boot();
  }

}
