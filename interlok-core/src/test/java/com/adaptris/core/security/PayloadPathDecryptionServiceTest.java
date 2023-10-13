package com.adaptris.core.security;

public class PayloadPathDecryptionServiceTest extends PayloadPathSecurityServiceCase {

  @Override
  protected CoreSecurityService create() {
    return new PayloadPathDecryptionService();
  }
 
}
