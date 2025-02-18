package com.adaptris.core.http.jetty;

import java.util.List;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.RolePrincipal;
import org.eclipse.jetty.security.UserPrincipal;
import org.eclipse.jetty.util.security.Credential;

import lombok.Getter;

class KeyLoginService extends AbstractLoginService {

  @Getter
  private final List<RolePrincipal> roles;
  @Getter
  private final UserPrincipal userPrincipal;

  KeyLoginService(String username, String credential, String role) {
    userPrincipal = new UserPrincipal(username, Credential.getCredential(credential));
    var rolePrincipal = new RolePrincipal(role);
    roles = List.of(rolePrincipal);
  }

  @Override
  protected List<RolePrincipal> loadRoleInfo(UserPrincipal user) {
    return getRoles();
  }

  @Override
  protected UserPrincipal loadUserInfo(String username) {
    return getUserPrincipal();
  }

}
