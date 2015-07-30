package com.adaptris.core.management;

import static com.adaptris.core.management.BootstrapProperties.getPropertyIgnoringCase;
import static com.adaptris.core.management.Constants.CFG_KEY_PROXY_AUTHENTICATOR;
import static com.adaptris.core.management.Constants.DEFAULT_PROXY_AUTHENTICATOR;
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
