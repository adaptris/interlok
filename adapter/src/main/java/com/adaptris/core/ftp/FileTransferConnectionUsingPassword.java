package com.adaptris.core.ftp;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.CoreException;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.security.password.Password;

/**
 * Common data/methods for file transfer connections that use username/password
 *
 * @author dsefton
 * @author $Author: dsefton $
 */
public abstract class FileTransferConnectionUsingPassword extends FileTransferConnection {

  @InputFieldHint(style = "PASSWORD")
  private String defaultPassword;

  /**
   * Get the password.
   *
   * @return the password.
   */

  public String getDefaultPassword() {
    return defaultPassword;
  }

  /**
   * Set the password (supports encrypted passwords).
   * 
   * @param s the password.
   * @see Password#MSCAPI_STYLE
   * @see Password#NON_PORTABLE_PASSWORD
   * @see Password#PORTABLE_PASSWORD
   */
  public void setDefaultPassword(String s) {
    this.defaultPassword = s;
  }

  /**
   *
   * @see com.adaptris.core.NullConnection#initConnection()
   */
  @Override
  protected void initConnection() throws CoreException {
    super.initConnection();

    if (defaultPassword == null) {
      log.warn("No default password, expected to be provided by destination");
    }
  }

  @Override
  protected UserInfo createUserInfo() throws FileTransferException {
    return new UserInfo(getDefaultUserName(), defaultPassword);
  }
}
