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

import static com.adaptris.core.fs.FsHelper.createFileReference;
import static com.adaptris.core.fs.FsHelper.createUrlFromString;
import static com.adaptris.core.management.Constants.DBG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;

import com.adaptris.core.util.Args;
import com.adaptris.util.URLString;

/**
 * Configure dynamic reload of log4j2 configuration.
 * 
 */
final class Log4jConfigurator extends LoggingConfigurator {

  // as declared the JLS says... can we trust it?
  private static enum BuildURI {
    FromFile {
      @Override
      URI build(String path) throws IOException, URISyntaxException {
        File f = createFileReference(new URLString(path).getURL());
        if (f.exists()) {
          System.err.println(f.toURI());
          return f.toURI();
        }
        throw new FileNotFoundException();
      }

    },
    FromURI {
      @Override
      URI build(String path) throws IOException, URISyntaxException {
        return new URI(path);
      }
    },
    ViaURL {
      @Override
      URI build(String path) throws IOException, URISyntaxException {
        if (probablyLocalFile(path)) {
          return FromFile.build(path);
        }
        URL url = createUrlFromString(path, true);
        return new URI(url.getProtocol(), url.getHost(), url.getPath(), null);
      }
      
    };
    
    abstract URI build(String path) throws IOException, URISyntaxException;
    
  }

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
        URI uri = Args.notNull(asURI(url), "loggingConfigUrl");
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

  private static URI asURI(final String path) {
    for (BuildURI c : BuildURI.values()) {
      try {
        if (DBG) System.err.println("(Info) Parse using " + c.name());
        return c.build(path);
      }
      catch (IOException | URISyntaxException e) {

      }
    }
    return null;
  }

  private static boolean probablyLocalFile(String loc) throws IOException {
    URLString url = new URLString(loc);
    return url.getProtocol() == null || "file".equals(url.getProtocol());
  }
}
