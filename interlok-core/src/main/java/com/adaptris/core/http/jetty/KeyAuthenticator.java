package com.adaptris.core.http.jetty;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.DeferredAuthentication;
import org.eclipse.jetty.security.authentication.LoginAuthenticator;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.UserIdentity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
class KeyAuthenticator extends LoginAuthenticator {

  static final String AUTH_METHOD = "KEY";

  @Getter
  private final String username;
  @Getter
  private final String headerName;

  @Override
  public String getAuthMethod() {
    return AUTH_METHOD;
  }

  @Override
  public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    String password = request.getHeader(getHeaderName());
    try {
      if (!mandatory) {
        return new DeferredAuthentication(this);
      }
      UserIdentity user = login(getUsername(), password, request);
      if (user != null) {
        return new UserAuthentication(getAuthMethod(), user);
      } else {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return Authentication.SEND_FAILURE;
      }
    } catch (IOException e) {
      throw new ServerAuthException(e);
    }
  }

  @Override
  public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory, Authentication.User validatedUser)
      throws ServerAuthException {
    return true;
  }

}
