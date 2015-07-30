/*
 * $RCSfile: SigningServiceTest.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/02/05 13:49:54 $
 * $Author: lchan $
 */
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
