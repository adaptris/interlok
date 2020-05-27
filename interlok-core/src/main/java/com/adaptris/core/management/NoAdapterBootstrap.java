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

import com.adaptris.core.management.logging.LoggingConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point into an adapter from the command line.
 *
 * Basically the same as {@link SimpleBootstrap} but without the Adapter being started.
 *
 * @author aanderson
 */
public class NoAdapterBootstrap extends CmdLineBootstrap {

  private transient Logger log = LoggerFactory.getLogger(NoAdapterBootstrap.class.getName());

  public NoAdapterBootstrap(String[] argv) throws Exception {
    super(argv);
  }

  @Override
  public void boot() throws Exception {
    log.info("Starting Jetty/UI without a local adapter");

    logVersionInformation();
    // standard boot
    LoggingConfigurator.newConfigurator().defaultInitialisation();
    BootstrapProperties bootProperties = new BootstrapProperties(getBootstrapResource());
    SystemPropertiesUtil.addSystemProperties(bootProperties);
    SystemPropertiesUtil.addJndiProperties(bootProperties);
    ProxyAuthenticator.register(bootProperties);

    // don't start adapter
    ManagementComponentFactory.create(bootProperties);
    ManagementComponentFactory.initCreated(bootProperties);
    Runtime.getRuntime().addShutdownHook(new ShutdownHandler(bootProperties));

    // don't launch adapter
    ManagementComponentFactory.startCreated(bootProperties);
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
      new NoAdapterBootstrap(argv).boot();
    }
    catch (Exception e) {
      LoggingConfigurator.newConfigurator().requestShutdown();
      throw e;
    }
  }
}
