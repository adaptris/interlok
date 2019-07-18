package com.adaptris.core.http.jetty;

import org.eclipse.jetty.security.JDBCLoginService;
import org.eclipse.jetty.security.LoginService;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Allows you to configure a {@code org.eclipse.jetty.security.JDBCLoginService} as the login service to use with Jetty.
 * <p>
 * This simply exposes the Jetty JdbcLoginService as Interlok configuration. The documentation from
 * <a href="https://wiki.eclipse.org/Jetty/Tutorial/Realms">the eclipse jetty site</a> should always
 * be considered canonical.
 * </p>
 *
 * @config jetty-jdbc-login-service
 */
@XStreamAlias("jetty-jdbc-login-service")
@DisplayOrder(order = {"userRealm", "filename"})
@ComponentProfile(summary = "allows use of org.eclipse.jetty.security.JDBCLoginService to authenticate users",
    tag = "jetty,authentication", since = "3.9.1")
public class JdbcLoginServiceFactory extends LoginServiceFactoryImpl {

  @Override
  public LoginService retrieveLoginService() throws Exception {
    JDBCLoginService loginService = new JDBCLoginService(getUserRealm(), getFilename());
    return new LoginServiceProxy().withLoginService(loginService);
  }

}
