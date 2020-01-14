package com.adaptris.interlok.client.jmx;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class PortManager {
  private static final int PORT_RANGE = 3000;

  private static final Set<Integer> usedPorts = Collections.synchronizedSet(new HashSet<Integer>());

  public static Integer nextUnusedPort(int offset) {
    int port = ThreadLocalRandom.current().nextInt(PORT_RANGE) + offset;
    while (!isPortAvailable(port)) {
      port = ThreadLocalRandom.current().nextInt(PORT_RANGE) + offset;
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
    try {
      ServerSocket srv = new ServerSocket(port);
      srv.close();
      srv = null;
      result = true;
    }
    catch (IOException e) {
      result = false;
    }
    usedPorts.add(port);
    return result;
  }
}
