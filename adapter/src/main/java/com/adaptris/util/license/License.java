/*
 * $Id: License.java,v 1.1 2009/04/28 13:20:25 lchan Exp $
 */
package com.adaptris.util.license;



/**
 * The License interface.
 */
public interface License {
  public static enum LicenseType {
    Restricted, Basic, Standard, Enterprise;
  }

  /**
   * Check if this particular license type is enabled in the license.
   * 
   * @param type the type.
   * @return true if enabled.
   */
  boolean isEnabled(LicenseType type);
  
  /**
   * Verify the license.
   * 
   * @throws LicenseException if a fatal error occurs querying the license.
   */
  void verify() throws LicenseException;

  /**
   * Has the license expired.
   * 
   * @return true if the license has expired.
   * @throws LicenseException if there was error querying the license.
   */
  boolean hasExpired() throws LicenseException;

  /**
   * Get the date the license expires.
   * 
   * @return the date the license expires.
   * @throws LicenseException if there was an error querying the license.
   */
  java.util.Date getExpiry() throws LicenseException;

}
