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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * No Authentication required.
 * 
 * @author gdries
 *
 */
@JacksonXmlRootElement(localName = "http-no-authentication")
@XStreamAlias("http-no-authentication")
public class NoAuthentication implements HttpAuthenticator {

  @Override
  public void setup(String target, AdaptrisMessage msg, ResourceTargetMatcher auth) throws CoreException {
    ThreadLocalCredentials.getInstance(target).removeThreadCredentials();
  }

  @Override
  public void close() {
  }

}
