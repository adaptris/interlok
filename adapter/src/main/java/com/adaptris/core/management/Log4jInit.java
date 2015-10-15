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

import static com.adaptris.core.management.Constants.DBG;
import static com.adaptris.core.management.Constants.ENABLE_JUL_LOGGING_BRIDGE;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.LoggingHelper;

/**
 * Configure dynamic reload of log4j configuration.
 * <p>
 * If you rely on log4j default initialisation, then it does not <code>
 * configureAndWatch</code> the configuration file (log4j.properties or log4j.xml). This is simply a mechanism for ensuring that the
 * first log4j configuration file on the classpath is monitored. log4j.xml is always preferred but we will fall back to
 * log4j.properties
 * </p>
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public final class Log4jInit {

  private Log4jInit() {
  }

  public static void configure() {
    configure(LoggingHelper.findLog4jConfiguration());
  }

  public static boolean configure(URL url) {
    boolean result = false;
    try {
      if (url != null) {
        String resource = resolve(url);
        if (DBG) {
          System.err.println("(Info) Log4jInit.configure() : Configuring Log4j with " + resource);
        }
        if (resource.endsWith(".xml")) {
          DOMConfigurator.configureAndWatch(resource);
          result = true;
        }
        else {
          PropertyConfigurator.configureAndWatch(resource);
          result = true;
        }
      }
      else {
        if (DBG) {
          System.err.println("(Info) Log4jInit.configure() : Unable to configure log4j, no config found on classpath");
        }
      }
    }
    catch (Exception ignored) {
      ;
    }
    if (ENABLE_JUL_LOGGING_BRIDGE) {
      if (DBG) {
        System.err.println("(Info) Log4jInit.configure() : java.util.logging now logs to log4j");
      }
      SLF4JBridgeHandler.install();
    }
    return result;
  }

  private static String resolve(URL url) throws IOException {
    String result = null;
    result = tryResolveURI(url.toExternalForm());
    if (result == null) {
      result = tryResolveFsHelper(url);
    }
    if (result == null) {
      result = url.getPath();
    }
    if (DBG) {
      System.err.println("(Info) Log4jInit.configure() : Resolved log4j configuration " + result);
    }
    return result;
  }

  private static String tryResolveURI(String url) {
    String result = null;
    try {
      result = new File(new URI(url)).getCanonicalPath();
    }
    catch (Exception e) {

    }
    return result;
  }

  private static String tryResolveFsHelper(URL url) {
    String result = null;
    try {
      result = FsHelper.createFileReference(url).getCanonicalPath();
    }
    catch (Exception e) {

    }
    return result;
  }
}
