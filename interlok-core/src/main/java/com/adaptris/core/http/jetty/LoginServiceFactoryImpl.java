package com.adaptris.core.http.jetty;

import java.io.File;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.fs.FsWorker;

/**
 * Abstract configuration for bundled {@code org.eclipse.jetty.security.LoginService} implementations.
 *
 */
public abstract class LoginServiceFactoryImpl implements JettyLoginServiceFactory {

  @NotBlank(message = "user realm may not be blank")
  @AutoPopulated
  @InputFieldDefault(value = "InterlokJetty")
  private String userRealm;
  @NotBlank(message = "login service configuration file may not be blank")
  private String filename;

  public LoginServiceFactoryImpl() {
    setUserRealm("InterlokJetty");
  }

  public String getUserRealm() {
    return userRealm;
  }

  /**
   * Set the realm for the login service.
   * 
   * @param userRealm the realm.
   */
  public void setUserRealm(String userRealm) {
    this.userRealm = Args.notNull(userRealm, "userRealm");
  }

  public <T extends LoginServiceFactoryImpl> T withUserRealm(String s) {
    setUserRealm(s);
    return (T) this;
  }

  public String getFilename() {
    return filename;
  }

  public String validateFilename() throws Exception {
    return FsWorker.checkReadable(new File(getFilename())).getCanonicalPath();
  }

  /**
   * Set the filename containing the configuration for the concrete class.
   * <p>
   * Depending on the concrete implementation, the file may well have different contents; for a {@link HashLoginServiceFactory} the
   * file will contain username/password/roles; for a {@link JdbcLoginServiceFactory} it will contain properties that tell the
   * underlying service how to connect to the database.
   * </p>
   * 
   * @param filename the filename containing configuration.
   */
  public void setFilename(String filename) {
    this.filename = Args.notNull(filename, "filename");
  }

  public <T extends LoginServiceFactoryImpl> T withFilename(String s) {
    setFilename(s);
    return (T) this;
  }

}
