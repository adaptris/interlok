/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.http.jetty;

import javax.servlet.ServletRequest;

import org.eclipse.jetty.security.LoginService;

public class JettyConstants {

  /**
   * Metadata key for the {@link JettyWrapper} object metadata made available from Jetty.
   */
  public static final String JETTY_WRAPPER = "jettyWrapper";
  /**
   * Metadata key that contains the URL that was used to post data to a Jetty instance : {@value #JETTY_URL}
   *
   * @see javax.servlet.http.HttpServletRequest#getRequestURL()
   */
  public static final String JETTY_URL = "jettyURL";

  /**
   * Metadata key that contains the URI that was used to post data to a Jetty instance : {@value #JETTY_URI}
   *
   * @see javax.servlet.http.HttpServletRequest#getRequestURI()
   */
  public static final String JETTY_URI = "jettyURI";

  /**
   * Metadata key that contains the query string that was used to post data to a Jetty instance : {@value #JETTY_QUERY_STRING}
   *
   * @see javax.servlet.http.HttpServletRequest#getQueryString()
   */
  public static final String JETTY_QUERY_STRING = "jettyQueryString";

  /**
   * Metadata key that contains the roles associated with a user, if available: {@value #JETTY_USER_ROLES}.
   * 
   */
  public static final String JETTY_USER_ROLES = "jettyUserRoles";
  /**
   * Key used to store the the user roles via {@link ServletRequest#setAttribute(String, Object)} if possible.
   * <p>
   * This is intended for use by {@link LoginService} instances to communicate the user roles to {@link BasicJettyConsumer} which
   * will in turn populate the {@link #JETTY_USER_ROLES} metadata key.
   * </p>
   */
  public static final String JETTY_USER_ROLE_ATTR = "_interlokJettyUserRoles";

}
