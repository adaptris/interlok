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

package com.adaptris.core.stubs;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.util.URLString;

// Helper for getting external resources; and making sure we have an inter-tubes connection.
//
public class ExternalResourcesHelper {

  public static final String DEFAULT_EXTERNAL_RESOURCE = "HEADER.html";
  public static final String EXTERNAL_SERVER = "development.adaptris.net";
  public static final String EXTERNAL_URL_PREFIX = "http://" + EXTERNAL_SERVER + "/installers/adapter/latest-stable/";

  private static Logger log = LoggerFactory.getLogger(ExternalResourcesHelper.class);

  public static String createUrl() {
    String url = EXTERNAL_URL_PREFIX + DEFAULT_EXTERNAL_RESOURCE;
    return url;
  }

  public static boolean isExternalServerAvailable() {
    return isExternalServerAvailable(EXTERNAL_SERVER, 80);
  }

  public static boolean isExternalServerAvailable(String server, int port) {
    boolean result = false;
    try (Socket s = new Socket()) {
      // Try and get a socket to dev on port 80
      // don't give it more than a second though...
      InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(server), port);
      s.connect(addr, 1000);
      result = true;
    }
    catch (Exception e) {
    }
    return result;
  }

  public static boolean isExternalServerAvailable(URLString server) {
    log.debug("Checking " + server.toString());
    return isExternalServerAvailable(server.getHost(), server.getPort());
  }

}
