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

package com.adaptris.core.management;

import static com.adaptris.core.management.Constants.CFG_KEY_PROXY_AUTHENTICATOR;
import static com.adaptris.core.management.Constants.DEFAULT_PROXY_AUTHENTICATOR;
import static com.adaptris.core.util.PropertyHelper.getPropertyIgnoringCase;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.net.Authenticator.RequestorType;
import java.net.PasswordAuthentication;

import com.adaptris.core.http.AdapterResourceAuthenticator;
import com.adaptris.core.http.ResourceAuthenticator;

final class ProxyAuthenticator {

  private static final String[] PROXY_USER_SYSPROPERTIES =
  {
      "http.proxyUser", "https.proxyUser", "proxyUser"
  };

  private static final String[] PROXY_PASSWORD_SYSPROPERTIES =
  {
      "http.proxyPassword", "https.proxyPassword", "proxyPass"
  };

  static void register(BootstrapProperties config) {
    if (!Boolean.valueOf(getPropertyIgnoringCase(config, CFG_KEY_PROXY_AUTHENTICATOR,
        DEFAULT_PROXY_AUTHENTICATOR))) {
      return;
    }
    String proxyPassword = getSystemProperty(PROXY_PASSWORD_SYSPROPERTIES);
    String proxyUser = getSystemProperty(PROXY_USER_SYSPROPERTIES);
    if (!isEmpty(proxyPassword) && !isEmpty(proxyUser)) {
      AdapterResourceAuthenticator.getInstance().addAuthenticator(
          new ProxyAuth(new PasswordAuthentication(proxyUser, proxyPassword.toCharArray())));
    }
  }

  private static String getSystemProperty(String[] keys) {
    for (String key : keys) {
      if (System.getProperties().containsKey(key)) {
        return System.getProperty(key);
      }
    }
    return null;
  }

  private static class ProxyAuth implements ResourceAuthenticator {
    private PasswordAuthentication auth;

    ProxyAuth(PasswordAuthentication pw) {
      auth = pw;
    }

    @Override
    public PasswordAuthentication authenticate(ResourceTarget target) {
      if (target.getRequestorType().equals(RequestorType.PROXY)) {
        return auth;
      }
      return null;
    }
  }
}
