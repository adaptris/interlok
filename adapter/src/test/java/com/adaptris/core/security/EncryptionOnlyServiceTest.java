package com.adaptris.core.security;


/**
 */
public class EncryptionOnlyServiceTest extends EncryptionServiceCase {

  public EncryptionOnlyServiceTest(String name) {
    super(name);
  }

  /**
   * @see com.adaptris.core.security.EncryptionServiceCase#create()
   */
  @Override
  protected CoreSecurityService create() {
    return new EncryptionOnlyService();
  }
}
