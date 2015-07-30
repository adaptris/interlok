package com.adaptris.core.security;

import com.adaptris.security.exc.PasswordException;

/**
 * Interface for providing the private key password within adapter configuration.
 *
 * @author lchan
 * 
 */
public interface PrivateKeyPasswordProvider {

  /**
   * Return the private key password as a char[] array.
   *
   * @return the private key password.
   * @throws PasswordException wrapping any other exception.
   */
  char[] retrievePrivateKeyPassword() throws PasswordException;

}
