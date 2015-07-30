package com.adaptris.core.jms;


/**
 * Simple interface that provides configuration information for sub components.
 * 
 * @author lchan
 * 
 */
public interface JmsConnectionConfig {

  /**
   * The client id.
   * 
   * @return the client id
   */
  String configuredClientId();

  /**
   * The password.
   * 
   * @return the password
   */
  String configuredPassword();

  /**
   * The username.
   * 
   * @return the username
   */
  String configuredUserName();

  /**
   * The vendor specific implementation.
   * 
   * @return the vendor specific implementation
   */
  VendorImplementation configuredVendorImplementation();

}
