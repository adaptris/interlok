package com.adaptris.core.http;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.net.HttpURLConnection;

import com.adaptris.core.http.HttpStatusProvider.Status;

/**
 * Builder class for a {@link HttpStatusProvider.Status}.
 * 
 * @author lchan
 *
 */
public class HttpStatusBuilder {


  private static enum StatusCodeWithText {
    
    ACCEPTED(HttpURLConnection.HTTP_ACCEPTED, "Accepted"),
    BAD_GATEWAY(HttpURLConnection.HTTP_BAD_GATEWAY, "Bad Gateway"),
    BAD_METHOD(HttpURLConnection.HTTP_BAD_METHOD, "Method Not Allowed"),
    BAD_REQUEST(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Request"),
    CLIENT_TIMEOUT(HttpURLConnection.HTTP_CLIENT_TIMEOUT, "Request Time-Out"),
    CONFLICT(HttpURLConnection.HTTP_CONFLICT, "Conflict"),
    CREATED(HttpURLConnection.HTTP_CREATED, "Created"),
    ENTITY_TOO_LARGE(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, "Request Entity Too Large"),
    FORBIDDEN(HttpURLConnection.HTTP_FORBIDDEN, "Forbidden"),
    GATEWAY_TIMEOUT(HttpURLConnection.HTTP_GATEWAY_TIMEOUT, "Gateway Timeout"),
    GONE(HttpURLConnection.HTTP_GONE, "Gone"),
    INTERNAL_ERROR(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error"),
    LENGTH_REQUIRED(HttpURLConnection.HTTP_LENGTH_REQUIRED, "Length Required"),
    MOVED_PERM(HttpURLConnection.HTTP_MOVED_PERM, "Moved Permanently"),
    MOVED_TEMP(HttpURLConnection.HTTP_MOVED_TEMP, "Temporary Redirect"),
    MULT_CHOICE(HttpURLConnection.HTTP_MULT_CHOICE, "Multiple Choices"),
    NO_CONTENT(HttpURLConnection.HTTP_NO_CONTENT, "No Content"),
    NOT_ACCEPTABLE(HttpURLConnection.HTTP_NOT_ACCEPTABLE, "Not Acceptable"),
    NOT_AUTHORITATIVE(HttpURLConnection.HTTP_NOT_AUTHORITATIVE, "Non-Authoritative Information"),
    NOT_FOUND(HttpURLConnection.HTTP_NOT_FOUND, "Not Found"),
    NOT_IMPLEMENTED(HttpURLConnection.HTTP_NOT_IMPLEMENTED, "Not Implemented"),
    NOT_MODIFIED(HttpURLConnection.HTTP_NOT_MODIFIED, "Not Modified"),
    OK(HttpURLConnection.HTTP_OK, "OK"),
    PARTIAL(HttpURLConnection.HTTP_PARTIAL, "Partial Content"),
    PAYMENT_REQUIRED(HttpURLConnection.HTTP_PAYMENT_REQUIRED, "Payment Required"),
    PRECON_FAILED(HttpURLConnection.HTTP_PRECON_FAILED, "Precondition Failed"),
    PROXY_AUTH(HttpURLConnection.HTTP_PROXY_AUTH, "Proxy Authentication Required"),
    REQ_TOO_LONG(HttpURLConnection.HTTP_REQ_TOO_LONG, "Request-URI Too Large"),
    RESET(HttpURLConnection.HTTP_RESET, "Reset Content"),
    SEE_OTHER(HttpURLConnection.HTTP_SEE_OTHER, "See Other"),
    UNAUTHORIZED(HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized"),
    UNAVAILABLE(HttpURLConnection.HTTP_UNAVAILABLE, "Service Unavailable"),
    UNSUPPORTED_TYPE(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, "Unsupported Media Type"),
    USE_PROXY(HttpURLConnection.HTTP_USE_PROXY, "Use Proxy"),
    VERSION_NOT_SUPPORTED(HttpURLConnection.HTTP_VERSION, "HTTP Version Not Supported"),
    // These two aren't defined by HttpURLConnection, but we have them for completeness.
    CONTINUE(100, "Continue"),
    SWITCH_PROTOCOL(101, "Switching Protocols");

    private Status status;

    StatusCodeWithText(int code, String text) {
      status = new StatusCodeImpl(code, text);
    }

    private boolean matches(int code) {
      return status.getCode() == code;
    }

    private Status getStatus() {
      return status;
    }
  }
  
  private int code;
  private String text;

  public HttpStatusBuilder() {

  }

  public HttpStatusBuilder withCode(int i) {
    this.code = i;
    return this;
  }
  
  // If what we are writing to is a ServletResponse, then we won't use getText out of the Status at all.
  // We include that capability for completeness.
  public HttpStatusBuilder withText(String s) {
    this.text = s;
    return this;
  }

  public Status build() {
    return create(code, text);
  }

  private static Status create(int code) {
    for (StatusCodeWithText c : StatusCodeWithText.values()) {
      if (c.matches(code)) {
        return c.getStatus();
      }
    }
    return new StatusCodeImpl(code, String.valueOf(code));
  }

  private static Status create(int code, String text) {
    if (isBlank(text)) {
      return create(code);
    }
    return new StatusCodeImpl(code, text);
  }

  private static class StatusCodeImpl implements Status {
    private int code;
    private String responseText;

    private StatusCodeImpl(int code, String text) {
      this.code = code;
      responseText = text;
    }

    @Override
    public int getCode() {
      return code;
    }

    @Override
    public String getText() {
      return responseText;
    }

  }
}
