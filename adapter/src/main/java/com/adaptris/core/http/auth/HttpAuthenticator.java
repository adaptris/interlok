package com.adaptris.core.http.auth;

import java.net.HttpURLConnection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;


/**
 * HttpAuthenticator is an interface designed to facilitate HttpAuthentication in various ways.
 * <p>
 * Some implementations of this interface will need to temporarily mutate global state and therefore
 * must be closed in a finally statement or try-with-resources block.
 * </p>
 * 
 * @author gdries
 */
public interface HttpAuthenticator extends AutoCloseable {

  /**
   * Initialize the HttpAuthenticator for a message and return. Any global state mutations should be done here.
   * @param target The URL to set authenticate for
   * @param msg The message to set up for
   */
  public void setup(String target, AdaptrisMessage msg) throws CoreException;

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
