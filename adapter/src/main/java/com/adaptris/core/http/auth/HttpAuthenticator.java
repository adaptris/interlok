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
package com.adaptris.core.http.auth;

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
   * Undo whatever global state modifications have been made by this HttpAuthenticator.
   */
  public void close();
}
