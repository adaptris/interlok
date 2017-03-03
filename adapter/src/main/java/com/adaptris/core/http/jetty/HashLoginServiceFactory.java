package com.adaptris.core.http.jetty;

import javax.validation.constraints.NotNull;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation that provides a {@link HashLoginService}.
 * 
 * @author lchan
 *
 */
@XStreamAlias("jetty-hash-login-service")
public class HashLoginServiceFactory implements JettyLoginServiceFactory {
  @NotNull
  @NotBlank
  @AutoPopulated
  private String userRealm;
  @NotNull
  @NotBlank
  private String filename;
  @AdvancedConfig
  @Deprecated
  private Integer refreshInterval;

  public HashLoginServiceFactory() {
    setUserRealm("InterlokJetty");
  }

  public HashLoginServiceFactory(String realm, String filename) {
    this();
    setUserRealm(realm);
    setFilename(filename);
  }

  @Override
  public LoginService retrieveLoginService() {
    HashLoginService loginService = new HashLoginService(getUserRealm(), getFilename());
    if (getRefreshInterval() != null) {
      loginService.setHotReload(true);
    }
    return loginService;
  }

  public String getUserRealm() {
    return userRealm;
  }

  public void setUserRealm(String userRealm) {
    this.userRealm = Args.notNull(userRealm, "userRealm");
  }

  public String getFilename() {
    return filename;
  }

  /**
   * Set the filename containing the username/password/roles.
   * 
   * @param filename the filename.
   */
  public void setFilename(String filename) {
    this.filename = Args.notNull(filename, "filename");
  }


  /**
   * @return the refreshInterval
   * @deprecated since 3.6.0 refresh interval has no effect.
   */
  @Deprecated
  public Integer getRefreshInterval() {
    return refreshInterval;
  }


  /**
   * Specify the refresh interval (in seconds) for monitoring the password file.
   * 
   * @deprecated since 3.6.0 refresh interval has no effect.
   * @param i the refreshInterval to set
   */
  @Deprecated
  public void setRefreshInterval(Integer i) {
    this.refreshInterval = i;
  }
}
