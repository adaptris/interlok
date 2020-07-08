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

import java.net.URI;
import java.net.URL;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import com.adaptris.interlok.util.ResourceLocator;

/**
 * Configure dynamic reload of log4j2 configuration.
 * 
 */
final class Log4jConfigurator extends LoggingConfigurator {

  Log4jConfigurator() {
  }


  @Override
  public void defaultInitialisation() {
    bridgeJavaUtilLogging();
  }

  public boolean initialiseFrom(String url) {
    boolean result = false;
    try {
      if (url != null) {
        URL actualURL = ResourceLocator.toURL(url);
        URI uri = actualURL.toURI();
        if (DBG) {
          System.err.println("(Info) Log4j2Init.configure() : Configuring Log4j2 with " + uri);
        }
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.setConfigLocation(uri);
        if (DBG) {
          System.err.println("(Info) Log4j2Init.configure() : Reconfigured...");
        }
        result = true;
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

  @Override
  public void requestShutdown() {
    if (DBG) System.err.println("(Info) Log4j2Init.shutdown()");
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Map<String, Appender> appenders = ctx.getConfiguration().getAppenders();
    for (String s : appenders.keySet()) {
      Appender app = appenders.get(s);
      if (DBG) System.err.println("(Info) Stopping " + app.getClass());
      app.stop();
    }
    // INTERLOK-1455 appears that
    // ctx.terminate()
    // or LogManager.shutdown() don't quite work exactly as expected.
  }
}
