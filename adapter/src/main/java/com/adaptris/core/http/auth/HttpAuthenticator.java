package com.adaptris.core.http.auth;

import java.net.HttpURLConnection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;


/**
 * HttpAuthenticator is an interface designed to facilitate HttpAuthentication in various ways. A HttpAuthenticator
 * object is expected to be used in a try-with-resources statement so that it can be guaranteed that it's close()
 * method will always be called. Some implementations of this interface will need to temporarily mutate global
 * state and therefore this kind of guarantee is highly desirable.
 */
public interface HttpAuthenticator extends AutoCloseable {

  /**
   * Initialize the HttpAuthenticator for a message and return. Any global state mutations should be done here.
   * @param msg
   * @return
   */
  public HttpAuthenticator setup(AdaptrisMessage msg) throws CoreException;

  /**
   * Perform whatever actions are required to the HttpURLConnection after it's been opened (setting custom headers, etc). Not
   * all implementations of this interface will have something to do here.
   */
  public void configureConnection(HttpURLConnection conn);

  /**
   * Undo whatever global state modifications have been made by this HttpAuthenticator.
   */
  public void close();
}
