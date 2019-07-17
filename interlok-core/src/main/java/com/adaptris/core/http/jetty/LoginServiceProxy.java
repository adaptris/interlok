package com.adaptris.core.http.jetty;

import java.util.stream.Collectors;

import javax.servlet.ServletRequest;

import org.eclipse.jetty.security.AbstractLoginService.RolePrincipal;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proxy class that allows us to add "roles" as a custom attribute to the {@link ServletRequest}.
 * 
 * <p>
 * This is to support getting the roles associated with a user from the login service; it isn't directly configurable and is
 * used by {@link HashLoginServiceFactory} to wrap {@link HashLoginService}.
 * </p>
 * 
 */
public class LoginServiceProxy extends ContainerLifeCycle implements LoginService {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private transient LoginService wrappedLoginService;

  public LoginServiceProxy() {
    super();
  }

  // Since we extend ContainerLifecycle we can just add the login service as a bean...
  public LoginServiceProxy withLoginService(LoginService wrapMe) {
    super.addBean(wrapMe);
    wrappedLoginService = wrapMe;
    return this;
  }

  @Override
  public String getName() {
    return wrappedLoginService.getName();
  }

  @Override
  public UserIdentity login(String username, Object credentials, ServletRequest request) {
    UserIdentity id = wrappedLoginService.login(username, credentials, request);
    if (id != null) {
      // Get the roles and add them to the ServletRequest as an attribute.
      String roles =
          id.getSubject().getPrincipals(RolePrincipal.class).stream().map(e -> e.getName()).collect(Collectors.joining(","));
      log.trace("Found roles : {}", roles);
      request.setAttribute(JettyConstants.JETTY_USER_ROLE_ATTR, roles);
    }
    return id;
  }

  @Override
  public boolean validate(UserIdentity user) {
    return wrappedLoginService.validate(user);
  }

  @Override
  public IdentityService getIdentityService() {
    return wrappedLoginService.getIdentityService();

  }

  @Override
  public void setIdentityService(IdentityService service) {
    wrappedLoginService.setIdentityService(service);
  }

  @Override
  public void logout(UserIdentity user) {
    wrappedLoginService.logout(user);
  }

}