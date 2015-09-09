package com.adaptris.core.security;

/**
 */
public class EncryptionSigningServiceTest extends EncryptionServiceCase {

  public EncryptionSigningServiceTest(String name) {
    super(name);
  }

  /**
   * @see com.adaptris.core.security.EncryptionServiceCase#create()
   */
  @Override
  protected CoreSecurityService create() {
    return new EncryptionSigningService();
  }

}
