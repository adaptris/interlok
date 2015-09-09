package com.adaptris.core.security;

/**
 */
public class SigningServiceTest extends EncryptionServiceCase {

  public SigningServiceTest(String name) {
    super(name);
  }

  /**
   * @see com.adaptris.core.security.EncryptionServiceCase#create()
   */
  @Override
  protected CoreSecurityService create() {
    return new SigningService();
  }

}
