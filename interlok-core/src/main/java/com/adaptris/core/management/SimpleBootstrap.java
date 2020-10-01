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

/**
 * Entry point into an adapter from the commandline.
 * <p>
 * Basically the same as StandardBootstrap but without the classpath initialization.
 * </p>
 *
 */
public class SimpleBootstrap extends CmdLineBootstrap {

  public SimpleBootstrap(String[] argv) throws Exception {
    super(argv);
  }

  @Override
  public void boot() throws Exception {
    logVersionInformation();
    super.standardBoot();
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
    SimpleBootstrap simpleBoot = new SimpleBootstrap(argv);
    try {
      simpleBoot.boot();
    }
    catch (Exception e) {
      if (!simpleBoot.startQuietly()) {
        e.printStackTrace();
        System.exit(1);
      }
      LoggingConfigurator.newConfigurator().requestShutdown();
    }
  }
}
