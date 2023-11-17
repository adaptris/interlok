package com.adaptris.core.security;

public class PayloadPathEncryptionServiceTest extends PayloadPathSecurityServiceCase {

  @Override
  protected CoreSecurityService create() {
    return new PayloadPathEncryptionService();
  }
  
}
