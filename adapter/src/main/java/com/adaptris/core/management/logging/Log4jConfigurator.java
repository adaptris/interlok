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

package com.adaptris.core.management.logging;

import static com.adaptris.core.management.Constants.DBG;

import java.io.File;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

/**
 * Configure dynamic reload of log4j2 configuration.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
final class Log4jConfigurator extends LoggingConfigurator {

  Log4jConfigurator() {
  }


  @Override
  public void defaultInitialisation() {
    bridgeJavaUtilLogging();
  }

  public boolean initialiseFrom(URL url) {
    boolean result = false;
    try {
      if (url != null) {
        File file = createFileReference(url, null);
        if (DBG) {
          System.err.println("(Info) Log4j2Init.configure() : Configuring Log4j2 with " + file.getCanonicalPath());
        }
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.setConfigLocation(file.toURI());
        if (DBG) {
          System.err.println("(Info) Log4j2Init.configure() : Reconfigured...");
        }
      }
      else {
        if (DBG) {
          System.err.println("(Info) Log4j2Init.configure() : No config found");
        }
      }
    }
    catch (Exception ignored) {
    }
    bridgeJavaUtilLogging();
    return result;
  }

}
