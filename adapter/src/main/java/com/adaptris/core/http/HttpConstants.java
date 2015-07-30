package com.adaptris.core.http;

import com.adaptris.util.text.mime.MimeConstants;

public interface HttpConstants extends MimeConstants {

  /** The default Accept header to use */
  String DEFAULT_ACCEPT = "image/gif */*";
  /**
   * General Fields associated HTTP Headers
   */
  String CACHE_CONTROL = "Cache-Control";
  /**
   * General Fields associated HTTP Headers
   */
  String CONNECTION = "Connection";
  /**
   * General Fields associated HTTP Headers
   */
  String DATE = "Date";
  /**
   * General Fields associated HTTP Headers
   */
  String PRAGMA = "Pragma";
  /**
   * General Fields associated HTTP Headers
   */
  String TRAILER = "Trailer";
  /**
   * General Fields associated HTTP Headers
   */
  String TRANSFER_ENCODING = "Transfer-Encoding";
  /**
   * General Fields associated HTTP Headers
   */
  String UPGRADE = "Upgrade";
  /**
   * General Fields associated HTTP Headers
   */
  String VIA = "Via";
  /**
   * General Fields associated HTTP Headers
   */
  String WARNING = "Warning";
  /**
   * Entity Fields.
   */
  String ALLOW = "Allow";
  /**
   * Entity Fields.
   */
  String CONTENT_ENCODING = "Content-Encoding";
  /**
   * Entity Fields.
   */
  String CONTENT_LANGUAGE = "Content-Language";
  /**
   * Entity Fields.
   */
  String CONTENT_LENGTH = "Content-Length";
  /**
   * Entity Fields.
   */
  String CONTENT_LOCATION = "Content-Location";
  /**
   * Entity Fields.
   */
  String CONTENT_MD5 = "Content-MD5";
  /**
   * Entity Fields.
   */
  String CONTENT_RANGE = "Content-Range";
  /**
   * Entity Fields.
   */
  String CONTENT_TYPE = "Content-Type";
  /**
   * Entity Fields.
   */
  String EXPIRES = "Expires";
  /**
   * Entity Fields.
   */
  String LASTMODIFIED = "Last-Modified";
  /**
   * Request Fields.
   */
  String ACCEPT = "Accept";
  /**
   * Request Fields.
   */
  String ACCEPT_CHARSET = "Accept-Charset";
  /**
   * Request Fields.
   */
  String ACCEPT_ENCODING = "Accept-Encoding";
  /**
   * Request Fields.
   */
  String ACCEPT_LANGUAGE = "Accept-Language";
  /**
   * Request Fields.
   */
  String AUTHORIZATION = "Authorization";
  /**
   * Request Fields.
   */
  String EXPECT = "Expect";
  /**
   * Request Fields.
   */
  String FROM = "From";
  /**
   * Request Fields.
   */
  String HOST = "Host";
  /**
   * Request Fields.
   */
  String IF_MATCH = "If-Match";
  /**
   * Request Fields.
   */
  String IF_MODIFIED_SINCE = "If-Modified-Since";
  /**
   * Request Fields.
   */
  String IF_NONE_MATCH = "If-None-Match";
  /**
   * Request Fields.
   */
  String IF_RANGE = "If-Range";
  /**
   * Request Fields.
   */
  String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
  /**
   * Request Fields.
   */
  String MAX_FORWARDS = "Max-Forwards";
  /**
   * Request Fields.
   */
  String PROXY_AUTHENTICATION = "Proxy-Authentication";
  /**
   * Request Fields.
   */
  String RANGE = "Range";
  /**
   * Request Fields.
   */
  String REFERER = "Referer";
  /**
   * Request Fields.
   */
  String TE = "TE";
  /**
   * Request Fields.
   */
  String USERAGENT = "User-Agent";
  /**
   * Response Fields.
   */
  String ACCEPT_RANGES = "Accept-Ranges";
  /**
   * Response Fields.
   */
  String AGE = "Age";
  /**
   * Response Fields.
   */
  String ETAG = "ETag";
  /**
   * Response Fields.
   */
  String LOCATION = "Location";
  /**
   * Response Fields.
   */
  String PROXY_AUTHENTICATE = "Proxy-Authenticate";
  /**
   * Response Fields.
   */
  String RETRY_AFTER = "Retry-After";
  /**
   * Response Fields.
   */
  String SERVER = "Server";
  /**
   * Response Fields.
   */
  String SERVLET_ENGINE = "Servlet-Engine";
  /**
   * Response Fields.
   */
  String VARY = "Vary";
  /**
   * Response Fields.
   */
  String WWW_AUTHENTICATE = "WWW-Authenticate";
  /**
   * Other Fields.
   */
  String COOKIE = "Cookie";
  /**
   * Other Fields.
   */
  String SET_COOKIE = "Set-Cookie";
  /**
   * Other Fields.
   */
  String SET_COOKIE2 = "Set-Cookie2";
  /**
   * Other Fields.
   */
  String MIME_VERSION = "MIME-Version";
  /**
   * Other Fields.
   */
  String IDENTITY = "identity";
  /**
   * Fields Values.
   */
  String CHUNKED = "chunked";
  /**
   * Fields Values.
   */
  String CLOSE = "close";
  /**
   * Fields Values.
   */
  String KEEP_ALIVE = "keep-alive";
  /**
   * Fields Values.
   */
  String WWW_FORM_URLENCODE = "application/x-www-form-urlencoded";
  // The default server socket timeout, 6 secs
  /**
   * Default server socket timeout, 6secs
   */
  int DEFAULT_SERVER_SOCKET_TIMEOUT = 6000;
  // Set the socket timeout to be 60 secs
  /**
   * Default socket timeout 1min
   *
   */
  int DEFAULT_SOCKET_TIMEOUT = 60000;

  /**
   * a colon
   *
   */
  String COLON = ": ";
  /**
   * Carriage return line feed.
   *
   */
  String CRLF = "\r\n";
  /**
   * A space.
   *
   */
  String SPACE = " ";

  /**
   * HTTP Version
   *
   */
  String VERSION_1 = "HTTP/1.0";
  /**
   * HTTP Version
   *
   */
  String VERSION_1_1 = "HTTP/1.1";

}