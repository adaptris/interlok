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

import javax.mail.URLName;

import com.adaptris.annotation.Removal;

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

  @Deprecated
  @Removal(version = "3.10.0")
  private InetAddress hostAddress; // still here because of serialization
  @Deprecated
  @Removal(version = "3.10.0")
  private boolean hostAddressKnown = false; // still here because of serialization

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

  @Deprecated
  @Removal(version = "3.10.0")
  private int hashCode = 0; // still here because of serialization

  protected URLString() {

  }

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


  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof URLString)) {
      return false;
    }
    URLString u2 = (URLString) obj;
    return toString().equals(u2.toString());
  }

  /**
   * Compute the hash code for this URLString.
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  private String slashPrefix(String file) {
    if (!isEmpty(getFile()) && !getFile().startsWith("/")) {
      return "/" + getFile();
    }
    return getFile();
  }


}
