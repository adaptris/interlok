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

package com.adaptris.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Helper class to manage ports.
 *
 * @author lchan
 *
 */
public class PortManager {
  private static final int PORT_RANGE = 3000;

  private static final Set<Integer> usedPorts = Collections.synchronizedSet(new HashSet<Integer>());

  public synchronized static Integer nextUnusedPort(int offset) {
    int port = ThreadLocalRandom.current().nextInt(PORT_RANGE) + offset;
    while (!isPortAvailable(port)) {
      port = ThreadLocalRandom.current().nextInt(PORT_RANGE) + offset;
    }
    return port;
  }

  public synchronized static void release(Integer port) {
    usedPorts.remove(port);
  }

  private static boolean isPortAvailable(Integer port) {
    boolean result = false;
    if (usedPorts.contains(port)) {
      return result;
    }
    try (ServerSocket srv = new ServerSocket(port)) {
      srv.setReuseAddress(true);
      result = true;
    }
    catch (IOException e) {
      result = false;
    }
    usedPorts.add(port);
    return result;
  }
}
