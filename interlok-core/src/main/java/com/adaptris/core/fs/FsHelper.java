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
import java.nio.charset.Charset;

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
  private static final String WIN_DRIVE_REGEX = "^[a-zA-Z]{1}$"; //matches an alphabetic character exactly 1 time in the string.

  /**
   * Go straight to a {@link File} from a url style string.
   *
   */
  @SuppressWarnings({"lgtm [java/path-injection]"})
  public static File toFile(String s) throws IOException, URISyntaxException {
    try {
      return createFileReference(createUrlFromString(s, true));
    } catch (IllegalArgumentException e) {
      return new File(s);
    }
  }

    /**
     * Attempts to convert the String to a File, and if an exception is encountered, fallback to the provided File.
     * @param s
     * @param fallback
     * @return
     */
  public static File toFile(String s, File fallback) {
    try {
      return toFile(s);
    } catch (Exception e) {
      return fallback;
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
    StringBuilder sb = new StringBuilder();
    // include any relative parts captured in the authority part of the URL
    if (url.getAuthority() != null && url.getAuthority().startsWith(".")) sb.append(url.getAuthority());
    if (url.getPath() != null) sb.append(url.getPath());
    String filename = URLDecoder.decode(sb.toString(), charSetToUse);
    // Cope with file://localhost/./config/blah -> /./config/blah is the result of getPath()
    // Munge that properly.
    if (filename.startsWith("/.")) {
      filename = filename.substring(1);
    }
    return new File(filename);
  }

  public static URI createUriFromString(String s, boolean backslashConvert) throws URISyntaxException {
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
        try {
          configuredUri = new URI(URLEncoder.encode(destToConvert, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
          configuredUri = new URI(URLEncoder.encode(destToConvert, Charset.defaultCharset()));
        }

      }
      else {
        throw e;
      }
    }
    return configuredUri;
  }

  /**
   * Creates a {@link URL} based on the passed destination.
   * <p>
   * Supports URLs with both the {@code file scheme} and without. If you define a directory without any leading slash or
   * if it starts with a slash is deemed to be an <strong>absolute</strong> path. If "./" or "../" is used at the start of your definition then
   * the path is deemed to be <strong>relative</strong> . This is true when using the {@code file scheme} or not.
   * </p>
   * <p>
   * With Windows systems the above is above is true, plus if you simply define the <strong>absolute</strong> path including the drive letter
   * e.g. 'c://my/path' this is also valid.
   * </p>
   * <p>
   * Both / and \ slashes are supported.
   * </p>
   *
   * @param s the string to convert to a URL.
   * @param backslashConvert whether or not to convert backslashes into forward slashes.
   *
   */
  public static URL createUrlFromString(String s, boolean backslashConvert) throws IOException, URISyntaxException {
    URI configuredUri = createUriFromString(s, backslashConvert);
    String scheme = configuredUri.getScheme();

    if ("file".equals(scheme)) {
      // nb for some reason, configuredUri.toUrl() doesn't work...
      // return configuredUri.toURL();
      return new URL(configuredUri.toString());
    }
    else {
      boolean isWinDrive = scheme != null && scheme.matches(WIN_DRIVE_REGEX);
      if (scheme == null || isWinDrive) {
        if (!isWinDrive && !s.startsWith(File.separator)) {
          if (!s.startsWith(".")) return new URL("file:///./" + configuredUri);
        }
        return new URL("file:///" + configuredUri);
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
