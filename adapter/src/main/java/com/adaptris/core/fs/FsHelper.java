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

package com.adaptris.core.fs;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.adaptris.fs.FsException;
import com.adaptris.fs.FsFilenameExistsException;
import com.adaptris.fs.FsWorker;

/**
 * <p>
 * Helper class for <code>FsMessageConsumer</code> and <code>FsMessageProducer</code>.
 * </p>
 */
public final class FsHelper {

  private FsHelper() {
    // no instances
  }

  /**
   * Create a file reference from a URL using the platform default encoding for the URL.
   * 
   * @see #createFileReference(URL, String)
   */
  public static File createFileReference(URL url) throws UnsupportedEncodingException {
    return createFileReference(url, null);
  }

  /**
   * Create a file reference from a URL using the platform default encoding for the URL.
   * 
   * @param url the URL.
   * @param charset the encoding that the url is considered to be in.
   * @return a File object
   * @throws UnsupportedEncodingException if the encoding was not supported.
   */
  public static File createFileReference(URL url, String charset) throws UnsupportedEncodingException {
    String charSetToUse = charset == null ? System.getProperty("file.encoding") : charset;
    String filename = URLDecoder.decode(url.getPath(), charSetToUse);
    // Cope with file://localhost/./config/blah -> /./config/blah is the result of getPath()
    // Munge that properly.
    if (filename.startsWith("/.")) {
      filename = filename.substring(1);
    }
    return new File(filename);
  }

  /**
   * <p>
   * Creates a {@link URL} based on the passed destination. If a <i>scheme</i> is present and is equal to <code>"file"</code> then
   * the <code>URL</code> is deemed to be <b>absolute</b> and is used as is. If the <i>scheme</i> is <code>null</code> the
   * <code>URL</code> is deemed to of scheme <code>"file"</code> and <b>relative</b> to the current working directory.
   * </p>
   * <p>
   * Note that this method will not convert backslashes into forward slashes, so passing in a string like {@code ..\dir\} will fail
   * with a URISyntaxException; use {@link #createUrlFromString(String, boolean)} to convert backslashes into forward slashes prior
   * to processing.
   * </p>
   * 
   * @param s the String to convert to a URL.
   * @return a new URL
   * @see #createUrlFromString(String, boolean)
   * @deprecated use {@link #createUrlFromString(String, boolean)} since 3.0.3
   */
  @Deprecated
  public static URL createUrlFromString(String s) throws Exception {
    return createUrlFromString(s, false);
  }

  /**
   * <p>
   * Creates a {@link URL} based on the passed destination. If a <i>scheme</i> is present and is equal to <code>"file"</code> then
   * the <code>URL</code> is deemed to be <b>absolute</b> and is used as is. If the <i>scheme</i> is <code>null</code> the
   * <code>URL</code> is deemed to of scheme <code>"file"</code> and <b>relative</b> to the current working directory.
   * </p>
   * 
   * @param s the string to convert to a URL.
   * @param backslashConvert whether or not to convert backslashes into forward slashes.
   * 
   */
  public static URL createUrlFromString(String s, boolean backslashConvert) throws Exception {
    String destToConvert = backslashConvert ? backslashToSlash(s) : s;
    URI configuredUri = null;
    try {
      configuredUri = new URI(destToConvert);
    }
    catch (URISyntaxException e) {
      // Specifically here to cope with file:///c:/ (which is
      // technically illegal according to RFC2396 but we need
      // to support it
      if (destToConvert.split(":").length >= 3) {
        configuredUri = new URI(URLEncoder.encode(destToConvert, "UTF-8"));
      }
      else {
        throw e;
      }
    }
    String scheme = configuredUri.getScheme();

    if ("file".equals(scheme)) {
      // nb for some reason, configuredUri.toUrl() doesn't work...
      // return configuredUri.toURL();
      return new URL(configuredUri.toString());
    }
    else {
      if (scheme == null) {
        return relativeConfig(configuredUri);
      }
      else {
        throw new IllegalArgumentException("illegal destination [" + s + "]");
      }
    }
  }

  public static FileFilter createFilter(String filterExpression, String filterImpl) throws Exception {
    FileFilter result = null;
    if (isEmpty(filterExpression)) {
      result = new NoOpFileFilter();
    }
    else {
      Class[] paramTypes =
      {
          filterExpression.getClass()
      };
      Object[] args =
      {
          filterExpression
      };
      Class c = Class.forName(filterImpl);
      Constructor cnst = c.getDeclaredConstructor(paramTypes);
      result = (FileFilter) cnst.newInstance(args);
    }
    return result;
  }

  public static File renameFile(File file, String suffix, FsWorker worker) throws FsException {
    File newFile = new File(file.getAbsolutePath() + suffix);

    try {
      worker.rename(file, newFile);
    }
    catch (FsFilenameExistsException e) {
      newFile = new File(file.getParentFile(), System.currentTimeMillis() + "." + file.getName() + suffix);
      worker.rename(file, newFile);
    }
    return newFile;
  }

  /**
   * 
   * @param uri the relative <code>URI</code> to process
   * @return a <code>file:/// URL</code> based on the current working directory (obtained by calling
   *         <code>System.getProperty("user.dir")</code>) plus the passed relative <code>uri</code>
   * @throws Exception wrapping any underlying <code>Exception</code>
   */
  private static URL relativeConfig(URI uri) throws Exception {
    String pwd = System.getProperty("user.dir");

    String path = pwd + "/" + uri; // ok even if uri starts with a /
    URL result = new URL("file:///" + path);

    return result;
  }

  private static String backslashToSlash(String url) {
    if (!isEmpty(url)) {
      return url.replaceAll("\\\\", "/");
    }
    return url;
  }
  
  private static class NoOpFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
      return true;
    }
    
  }
}
