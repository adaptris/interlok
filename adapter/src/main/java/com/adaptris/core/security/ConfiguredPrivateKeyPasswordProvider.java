package com.adaptris.core.security;

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Private key password provider which explicitly configures the password in configuration
 * 
 * <p>
 * Although there is nothing to stop you from having a plain text private key password configured in adapter configuration using
 * this provider; it is strongly discouraged. You should consider using the {@link Password#encode(String, String)} method to
 * generate an encoded password for inserting into configuration.
 * </p>
 * 
 * @config configured-private-key-password-provider
 * @author lchan
 * @see Password#decode(String)
 * 
 */
@XStreamAlias("configured-private-key-password-provider")
public class ConfiguredPrivateKeyPasswordProvider implements PrivateKeyPasswordProvider {
  private transient char[] pkPassword;
  @InputFieldHint(style = "PASSWORD")
  private String encodedPassword = null;

  public ConfiguredPrivateKeyPasswordProvider() {

  }

  public ConfiguredPrivateKeyPasswordProvider(String encPassword) {
    this();
    setEncodedPassword(encPassword);
  }

  /**
   * Return the private key password as a char[] array.
   * 
   * @return the configured private key, decoded using {@link Password#decode(String)}
   */
  @Override
  public char[] retrievePrivateKeyPassword() throws PasswordException {
    if (pkPassword == null && !isEmpty(encodedPassword)) {
      pkPassword = Password.decode(encodedPassword).toCharArray();
    }
    return pkPassword;
  }

  public String getEncodedPassword() {
    return encodedPassword;
  }

  public void setEncodedPassword(String encodedPassword) {
    // don't bother checking for null, as it's perfectly valid.
    this.encodedPassword = encodedPassword;
  }

}
