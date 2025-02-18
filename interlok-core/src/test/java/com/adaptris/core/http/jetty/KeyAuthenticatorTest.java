package com.adaptris.core.http.jetty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.UserPrincipal;
import org.eclipse.jetty.security.authentication.DeferredAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.security.Credential;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class KeyAuthenticatorTest {

  @Test
  void getAuthMethod() {
    KeyAuthenticator authenticator = new KeyAuthenticator("key", "apiKey");
    assertEquals("KEY", authenticator.getAuthMethod());
  }

  @Test
  void validateRequest() throws Exception {
    KeyAuthenticator authenticator = Mockito.spy(new KeyAuthenticator("key", "apiKey"));
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    Mockito.when(req.getHeader("apiKey")).thenReturn("1234");
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);
    Mockito.doReturn(createDummyUserIdentity()).when(authenticator).login(eq("key"), eq("1234"), any());
    Authentication result = authenticator.validateRequest(req, res, true);
    assertNotNull(result);
    assertTrue(result instanceof UserAuthentication);
    UserAuthentication userAuthentication = (UserAuthentication) result;
    assertEquals("username", userAuthentication.getUserIdentity().getUserPrincipal().getName());
  }

  @Test
  void validateRequestFailed() throws Exception {
    KeyAuthenticator authenticator = Mockito.spy(new KeyAuthenticator("key", "apiKey"));
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);
    Mockito.doReturn(null).when(authenticator).login(any(), any(), any());
    Authentication result = authenticator.validateRequest(req, res, true);
    assertTrue(result instanceof Authentication.Failure);
    Mockito.verify(res).sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void validateRequestNotMandatory() throws Exception {
    KeyAuthenticator authenticator = Mockito.spy(new KeyAuthenticator("key", "apiKey"));
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);
    Mockito.doReturn(null).when(authenticator).login(any(), any(), any());
    Authentication result = authenticator.validateRequest(req, res, false);
    assertTrue(result instanceof DeferredAuthentication);
  }

  @Test
  void secureResponse() throws Exception {
    KeyAuthenticator authenticator = new KeyAuthenticator("key", "apiKey");
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);
    Authentication.User user = Mockito.mock(Authentication.User.class);
    assertTrue(authenticator.secureResponse(req, res, true, user));
  }

  private UserIdentity createDummyUserIdentity() {
    return new UserIdentity() {
      @Override
      public Subject getSubject() {
        return null;
      }

      @Override
      public Principal getUserPrincipal() {
        return new UserPrincipal("username", Credential.getCredential("1234"));
      }

      @Override
      public boolean isUserInRole(String role, Scope scope) {
        return true;
      }
    };
  }

}
