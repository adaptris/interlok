package com.adaptris.core.management.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


// Yes this is duplicate code, but ht other one is in stubs which we can't depend on
// because otherwise we have circular dependency hell.
class PortManager {
  private static final int PORT_RANGE = 3000;
  private static final int MAX_ATTEMPTS = 100;
  private static final Set<Integer> usedPorts = Collections.synchronizedSet(new HashSet<Integer>());

  public static Integer nextUnusedPort(int offset) {
    return nextUnusedPort(offset, MAX_ATTEMPTS);
  }


  public static Integer nextUnusedPort(int offset, int max) {
    int port = ThreadLocalRandom.current().nextInt(PORT_RANGE) + offset;
    int attemptCount = 0;
    while (!isPortAvailable(port) && attemptCount < max) {
      attemptCount++;
      port = ThreadLocalRandom.current().nextInt(PORT_RANGE) + offset;
    }
    if (attemptCount > max) {
      throw new RuntimeException("Max attempts to find a port reached");
    }
    return port;
  }

  public static void release(Integer port) {
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
