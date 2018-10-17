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
package com.adaptris.core.http.auth;

import com.adaptris.core.http.ResourceAuthenticator.ResourceTarget;

/**
 * Allows different HTTP implementations to offer different matching mechanisms for authentication purposes.
 * 
 * <p>
 * For instance, when using {@code org.apache.httpcomponents:httpclient} with {@link java.net.Authenticator#getRequestingURL()} may
 * return null which means you need a slightly different strategy to match a {@link java.net.PasswordAuthentication} instance
 * against a given URL.
 * </p>
 * 
 * @author lchan
 *
 */
public interface ResourceTargetMatcher {

  boolean matches(ResourceTarget target);

}
