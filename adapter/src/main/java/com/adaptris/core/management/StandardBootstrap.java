/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.management;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import com.adaptris.core.management.logging.LoggingConfigurator;

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
    LoggingConfigurator.newConfigurator().defaultInitialisation();
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
    try {
      new StandardBootstrap(argv).boot();
    }
    catch (Exception e) {
      LoggingConfigurator.newConfigurator().requestShutdown();
      throw e;
    }

  }

}
