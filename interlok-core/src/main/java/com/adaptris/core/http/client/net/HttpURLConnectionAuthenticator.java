/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;

import com.adaptris.core.http.auth.HttpAuthenticator;

/**
 * HttpURLConnectionAuthenticator is an interface designed to facilitate HttpAuthentication in various ways.
 * <p>
 * Some implementations of this interface will need to temporarily mutate global state and therefore must be closed in a finally
 * statement or try-with-resources block.
 * </p>
 * 
 * @author gdries
 */
public interface HttpURLConnectionAuthenticator extends HttpAuthenticator {

  /**
   * Perform whatever actions are required to the HttpURLConnection after it's been opened (setting custom headers, etc). Not
   * all implementations of this interface will have something to do here.
   */
  public void configureConnection(HttpURLConnection conn) throws Exception;

}
