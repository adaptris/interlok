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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

import com.adaptris.util.URLString;

/**
 * Abstract implementation of AdaptrisMarshaller.
 *
 * @author lchan
 *
 */
public abstract class AbstractMarshaller implements AdaptrisMarshaller {

  /**
   * @see AdaptrisMarshaller#marshal(java.lang.Object, java.lang.String)
   */
  @Override
  public void marshal(Object obj, String fileName) throws CoreException {
    this.marshal(obj, new File(fileName));
  }

  /**
   * @see AdaptrisMarshaller#marshal(java.lang.Object, File)
   */
  @Override
  public void marshal(Object obj, File fileName) throws CoreException {
    Writer writer = null;
    try {
      writer = new FileWriter(fileName);
      marshal(obj, writer);
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    finally {
      IOUtils.closeQuietly(writer);
    }
  }

  /**
   * @see AdaptrisMarshaller#marshal(java.lang.Object, OutputStream)
   */
  @Override
  public void marshal(Object obj, OutputStream outputStream) throws CoreException {
    this.marshal(obj, new OutputStreamWriter(outputStream));
  }

  /**
   * @see AdaptrisMarshaller#marshal(java.lang.Object, java.net.URL)
   */
  @Override
  public void marshal(Object obj, URL fileUrl) throws CoreException {
    if (obj == null || fileUrl == null) {
      throw new IllegalArgumentException("Cannot marshall to a null URL");
    }

    if (!fileUrl.getProtocol().equals("file")) { // URL can only be file
      throw new CoreException("URL protocol must be file:");
    }

    this.marshal(obj, new File(fileUrl.getFile()));
  }

  /**
   * @see AdaptrisMarshaller#unmarshal(java.lang.String)
   */
  @Override
  public Object unmarshal(String xml) throws CoreException {
    Reader reader = null;
    try {
      reader = new StringReader(xml);
      return unmarshal(reader);
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    finally {
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * @see AdaptrisMarshaller#unmarshal(java.io.File)
   */
  @Override
  public Object unmarshal(File file) throws CoreException {
    Reader reader = null;
    try {
      reader = new FileReader(file);
      return unmarshal(reader);
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    finally {
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * @see AdaptrisMarshaller#unmarshal(java.net.URL)
   */
  @Override
  public Object unmarshal(URL fileUrl) throws CoreException {
    if (fileUrl == null) {
      throw new IllegalArgumentException("Cannot unmarshall null");
    }
    Reader in = null;
    try {
      in = new InputStreamReader(fileUrl.openStream());
      return this.unmarshal(in);
    }
    catch (IOException e) {
      throw new CoreException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }

  }

  @Override
  public Object unmarshal(URLString loc) throws CoreException {
    Object result = null;
    InputStream in = null;
    try {
      in = connectToUrl(loc);
      if (in != null) {
        result = this.unmarshal(in);
        in.close();
      }
      else {
        throw new IOException("could not unmarshal component from [" + loc + "]");
      }
    }
    catch (IOException e) {
      throw new CoreException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }

    return result;
  }

  @Override
  public Object unmarshal(InputStream stream) throws CoreException {
    return this.unmarshal(new InputStreamReader(stream));
  }

  /**
   * <p>
   * Connect to the specified URL.
   * </p>
   *
   * @param loc the URL location.
   * @return an InputStream containing the contents of the URL specified.
   * @throws IOException on error.
   */
  protected InputStream connectToUrl(URLString loc) throws IOException {
    if (loc.getProtocol() == null || "file".equals(loc.getProtocol())) {
      return connectToFile(loc.getFile());
    }
    URL url = new URL(loc.toString());
    URLConnection conn = url.openConnection();
    //    ProxyUtil.applyBasicProxyAuthorisation(conn);
    return conn.getInputStream();
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
  private InputStream connectToFile(String localFile) throws IOException {
    InputStream in = null;
    File f = new File(localFile);
    if (f.exists()) {
      in = new FileInputStream(f);
    }
    else {
      ClassLoader c = this.getClass().getClassLoader();
      URL u = c.getResource(localFile);

      if (u != null) {
        in = u.openStream();
      }
    }
    return in;
  }

}
