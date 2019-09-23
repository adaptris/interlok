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

import java.net.PasswordAuthentication;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.security.password.Password;


public abstract class UserPassAuthentication implements HttpAuthenticator {

  private transient ThreadLocalCredentials threadLocalCreds;
  
  @Override
  public void setup(String target, AdaptrisMessage msg, ResourceTargetMatcher matcher) throws CoreException {
    threadLocalCreds = ThreadLocalCredentials.getInstance(target, matcher);
    threadLocalCreds.setThreadCredentials(getPasswordAuthentication(msg));
    AdapterResourceAuthenticator.getInstance().addAuthenticator(threadLocalCreds);
  }
  
  protected abstract PasswordAuthentication getPasswordAuthentication(AdaptrisMessage msg)
    throws CoreException;

  
  @Override
  public void close() {
    threadLocalCreds.removeThreadCredentials();
    AdapterResourceAuthenticator.getInstance().removeAuthenticator(threadLocalCreds);
  }

  protected static char[] decodePassword(String pw) throws CoreException {
    try {
      return Password.decode(pw).toCharArray();
    } catch (Exception e) {
      throw new CoreException("Unable to decode password", e);
    }
  }
}
