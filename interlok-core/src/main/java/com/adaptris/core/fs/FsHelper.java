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

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.fs.FsException;
import com.adaptris.fs.FsFilenameExistsException;
import com.adaptris.fs.FsWorker;
import com.adaptris.interlok.util.FileFilterBuilder;

/**
 */
public abstract class FsHelper {

  private static transient Logger log = LoggerFactory.getLogger(FsHelper.class);

  /**
   * Go straight to a {@link File} from a url style string.
   *
   */
  @SuppressWarnings({"lgtm [java/path-injection]"})
  public static File toFile(String s) throws IOException, URISyntaxException {
    try {
      return createFileReference(createUrlFromString(s, true));
    } catch (IllegalArgumentException e) {
      // Catch it from createUrlFromString(), since it's probably c:/file.
      return new File(s);
    }
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
    String charSetToUse = StringUtils.defaultIfBlank(charset, System.getProperty("file.encoding"));
    String filename = URLDecoder.decode(url.getPath(), charSetToUse);
    // Cope with file://localhost/./config/blah -> /./config/blah is the result of getPath()
    // Munge that properly.
    if (filename.startsWith("/.")) {
      filename = filename.substring(1);
    }
    return new File(filename);
  }

  /**
   * Creates a {@link URL} based on the passed destination.
   * <p>
   * If a {@code scheme} is present and is equal to {@code file} then the URL is deemed to be <strong>absolute</strong> and is used
   * as is. If the {@code scheme} is null then the URL is considered a {@code "file"} URL, and <strong>relative</strong> to the
   * current working directory.
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
  public static URL createUrlFromString(String s) throws IOException, URISyntaxException {
    return createUrlFromString(s, false);
  }

  /**
   * Creates a {@link URL} based on the passed destination.
   * <p>
   * If a {@code scheme} is present and is equal to {@code file} then the URL is deemed to be <strong>absolute</strong> and is used
   * as is. If the {@code scheme} is null then the URL is considered a {@code "file"} URL, and <strong>relative</strong> to the
   * current working directory.
   * </p>
   *
   * @param s the string to convert to a URL.
   * @param backslashConvert whether or not to convert backslashes into forward slashes.
   *
   */
  public static URL createUrlFromString(String s, boolean backslashConvert) throws IOException, URISyntaxException {
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
        return new URL("file:///" + configuredUri.toString());
      }
      else {
        throw new IllegalArgumentException("Illegal URL [" + s + "]");
      }
    }
  }

  public static FileFilter createFilter(String filterExpression, String filterImpl) throws Exception {
    return logWarningIfRequired(FileFilterBuilder.build(filterExpression, filterImpl));
  }

  public static FileFilter logWarningIfRequired(FileFilter f) {
    try {
      Class<?> clz = Class.forName("org.apache.oro.io.RegexFilenameFilter");
      if (clz.isAssignableFrom(f.getClass())) {
        log.warn("{} is deprecated, use a java.util.regex.Pattern based filter instead", f.getClass().getCanonicalName());
      }
    }
    catch (Exception e) {

    }
    return f;
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

  // /**
  // *
  // * @param uri the relative <code>URI</code> to process
  // * @return a <code>file:/// URL</code> based on the current working directory (obtained by
  // calling
  // * <code>System.getProperty("user.dir")</code>) plus the passed relative <code>uri</code>
  // * @throws Exception wrapping any underlying <code>Exception</code>
  // */
  // private static URL relativeConfig(URI uri) throws IOException {
  // String pwd = System.getProperty("user.dir");
  //
  // String path = pwd + "/" + uri; // ok even if uri starts with a /
  // URL result = new URL("file:///" + path);
  //
  // return result;
  // }

  private static String backslashToSlash(String url) {
    if (!isEmpty(url)) {
      return url.replaceAll("\\\\", "/");
    }
    return url;
  }
}
