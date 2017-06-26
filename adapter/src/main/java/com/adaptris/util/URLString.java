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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;

import javax.mail.URLName;

/**
 * A Simple URL parser, that can parse any given URL into it's constituent parts.
 * <p>
 * A URL should be in the form
 * <code> &lt;protocol>://&lt;user:password>@&lt;host:port>/&lt;path>#ref
 *  </code> Example:
 * <ul>
 * <li>smtp://user%40btinternet.com:password@mail.btinternet.com/</li>
 * <li>http://myname:password@localhost:8080/thisURL</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Does almost the same thing as {@link javax.mail.URLName} but is serializable so it can be used
 * over JMX. The only difference is within the {@link #getURL()} method where a / prefix will be
 * added to the file name for non file url.
 * </p>
 * 
 * 
 * @author $Author: lchan $
 */
public class URLString implements Serializable {

  private static final long serialVersionUID = -4356146361589831204L;

  private static final String PROTOCOL_FILE = "file";

  private transient URLName urlProxy;

  /**
   * The full version of the URL
   */
  protected String fullURL;

  /**
   * The protocol to use (ftp, http, nntp, imap, pop3 ... etc.) .
   */
  private String protocol;

  /**
   * The username to use when connecting
   */
  private String username;

  /**
   * The password to use when connecting.
   */
  private String password;

  /**
   * The host name to which to connect.
   */
  private String host;

  /**
   * The host's IP address, used in equals and hashCode. Computed on demand.
   */
  private InetAddress hostAddress;
  private boolean hostAddressKnown = false;

  /**
   * The protocol port to connect to.
   */
  private int port = -1;

  /**
   * The specified file name on that host.
   */
  private String file;

  /**
   * # reference.
   */
  private String ref;

  /**
   * Our hash code.
   */
  private int hashCode = 0;

  /**
   * Creates a URLString object from the specified protocol, host, port number, file, username, and
   * password. Specifying a port number of -1 indicates that the URL should use the default port for
   * the protocol.
   */
  public URLString(String protocol, String host, int port, String file, String username, String password) {
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    urlProxy = new URLName(protocol, host, port, file, username, password);
    this.file = urlProxy.getFile();
    this.ref = urlProxy.getRef();
    this.username = urlProxy.getUsername();
    this.password = urlProxy.getPassword();
    this.fullURL = urlProxy.toString();
  }

  /**
   * Construct a URLString from a java.net.URL object.
   */
  public URLString(URL url) {
    this(url.toString());
  }

  /**
   * Construct a URLString from a java.io.File object.
   * 
   * @throws MalformedURLException
   */
  public URLString(File file) throws MalformedURLException {
    this(file.toURI().toURL());
  }

  /**
   * Construct a URLString from the string. Parses out all the possible information (protocol, host,
   * port, file, username, password).
   */
  public URLString(String url) {
    urlProxy = new URLName(url);
    this.fullURL = urlProxy.toString();
    this.protocol = urlProxy.getProtocol();
    this.host = urlProxy.getHost();
    this.port = urlProxy.getPort();
    this.file = urlProxy.getFile();
    this.ref = urlProxy.getRef();
    this.username = urlProxy.getUsername();
    this.password = urlProxy.getPassword();
  }

  /**
   * Constructs a string representation of this URLString.
   */
  @Override
  public String toString() {
    if (fullURL == null) {
      // add the "protocol:"
      StringBuffer tempURL = new StringBuffer();
      if (protocol != null) {
        tempURL.append(protocol);
        tempURL.append(":");
      }

      if (username != null || host != null) {
        // add the "//"
        tempURL.append("//");

        // add the user:password@
        // XXX - can you just have a password? without a username?
        if (username != null) {
          tempURL.append(username);

          if (password != null) {
            tempURL.append(":");
            tempURL.append(password);
          }

          tempURL.append("@");
        }

        // add host
        if (host != null) {
          tempURL.append(host);
        }

        // add port (if needed)
        if (port != -1) {
          tempURL.append(":");
          tempURL.append(Integer.toString(port));
        }
        if (file != null) {
          tempURL.append("/");
        }
      }

      // add the file
      if (file != null) {
        tempURL.append(file);
      }

      // add the ref
      if (ref != null) {
        tempURL.append("#");
        tempURL.append(ref);
      }

      // create the fullURL now
      fullURL = tempURL.toString();
    }

    return fullURL;
  }

  /**
   * Returns the port number of this URLString. Returns -1 if the port is not set.
   */
  public int getPort() {
    return port;
  }

