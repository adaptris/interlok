/*
 * Copyright 2017 Adaptris Ltd.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import com.adaptris.core.fs.FsHelper;

public abstract class URLHelper {

  /**
   * <p>
   * Connect to the URL specified by this URLString
   * </p>
   *
   * @param loc the URL location.
   * @return an InputStream containing the contents of the URL specified.
   * @throws IOException on error.
   */
  public static InputStream connect(URLString loc) throws IOException {
    if (loc.getProtocol() == null || "file".equals(loc.getProtocol())) {
      return connectToFile(loc.getFile());
    }
    URL url = new URL(loc.toString());
    URLConnection conn = url.openConnection();
    return conn.getInputStream();
  }

  /**
   * Connect to the specified URL.
   * 
   * @param loc the URL
   * @return an InputStream containing the contents of the URL specified.
   */
  public static InputStream connect(String loc) throws IOException, URISyntaxException {
    try {
      URI uri = new URI(loc);
      return connect(new URLString(loc));
    } catch (URISyntaxException e) {
      // URISyntax exception (possibly because of file:///c:/xxx); so let's just assume that
      return connect(new URLString(FsHelper.createUrlFromString(loc, true)));
    }

  }

  /**
   * <p>
   * Create an InputStream from a local file.
   * </p>
   *
   * @param localFile the local file.
   * @return an InputStream from the local file.
   * @throws IOException on error.
   */
  private static InputStream connectToFile(String localFile) throws IOException {
    InputStream in = null;
    File f = new File(localFile);
    if (f.exists()) {
      in = new FileInputStream(f);
    }
    else {
      ClassLoader c = URLHelper.class.getClassLoader();
      URL u = c.getResource(localFile);
      if (u != null) {
        in = u.openStream();
      }
    }
    if (in == null) {
      throw new IOException("Couldn't find [" + localFile + "] on filesystem or classpath");
    }
    return in;
  }

}
