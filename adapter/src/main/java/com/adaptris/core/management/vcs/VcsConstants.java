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

package com.adaptris.core.management.vcs;

/**
 * Constants controlling behaviour when integrating against a version control system.
 * 
 * @author lchan
 *
 */
public class VcsConstants {


  /**
   * Implementation to load, if property not provided will attempt to load one from lib.
   */
  public static final String VSC_IMPLEMENTATION = "vcs.implementation";

  /**
   * Whether or not to always do a git checkout -f (or equivalent)
   */
  public static final String VCS_CLEAN_UPDATE = "vcs.always.reset";

  /**
   * The password to access VCS (if required)
   * 
   */
  public static final String VCS_PASSWORD_KEY = "vcs.password";
  
  /**
   * The username to access VCS (if required)
   * 
   */
  public static final String VCS_USERNAME_KEY = "vcs.username";
  
  /**
   * The local working copy for any files checked out.
   * 
   */
  public static final String VCS_LOCAL_URL_KEY = "vcs.workingcopy.url";
  
  /**
   * The remote VCS url.
   * 
   */
  public static final String VCS_REMOTE_REPO_URL_KEY = "vcs.remote.repo.url";

  /**
   * The revision or branch to checkout.
   * 
   */
  public static final String VCS_REVISION_KEY = "vcs.revision";
  
  public static final String VCS_SSH_TUNNEL_PORT_KEY = "vcs.ssh.tunnel.port";
  
  /**
   * The SSH keyfile
   * 
   */
  public static final String VCS_SSH_KEYFILE_URL_KEY = "vcs.ssh.keyfile.url";
  
  /**
   * The SSH password
   * 
   */
  public static final String VCS_SSH_PASSPHRASE_KEY = "vcs.ssh.passphrase";
  
  public static final String VCS_SSL_CERTIFICATE_URL_KEY = "vcs.ssl.certificate.url";
  
  public static final String VCS_SSL_PASSWORD_KEY = "vcs.ssl.password";
  
  /**
   * The type of VCS authentication which may be different according to each VCS provider.
   * 
   */
  public static final String VCS_AUTHENTICATION_IMPL_KEY = "vcs.auth";

  /**
   * Whether or not we attempt to use a HTTP proxy when performing connections.
   * <p>
   * If the target resource is a HTTP resource, then setting the standard java system properties
   * {@code http.proxyHost, http.proxyPort} will be sufficient. This will only be used when connecting to non-http resources (such
   * as SSH or similar).
   * </p>
   */
  public static final String VCS_SSH_PROXY = "vcs.ssh.proxy";

  /**
   * The proxy username (if any).
   */
  public static final String VCS_SSH_PROXY_USERNAME = "vcs.ssh.proxy.username";

  /**
   * The proxy password (if any).
   */
  public static final String VCS_SSH_PROXY_PASSWORD = "vcs.ssh.proxy.password";
}