  /**
   * Returns the protocol of this URLString. Returns null if this URLString has no protocol.
   */
  public String getProtocol() {
    return protocol;
  }

  /**
   * Returns the file name of this URLString. Returns null if this URLString has no file name.
   */
  public String getFile() {
    return file;
  }

  /**
   * Returns the reference of this URLString. Returns null if this URLString has no reference.
   */
  public String getRef() {
    return ref;
  }

  /**
   * Returns the host of this URLString. Returns null if this URLString has no host.
   */
  public String getHost() {
    return host;
  }

  /**
   * Returns the user name of this URLString. Returns null if this URLString has no user name.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Returns the password of this URLString. Returns null if this URLString has no password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Constructs a URL from the URLString.
   */
  public URL getURL() throws MalformedURLException {
    if (PROTOCOL_FILE.equalsIgnoreCase(getProtocol()) || isEmpty(getProtocol())) {
      // Cope with file:///./path/to/my/thing and perhaps ./path/to/my/thing
      return new URL(getProtocol(), getHost(), getPort(), getFile());
    }
    // Otherwise add a / prefix if it doesn't already exist so that http://my.server/path/to/my/thing
    // is a valid URL
    return new URL(getProtocol(), getHost(), getPort(), slashPrefix(getFile()));
  }

  /**
   * Compares two URLStrings. The result is true if and only if the argument is not null and is a
   * URLString object that represents the same URLString as this object. Two URLString objects are
   * equal if they have the same protocol and the same host, the same port number on the host, the
   * same username, and the same file on the host. The fields (host, username, file) are also
   * considered the same if they are both null.
   * <p>
   * 
   * Hosts are considered equal if the names are equal (case independent) or if host name lookups
   * for them both succeed and they both reference the same IP address.
   * <p>
   * 
   * Note that URLString has no knowledge of default port numbers for particular protocols, so
   * "imap://host" and "imap://host:143" would not compare as equal.
   * <p>
   * 
   * Note also that the password field is not included in the comparison, nor is any reference field
   * appended to the filename.
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof URLString)) {
      return false;
    }
    URLString u2 = (URLString) obj;

    // compare protocols
    if (u2.protocol == null || !u2.protocol.equals(protocol)) {
      return false;
    }

    // compare hosts
    InetAddress a1 = getHostAddress(), a2 = u2.getHostAddress();
    // if we have internet address for both, and they're not the same, fail
    if (a1 != null && a2 != null) {
      if (!a1.equals(a2)) {
        return false;
        // else, if we have host names for both, and they're not the same, fail
      }
    } else if (host != null && u2.host != null) {
      if (!host.equalsIgnoreCase(u2.host)) {
        return false;
        // else, if not both null
      }
    } else if (host != u2.host) {
      return false;
    }
    // at this point, hosts match

    // compare usernames
    if (!(username == u2.username || username != null && username.equals(u2.username))) {
      return false;
    }

    // Forget about password since it doesn't
    // really denote a different store.

    // compare files
    String f1 = file == null ? "" : file;
    String f2 = u2.file == null ? "" : u2.file;

    if (!f1.equals(f2)) {
      return false;
    }

    // compare ports
    if (port != u2.port) {
      return false;
    }

    // all comparisons succeeded, they're equal
    return true;
  }

  /**
   * Compute the hash code for this URLString.
   */
  @Override
  public int hashCode() {
    if (hashCode != 0) {
      return hashCode;
    }
    if (protocol != null) {
      hashCode += protocol.hashCode();
    }
    InetAddress addr = getHostAddress();
    if (addr != null) {
      hashCode += addr.hashCode();
    } else if (host != null) {
      hashCode += host.toLowerCase(Locale.ENGLISH).hashCode();
    }
    if (username != null) {
      hashCode += username.hashCode();
    }
    if (file != null) {
      hashCode += file.hashCode();
    }
    hashCode += port;
    return hashCode;
  }

  /**
   * Get the IP address of our host. Look up the name the first time and remember that we've done
   * so, whether the lookup fails or not.
   */
  private synchronized InetAddress getHostAddress() {
    if (hostAddressKnown) {
      return hostAddress;
    }
    if (host == null) {
      return null;
    }
    try {
      hostAddress = InetAddress.getByName(host);
    } catch (UnknownHostException ex) {
      hostAddress = null;
    }
    hostAddressKnown = true;
    return hostAddress;
  }

  private String slashPrefix(String file) {
    if (!isEmpty(getFile()) && !getFile().startsWith("/")) {
      return "/" + getFile();
    }
    return getFile();
  }


}
