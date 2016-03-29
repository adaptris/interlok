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

import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;


public abstract class UserPassAuthentication implements HttpAuthenticator {

  private ThreadLocalCredentials threadLocalCreds;
  
  @Override
  public void setup(String target, AdaptrisMessage msg) throws CoreException {
    threadLocalCreds = ThreadLocalCredentials.getInstance(target);
    threadLocalCreds.setThreadCredentials(getPasswordAuthentication(msg));
    AdapterResourceAuthenticator.getInstance().addAuthenticator(threadLocalCreds);
    return;
  }
  
  protected abstract PasswordAuthentication getPasswordAuthentication(AdaptrisMessage msg)
    throws CoreException;

  @Override
  public void configureConnection(HttpURLConnection conn) {
    // Nothing to do here
  }
  
  @Override
  public void close() {
    threadLocalCreds.removeThreadCredentials();
    AdapterResourceAuthenticator.getInstance().removeAuthenticator(threadLocalCreds);
  }

}
