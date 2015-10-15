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

package com.adaptris.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.http.HttpConstants;
/** Constants.
 */
public final class Http implements HttpConstants {

  // /** The default Accept header to use */
  // public static final String DEFAULT_ACCEPT = "image/gif */*";
  // /** General Fields associated HTTP Headers
  // */
  // public static final String CACHE_CONTROL = "Cache-Control";
  // /** General Fields associated HTTP Headers
  // */
  // public static final String CONNECTION = "Connection";
  // /** General Fields associated HTTP Headers
  // */
  // public static final String DATE = "Date";
  // /** General Fields associated HTTP Headers
  // */
  // public static final String PRAGMA = "Pragma";
  // /** General Fields associated HTTP Headers
  // */
  // public static final String TRAILER = "Trailer";
  // /** General Fields associated HTTP Headers
  // */
  // public static final String TRANSFER_ENCODING = "Transfer-Encoding";
  // /** General Fields associated HTTP Headers
  // */
  // public static final String UPGRADE = "Upgrade";
  // /** General Fields associated HTTP Headers
  // */
  // public static final String VIA = "Via";
  // /** General Fields associated HTTP Headers
  // */
  // public static final String WARNING = "Warning";
  // /** Entity Fields.
  // */
  // public static final String ALLOW = "Allow";
  // /** Entity Fields.
  // */
  // public static final String CONTENT_ENCODING = "Content-Encoding";
  // /** Entity Fields.
  // */
  // public static final String CONTENT_LANGUAGE = "Content-Language";
  // /** Entity Fields.
  // */
  // public static final String CONTENT_LENGTH = "Content-Length";
  // /** Entity Fields.
  // */
  // public static final String CONTENT_LOCATION = "Content-Location";
  // /** Entity Fields.
  // */
  // public static final String CONTENT_MD5 = "Content-MD5";
  // /** Entity Fields.
  // */
  // public static final String CONTENT_RANGE = "Content-Range";
  // /** Entity Fields.
  // */
  // public static final String CONTENT_TYPE = "Content-Type";
  // /** Entity Fields.
  // */
  // public static final String EXPIRES = "Expires";
  // /** Entity Fields.
  // */
  // public static final String LASTMODIFIED = "Last-Modified";
  // /** Request Fields.
  // */
  // public static final String ACCEPT = "Accept";
  // /** Request Fields.
  // */
  // public static final String ACCEPT_CHARSET = "Accept-Charset";
  // /** Request Fields.
  // */
  // public static final String ACCEPT_ENCODING = "Accept-Encoding";
  // /** Request Fields.
  // */
  // public static final String ACCEPT_LANGUAGE = "Accept-Language";
  // /** Request Fields.
  // */
  // public static final String AUTHORIZATION = "Authorization";
  // /** Request Fields.
  // */
  // public static final String EXPECT = "Expect";
  // /** Request Fields.
  // */
  // public static final String FROM = "From";
  // /** Request Fields.
  // */
  // public static final String HOST = "Host";
  // /** Request Fields.
  // */
  // public static final String IF_MATCH = "If-Match";
  // /** Request Fields.
  // */
  // public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
  // /** Request Fields.
  // */
  // public static final String IF_NONE_MATCH = "If-None-Match";
  // /** Request Fields.
  // */
  // public static final String IF_RANGE = "If-Range";
  // /** Request Fields.
  // */
  // public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
  // /** Request Fields.
  // */
  // public static final String MAX_FORWARDS = "Max-Forwards";
  // /** Request Fields.
  // */
  // public static final String PROXY_AUTHENTICATION = "Proxy-Authentication";
  // /** Request Fields.
  // */
  // public static final String RANGE = "Range";
  // /** Request Fields.
  // */
  // public static final String REFERER = "Referer";
  // /** Request Fields.
  // */
  // public static final String TE = "TE";
  // /** Request Fields.
  // */
  // public static final String USERAGENT = "User-Agent";
  // /** Response Fields.
  // */
  // public static final String ACCEPT_RANGES = "Accept-Ranges";
  // /** Response Fields.
  // */
  // public static final String AGE = "Age";
  // /** Response Fields.
  // */
  // public static final String ETAG = "ETag";
  // /** Response Fields.
  // */
  // public static final String LOCATION = "Location";
  // /** Response Fields.
  // */
  // public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
  // /** Response Fields.
  // */
  // public static final String RETRY_AFTER = "Retry-After";
  // /** Response Fields.
  // */
  // public static final String SERVER = "Server";
  // /** Response Fields.
  // */
  // public static final String SERVLET_ENGINE = "Servlet-Engine";
  // /** Response Fields.
  // */
  // public static final String VARY = "Vary";
  // /** Response Fields.
  // */
  // public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
  // /** Other Fields.
  // */
  // public static final String COOKIE = "Cookie";
  // /** Other Fields.
  // */
  // public static final String SET_COOKIE = "Set-Cookie";
  // /** Other Fields.
  // */
  // public static final String SET_COOKIE2 = "Set-Cookie2";
  // /** Other Fields.
  // */
  // public static final String MIME_VERSION = "MIME-Version";
  // /** Other Fields.
  // */
  // public static final String IDENTITY = "identity";
  // /** Fields Values.
  // */
  // public static final String CHUNKED = "chunked";
  // /** Fields Values.
  // */
  // public static final String CLOSE = "close";
  // /** Fields Values.
  // */
  // public static final String KEEP_ALIVE = "keep-alive";
  // /** Fields Values.
  // */
  // public static final String WWW_FORM_URLENCODE =
  // "application/x-www-form-urlencoded";
  // // The default server socket timeout, 6 secs
  // /** Default server socket timeout, 6secs
  // */
  // public static final int DEFAULT_SERVER_SOCKET_TIMEOUT = 6000;
  // // Set the socket timeout to be 60 secs
  // /** Default socket timeout 1min
  // *
  // */
  // public static final int DEFAULT_SOCKET_TIMEOUT = 60000;
  //
  // /** a colon
  // *
  // */
  // public static final String COLON = ": ";
  // /** Carriage return line feed.
  // *
  // */
  // public static final String CRLF = "\r\n";
  // /** A space.
  // *
  // */
  // public static final String SPACE = " ";
  //
  // /** HTTP Version
  // *
  // */
  // public static final String VERSION_1 = "HTTP/1.0";
  // /** HTTP Version
  // *
  // */
  // public static final String VERSION_1_1 = "HTTP/1.1";

  private Http() {
  }

  /** This is a dummy logger that I can use to for logging
   *  low level trace messages.
   * @return a Logger with a category of com.adaptris.http.SocketLogger.
   */
  public static Log getSocketLogger() {
    return LogFactory.getLog("com.adaptris.http.SocketLogger");
  }
}
