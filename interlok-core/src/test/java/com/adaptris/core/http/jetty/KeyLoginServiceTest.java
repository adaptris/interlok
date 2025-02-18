package com.adaptris.core.http.jetty;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jetty.security.RolePrincipal;
import org.eclipse.jetty.security.UserPrincipal;
import org.eclipse.jetty.util.security.Credential;
import org.junit.jupiter.api.Test;

class KeyLoginServiceTest {

  @Test
  void loadInfo() {
    KeyLoginService loginService = new KeyLoginService("key", "1234", "role");
    UserPrincipal userPrincipal = loginService.loadUserInfo("key");
    assertEquals("key", userPrincipal.getName());
    assertTrue(userPrincipal.authenticate(Credential.getCredential("1234")));
    assertFalse(userPrincipal.authenticate(Credential.getCredential("5678")));
    List<RolePrincipal> roles = loginService.loadRoleInfo(userPrincipal);
    assertEquals(1, roles.size());
    assertEquals("role", roles.get(0).getName());
  }

}
