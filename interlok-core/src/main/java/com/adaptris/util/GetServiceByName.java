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

package com.adaptris.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GetServiceByName. The <code>GetServiceByName</code> class provides lookup of port numbers from the <code>/etc/services</code>
 * file.
 * <p>
 * For example, given a line in <tt>/etc/services</tt> {@code http  80/tcp} A search for service "http" and type "tcp" returns 80
 * 
 * @author $Author: lchan $
 */
public final class GetServiceByName {

  /** List of files that might contain our list of service entries...
   */
  private static final String[] SERVICES_FILES =
    {
      "/etc/inet/services",
      "c:\\winnt\\system32\\drivers\\etc\\services",
      "/etc/services",
      "c:\\windows\\system32\\drivers\\etc\\services",
      "c:\\windows\\services" };
      
  private static GetServiceByName service = null;
  private transient Logger logR = LoggerFactory.getLogger(this.getClass());
  private ByteArrayInputStream input = null;

  private GetServiceByName() throws IOException {
    initialise();
  }

  /** Initialise this class
   *  <p>Basically, attempt to find the services file based on what platform
   *  we're on.
   *  @see #setFile(String)
   */
  private void initialise() throws IOException {
    for (int i = 0; i < SERVICES_FILES.length; i++) {
      try {
        setFile(SERVICES_FILES[i]);
      }
      catch (FileNotFoundException ignoredByDesign) {
        // Just continue...
        ;
      }
    }

    if (input == null) {
      throw new FileNotFoundException("Couldn't find services file ");
    }

  }

  /** setFile.
   *  @param s the file containing the list of services.
   */
  private void setFile(String s) throws IOException {

    File f = new File(s);
    int size = Integer.valueOf(String.valueOf(f.length())).intValue();
    DataInputStream di = new DataInputStream(new FileInputStream(f));
    byte[] b = new byte[size];

    di.readFully(b);
    di.close();

    input = new ByteArrayInputStream(b);
    input.mark(0);

  }

  /** Parse an individual line in the services file.
   *  @param line the line to be parsed.
   *  @param serviceName the service name.
   *  @param type the type of service (UDP/TCP)
   *  @return the port number matching the service and type, -1 if not found.
   */
  private int parseServicesLine(String line, String serviceName, String type) {

    int port = -1;
    StringTokenizer st = new StringTokenizer(line, " \t/#");

    do {
      // First get the name on the line (parameter 1):
      if (!st.hasMoreTokens()) {
        break;
      }

      String name = st.nextToken().trim();

      // Next get the service name on the line (parameter 2):
      if (!st.hasMoreTokens()) {
        break;
      }

      String portValue = st.nextToken().trim();

      // Finally get the class on the line (parameter 3):
      if (!st.hasMoreTokens()) {
        break;
      }

      String classValue = st.nextToken().trim();

      if (!classValue.equals(type)) {
        break;
      }

      // Return port number, if name on this line matches:
      if (name.equals(serviceName)) {
        try { // Convert the port number string to integer
          port = Integer.parseInt(portValue);
        }
        catch (NumberFormatException ignoredByDesign) {
          // Ignore corrupt /etc/services lines:
          ;
        }
      }

      break;
    }
    while (false);

    return port;
  }

  /** Get the integer port represented by the servicename and class.
   *  @param serviceName the service name.
   *  @param serviceType the type of service (UDP/TCP)
   *  @return the port number matching the service and type, -1 if not found.
   */
  private int getPortByName(String serviceName, String serviceType) {

    int port = -1;

    // Look for our service, line-by-line:
    try {

      String line;
      input.reset();

      // Read /etc/services file.
      // Skip comments and empty lines.
      BufferedReader buf = new BufferedReader(new InputStreamReader(input));

      while ((line = buf.readLine()) != null && port == -1) {
        if (line.length() != 0 && line.charAt(0) != '#') {
          port = parseServicesLine(line, serviceName, serviceType);
        }
      }
    }
    catch (IOException ignoredByDesign) {
      // Ok, we've had an IO error, so just return the port we've found
      // Keep defaults
      ;
    }

    return port; // port number or -1 (on error)
  }

  /**
   * Get the integer port represented by the servicename and class.
   * <p>
   * For exampel, given a line in <tt>/etc/services</tt> of {@code http  80/tcp} A search for service "http" and type "tcp" returns
   * 80
   * 
   * @param serviceName the service name.
   * @param type the type of service (UDP/TCP)
   * @return the port number matching the service and type, -1 if not found.
   */
  public static int getPort(String serviceName, String type) {

    int port = -1;

    try {
      if (service == null) {
        service = new GetServiceByName();
      }

      port = service.getPortByName(serviceName, type);
    }
    catch (Exception ignoredByDesign) {
      // Do Nothing
      ;
    }

    return port;
  }

  /** Convenience Method to get a tcp service method.
   * @see #getPort(String, String)
   */
  public static int getPort(String serviceName) {
    return getPort(serviceName, "tcp");
  }
}
