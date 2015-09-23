package com.adaptris.core.http;

import java.net.HttpURLConnection;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface for providing a HTTP Status.
 * 
 */
public interface HttpStatusProvider {

  /**
   * Default status code definitions.
   * 
   */
  public enum HttpStatus {
    ACCEPTED_202(HttpURLConnection.HTTP_ACCEPTED),
    BAD_GATEWAY_502(HttpURLConnection.HTTP_BAD_GATEWAY),
    BAD_METHOD_405(HttpURLConnection.HTTP_BAD_METHOD),
    BAD_REQUEST_400(HttpURLConnection.HTTP_BAD_REQUEST),
    REQUEST_TIMEOUT_408(HttpURLConnection.HTTP_CLIENT_TIMEOUT),
    CONFLICT_409(HttpURLConnection.HTTP_CONFLICT),
    CREATED_201(HttpURLConnection.HTTP_CREATED),
    REQUEST_TOO_LARGE_413(HttpURLConnection.HTTP_ENTITY_TOO_LARGE),
    FORBIDDEN_403(HttpURLConnection.HTTP_FORBIDDEN),
    GATEWAY_TIMEOUT_504(HttpURLConnection.HTTP_GATEWAY_TIMEOUT),
    GONE_410(HttpURLConnection.HTTP_GONE),
    INTERNAL_ERROR_500(HttpURLConnection.HTTP_INTERNAL_ERROR),
    LENGTH_REQUIRED_411(HttpURLConnection.HTTP_LENGTH_REQUIRED),
    MOVED_PERM_301(HttpURLConnection.HTTP_MOVED_PERM),
    MOVED_TEMP_302(HttpURLConnection.HTTP_MOVED_TEMP),
    MULT_CHOICE_300(HttpURLConnection.HTTP_MULT_CHOICE),
    NO_CONTENT_204(HttpURLConnection.HTTP_NO_CONTENT),
    NOT_ACCEPTABLE_406(HttpURLConnection.HTTP_NOT_ACCEPTABLE),
    NOT_AUTHORITATIVE_203(HttpURLConnection.HTTP_NOT_AUTHORITATIVE),
    NOT_FOUND_404(HttpURLConnection.HTTP_NOT_FOUND),
    NOT_IMPLEMENTED_501(HttpURLConnection.HTTP_NOT_IMPLEMENTED),
    NOT_MODIFIED_304(HttpURLConnection.HTTP_NOT_MODIFIED),
    OK_200(HttpURLConnection.HTTP_OK),
    PARTIAL_206(HttpURLConnection.HTTP_PARTIAL),
    PAYMENT_REQUIRED_402(HttpURLConnection.HTTP_PAYMENT_REQUIRED),
    PRECON_FAILED_412(HttpURLConnection.HTTP_PRECON_FAILED),
    PROXY_AUTH_407(HttpURLConnection.HTTP_PROXY_AUTH),
    REQUEST_URI_TOO_LONG_414(HttpURLConnection.HTTP_REQ_TOO_LONG),
    RESET_205(HttpURLConnection.HTTP_RESET),
    SEE_OTHER_303(HttpURLConnection.HTTP_SEE_OTHER),
    UNAUTHORIZED_401(HttpURLConnection.HTTP_UNAUTHORIZED),
    UNAVAILABLE_503(HttpURLConnection.HTTP_UNAVAILABLE),
    UNSUPPORTED_TYPE_415(HttpURLConnection.HTTP_UNSUPPORTED_TYPE),
    USE_PROXY_305(HttpURLConnection.HTTP_USE_PROXY),
    HTTP_VERSION_NOT_SUPPORTED_505(HttpURLConnection.HTTP_VERSION),
    CONTINUE_100(100),
    SWITCH_PROTOCOL_101(101);
    private int statusCode;

    private HttpStatus(int i) {
      statusCode = i;
    }

    public int getStatusCode() {
      return statusCode;
    }
  }

  public interface Status {
    /**
     * The HTTP status code itself.
     * 
     * @return the status code.
     */
    int getCode();

    /**
     * The text associated with the status, if any.
     * 
     * @return the text.
     */
    String getText();
  }

  /**
   * Get the method that should be used with the HTTP request.
   * 
   * @param msg the {@link AdaptrisMessage} if required to derive the method.
   * @return the {@link ResponseCodeProvider.Status}.
   * 
   */
  Status getStatus(AdaptrisMessage msg);
}
